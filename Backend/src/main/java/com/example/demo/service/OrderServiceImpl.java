package com.example.demo.service;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderTimelineDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final AccountDAO accountDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductVariantDAO productVariantDAO;
    private final OrderDAO orderDAO;
    private final CouponService couponService;
    private final NotificationService notificationService;
    private final WalletService walletService;

    public OrderServiceImpl(AccountDAO accountDAO,
                            CartItemDAO cartItemDAO,
                            ProductVariantDAO productVariantDAO,
                            OrderDAO orderDAO,
                            CouponService couponService,
                            NotificationService notificationService,
                            WalletService walletService) {
        this.accountDAO = accountDAO;
        this.cartItemDAO = cartItemDAO;
        this.productVariantDAO = productVariantDAO;
        this.orderDAO = orderDAO;
        this.couponService = couponService;
        this.notificationService = notificationService;
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

        notificationService.notifyUser(user, "Order created", "Order " + saved.getId() + " has been created.", "ORDER", "/orders");
        return toDTO(saved);
    }

    @Override
    public List<OrderDTO> findMyOrders() {
        return orderDAO.findByCurrentUser(accountDAO.getCurrentAccountUsername())
                .stream()
                .map(this::toDTO)
                .toList();
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
        return toDTO(order);
    }

    @Override
    public List<OrderDTO> findAll(OrderStatus status) {
        return orderDAO.findAll(status).stream().map(this::toDTO).toList();
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

        if (status == OrderStatus.CANCELLED && current.getPaymentMethod() == PaymentMethod.WALLET && current.getTotalAmount() != null && current.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            walletService.refund(current.getUser(), current.getTotalAmount(), current.getId(), "Refund for cancelled order " + current.getId());
        }

        current.setStatus(status);
        current.addTimeline(timeline(status, statusMessage(status)));
        SalesOrder saved = orderDAO.save(current);
        notificationService.notifyUser(saved.getUser(), "Order update", "Order " + saved.getId() + " changed to status " + status + ".", "ORDER", "/orders");
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
