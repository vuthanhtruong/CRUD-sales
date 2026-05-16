package com.example.demo.service;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderTimelineDTO;
import com.example.demo.dto.queue.MailQueueMessageDTO;
import com.example.demo.dto.queue.NotificationQueueMessageDTO;
import com.example.demo.messaging.QueuePublisherService;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final AccountDAO accountDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductVariantDAO productVariantDAO;
    private final OrderDAO orderDAO;
    private final CouponService couponService;
    private final NotificationService notificationService;
    private final QueuePublisherService queuePublisherService;
    private final MailSenderService mailSenderService;
    private final WalletService walletService;

    @Value("${app.frontend.orders-url:http://localhost:4200/orders}")
    private String ordersUrl;

    public OrderServiceImpl(AccountDAO accountDAO,
                            CartItemDAO cartItemDAO,
                            ProductVariantDAO productVariantDAO,
                            OrderDAO orderDAO,
                            CouponService couponService,
                            NotificationService notificationService,
                            QueuePublisherService queuePublisherService,
                            MailSenderService mailSenderService,
                            WalletService walletService) {
        this.accountDAO = accountDAO;
        this.cartItemDAO = cartItemDAO;
        this.productVariantDAO = productVariantDAO;
        this.orderDAO = orderDAO;
        this.couponService = couponService;
        this.notificationService = notificationService;
        this.queuePublisherService = queuePublisherService;
        this.mailSenderService = mailSenderService;
        this.walletService = walletService;
    }

    @Override
    public OrderDTO checkout(CheckoutRequestDTO request) {
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("No cart items selected");
        }

        Account account = accountDAO.getAccountByUsername(accountDAO.getCurrentAccountUsername());
        if (account == null || account.getUser() == null) {
            throw new RuntimeException("Account not found");
        }

        Person user = account.getUser();
        List<CartItem> items = cartItemDAO.findByIds(request.getCartItemIds());
        if (items.size() != request.getCartItemIds().size()) {
            throw new RuntimeException("Some cart items were not found");
        }

        for (CartItem item : items) {
            assertOwnCartItem(item, user.getId());
        }

        SalesOrder order = new SalesOrder();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod() == null ? PaymentMethod.COD : request.getPaymentMethod());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setNote(request.getNote());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {
            ProductVariant variant = item.getProductVariant();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Invalid product quantity");
            }

            int updated = productVariantDAO.decreaseQuantity(
                    variant.getId().getProductId(),
                    variant.getId().getSizeId(),
                    variant.getId().getColorId(),
                    quantity
            );

            if (updated == 0) {
                throw new RuntimeException(
                        "Product \"" + variant.getProduct().getProductName() +
                                "\" (" + variant.getSize().getName() + " / " + variant.getColor().getName() +
                                ") is out of stock"
                );
            }

            BigDecimal unitPrice = variant.getProduct().getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductVariant(variant);
            orderItem.setProductName(variant.getProduct().getProductName());
            orderItem.setSizeName(variant.getSize().getName());
            orderItem.setColorName(variant.getColor().getName());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(lineTotal);
            order.addItem(orderItem);
        }

        Coupon coupon = couponService.findValidCoupon(request.getCouponCode(), subtotal);
        BigDecimal discount = couponService.calculateDiscount(coupon, subtotal);
        if (coupon != null) {
            order.setCouponCode(coupon.getCode());
            coupon.setUsedCount((coupon.getUsedCount() == null ? 0 : coupon.getUsedCount()) + 1);
        }
        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setTotalAmount(subtotal.subtract(discount));
        order.addTimeline(timeline(OrderStatus.PENDING, "Order created and waiting for confirmation."));

        SalesOrder saved = orderDAO.save(order);

        if (saved.getPaymentMethod() == PaymentMethod.WALLET) {
            walletService.pay(user, saved.getTotalAmount(), saved.getId(), "Payment for order " + saved.getId());
            saved.addTimeline(timeline(OrderStatus.PENDING, "Wallet payment completed."));
            saved = orderDAO.save(saved);
        }

        for (String id : request.getCartItemIds()) {
            cartItemDAO.delete(id);
        }

        queueOrderCreatedEvents(saved);
        return toDTO(saved);
    }

    @Override
    public List<OrderDTO> findMyOrders() {
        return orderDAO.findByCurrentUserDTO(accountDAO.getCurrentAccountUsername());
    }

    @Override
    public OrderDTO findById(String id) {
        SalesOrder order = orderDAO.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
        String role = accountDAO.getCurrentAccountRole(accountDAO.getCurrentAccountUsername());
        if (!"ADMIN".equals(role)) {
            Person user = accountDAO.getCurrentUser();
            if (order.getUser() == null || !user.getId().equals(order.getUser().getId())) {
                throw new RuntimeException("You cannot access this order");
            }
        }
        return orderDAO.findByIdDTO(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<OrderDTO> findAll(OrderStatus status) {
        return orderDAO.findAllDTO(status);
    }

    @Override
    public OrderDTO updateStatus(String id, OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        SalesOrder current = orderDAO.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        if (current.getStatus() == OrderStatus.COMPLETED && status == OrderStatus.CANCELLED) {
            throw new RuntimeException("Completed orders cannot be cancelled");
        }

        if (status == OrderStatus.CANCELLED && current.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : current.getItems()) {
                ProductVariant variant = item.getProductVariant();
                productVariantDAO.increaseQuantity(
                        variant.getId().getProductId(),
                        variant.getId().getSizeId(),
                        variant.getId().getColorId(),
                        item.getQuantity()
                );
            }
        }

        if (status == OrderStatus.CANCELLED && current.getPaymentMethod() == PaymentMethod.WALLET && current.getTotalAmount() != null && current.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.refund(current.getUser(), current.getTotalAmount(), current.getId(), "Refund for cancelled order " + current.getId());
        }

        current.setStatus(status);
        current.addTimeline(timeline(status, statusMessage(status)));
        SalesOrder saved = orderDAO.save(current);
        queueOrderStatusChangedEvents(saved, status);
        return toDTO(saved);
    }

    private OrderTimeline timeline(OrderStatus status, String note) {
        OrderTimeline event = new OrderTimeline();
        event.setStatus(status);
        event.setNote(note);
        return event;
    }

    private String statusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Order is waiting for confirmation.";
            case CONFIRMED -> "The order has been confirmed.";
            case SHIPPING -> "The order is being shipped.";
            case COMPLETED -> "The order has been completed.";
            case CANCELLED -> "The order was cancelled and stock was restored when needed.";
        };
    }

    private void assertOwnCartItem(CartItem item, String userId) {
        if (item.getCart() == null || item.getCart().getUser() == null || !userId.equals(item.getCart().getUser().getId())) {
            throw new RuntimeException("You cannot modify this cart item");
        }
    }

    private void queueOrderCreatedEvents(SalesOrder order) {
        if (order == null || order.getUser() == null) return;

        Person user = order.getUser();
        String notificationTitle = "Order created";
        String notificationMessage = "Order " + order.getId() + " has been created.";
        MailQueueMessageDTO mailMessage = buildOrderCreatedEmail(order);

        runAfterCommit(() -> {
            queueNotification(user, notificationTitle, notificationMessage, "ORDER", "/orders");
            queueMail(mailMessage);
        });
    }

    private void queueOrderStatusChangedEvents(SalesOrder order, OrderStatus status) {
        if (order == null || order.getUser() == null) return;

        Person user = order.getUser();
        String notificationTitle = "Order update";
        String notificationMessage = "Order " + order.getId() + " changed to status " + status + ".";
        MailQueueMessageDTO mailMessage = buildOrderStatusChangedEmail(order, status);

        runAfterCommit(() -> {
            queueNotification(user, notificationTitle, notificationMessage, "ORDER", "/orders");
            queueMail(mailMessage);
        });
    }

    private MailQueueMessageDTO buildOrderCreatedEmail(SalesOrder order) {
        String email = getUserEmail(order);
        if (email == null) return null;

        String subject = "Nova Commerce - Order " + order.getId() + " created";
        String body = "Hello " + displayName(order) + ",\n\n" +
                "Your order has been created and is waiting for confirmation.\n\n" +
                "Order ID: " + order.getId() + "\n" +
                "Status: " + order.getStatus() + "\n" +
                "Payment method: " + order.getPaymentMethod() + "\n" +
                "Total amount: " + money(order.getTotalAmount()) + "\n" +
                "Shipping address: " + safe(order.getShippingAddress()) + "\n\n" +
                "You can view your order here: " + ordersUrl + "\n\n" +
                "Nova Commerce Team";
        return new MailQueueMessageDTO(email, subject, body);
    }

    private MailQueueMessageDTO buildOrderStatusChangedEmail(SalesOrder order, OrderStatus status) {
        String email = getUserEmail(order);
        if (email == null) return null;

        String subject = "Nova Commerce - Order " + order.getId() + " status updated";
        String body = "Hello " + displayName(order) + ",\n\n" +
                "Your order status has been updated.\n\n" +
                "Order ID: " + order.getId() + "\n" +
                "New status: " + status + "\n" +
                "Message: " + statusMessage(status) + "\n" +
                "Total amount: " + money(order.getTotalAmount()) + "\n\n" +
                "You can view your order here: " + ordersUrl + "\n\n" +
                "Nova Commerce Team";
        return new MailQueueMessageDTO(email, subject, body);
    }

    private String getUserEmail(SalesOrder order) {
        if (order.getUser() == null || order.getUser().getEmail() == null || order.getUser().getEmail().isBlank()) {
            return null;
        }
        return order.getUser().getEmail();
    }

    private String displayName(SalesOrder order) {
        if (order.getReceiverName() != null && !order.getReceiverName().isBlank()) {
            return order.getReceiverName();
        }
        if (order.getUser() == null) return "customer";
        String fullName = (safe(order.getUser().getFirstName()) + " " + safe(order.getUser().getLastName())).trim();
        return fullName.isBlank() ? "customer" : fullName;
    }

    private String money(BigDecimal amount) {
        return amount == null ? "0" : amount.stripTrailingZeros().toPlainString();
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void queueNotification(Person user, String title, String message, String type, String targetUrl) {
        if (user == null) return;
        try {
            queuePublisherService.publishNotification(new NotificationQueueMessageDTO(user.getId(), title, message, type, targetUrl));
        } catch (Exception ex) {
            log.warn("RabbitMQ notification publish failed for user {}. Falling back to direct notification.", user.getId(), ex);
            notificationService.notifyUser(user, title, message, type, targetUrl);
        }
    }

    private void queueMail(MailQueueMessageDTO message) {
        if (message == null || message.getTo() == null || message.getTo().isBlank()) return;
        try {
            queuePublisherService.publishMail(message);
        } catch (Exception ex) {
            log.warn("RabbitMQ mail publish failed for {}. Falling back to direct SMTP.", message.getTo(), ex);
            try {
                mailSenderService.send(message);
            } catch (Exception mailEx) {
                log.warn("Order email could not be sent to {}. Checkout/status update was kept successful.", message.getTo(), mailEx);
            }
        }
    }

    private OrderDTO toDTO(SalesOrder order) {
        String username = null;
        String customerName = null;
        if (order.getUser() != null) {
            customerName = (safe(order.getUser().getFirstName()) + " " + safe(order.getUser().getLastName())).trim();
            Account account = null;
            try {
                account = accountDAO.getAccountById(order.getUser().getId());
            } catch (RuntimeException ignored) {
            }
            if (account != null) {
                username = account.getUsername();
            }
        }

        List<OrderItemDTO> itemDTOs = order.getItems() == null ? List.of() : order.getItems().stream()
                .map(item -> {
                    ProductVariant variant = item.getProductVariant();
                    return new OrderItemDTO(
                            item.getId(),
                            variant == null ? null : variant.getId().getProductId(),
                            item.getProductName(),
                            variant == null ? null : variant.getId().getSizeId(),
                            item.getSizeName(),
                            variant == null ? null : variant.getId().getColorId(),
                            item.getColorName(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    );
                }).toList();

        List<OrderTimelineDTO> timelineDTOs = order.getTimeline() == null ? List.of() : order.getTimeline().stream()
                .map(e -> new OrderTimelineDTO(e.getId(), e.getStatus(), e.getNote(), e.getCreatedAt()))
                .toList();

        return new OrderDTO(
                order.getId(),
                username,
                customerName,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCouponCode(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingAddress(),
                order.getNote(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemDTOs,
                timelineDTOs
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
