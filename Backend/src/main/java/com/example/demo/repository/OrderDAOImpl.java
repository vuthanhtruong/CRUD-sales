package com.example.demo.repository;

import com.example.demo.dto.OrderTimelineDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.SalesOrder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class OrderDAOImpl implements OrderDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SalesOrder save(SalesOrder order) {
        if (order.getId() == null) {
            entityManager.persist(order);
            return order;
        }
        return entityManager.merge(order);
    }

    @Override
    public Optional<SalesOrder> findById(String id) {
        return entityManager.createQuery(
                        "SELECT DISTINCT o FROM SalesOrder o " +
                                "LEFT JOIN FETCH o.items i " +
                                "LEFT JOIN FETCH i.productVariant v " +
                                "LEFT JOIN FETCH v.product p " +
                                "LEFT JOIN FETCH v.size s " +
                                "LEFT JOIN FETCH v.color c " +
                                "LEFT JOIN FETCH o.user u " +
                                "WHERE o.id = :id",
                        SalesOrder.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<SalesOrder> findByCurrentUser(String username) {
        return entityManager.createQuery(
                        "SELECT DISTINCT o FROM SalesOrder o " +
                                "LEFT JOIN FETCH o.items i " +
                                "LEFT JOIN FETCH i.productVariant v " +
                                "LEFT JOIN FETCH v.product p " +
                                "LEFT JOIN FETCH v.size s " +
                                "LEFT JOIN FETCH v.color c " +
                                "JOIN Account a ON a.user.id = o.user.id " +
                                "WHERE a.username = :username " +
                                "ORDER BY o.createdAt DESC",
                        SalesOrder.class)
                .setParameter("username", username)
                .getResultList();
    }

    @Override
    public List<SalesOrder> findAll(OrderStatus status) {
        String jpql = "SELECT DISTINCT o FROM SalesOrder o " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.productVariant v " +
                "LEFT JOIN FETCH v.product p " +
                "LEFT JOIN FETCH v.size s " +
                "LEFT JOIN FETCH v.color c " +
                "LEFT JOIN FETCH o.user u ";
        if (status != null) {
            jpql += "WHERE o.status = :status ";
        }
        jpql += "ORDER BY o.createdAt DESC";

        var query = entityManager.createQuery(jpql, SalesOrder.class);
        if (status != null) {
            query.setParameter("status", status);
        }
        return query.getResultList();
    }

    @Override
    public SalesOrder updateStatus(String id, OrderStatus status) {
        SalesOrder order = entityManager.find(SalesOrder.class, id);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        order.setStatus(status);
        return entityManager.merge(order);
    }

    @Override
    public long countAll() {
        return entityManager.createQuery("SELECT COUNT(o) FROM SalesOrder o", Long.class).getSingleResult();
    }

    @Override
    public long countByStatus(OrderStatus status) {
        return entityManager.createQuery("SELECT COUNT(o) FROM SalesOrder o WHERE o.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public BigDecimal sumRevenue() {
        BigDecimal revenue = entityManager.createQuery(
                        "SELECT COALESCE(SUM(o.totalAmount), 0) FROM SalesOrder o WHERE o.status = :status",
                        BigDecimal.class)
                .setParameter("status", OrderStatus.COMPLETED)
                .getSingleResult();
        return revenue == null ? BigDecimal.ZERO : revenue;
    }

    private static final String ORDER_DTO_SELECT =
            "SELECT new com.example.demo.dto.OrderDTO(" +
                    "o.id, a.username, CONCAT(CONCAT(COALESCE(u.firstName, ''), ' '), COALESCE(u.lastName, '')), " +
                    "o.status, o.paymentMethod, o.subtotalAmount, o.discountAmount, o.totalAmount, o.couponCode, " +
                    "o.receiverName, o.receiverPhone, o.shippingAddress, o.note, o.createdAt, o.updatedAt) " +
                    "FROM SalesOrder o JOIN o.user u LEFT JOIN Account a ON a.user.id = u.id ";

    @Override
    public Optional<OrderDTO> findByIdDTO(String id) {
        return entityManager.createQuery(
                        ORDER_DTO_SELECT + "WHERE o.id = :id",
                        OrderDTO.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .map(this::hydrateOrderDTO);
    }

    @Override
    public List<OrderDTO> findByCurrentUserDTO(String username) {
        return entityManager.createQuery(
                        ORDER_DTO_SELECT + "WHERE a.username = :username ORDER BY o.createdAt DESC",
                        OrderDTO.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .map(this::hydrateOrderDTO)
                .toList();
    }

    @Override
    public List<OrderDTO> findAllDTO(OrderStatus status) {
        String jpql = ORDER_DTO_SELECT;
        if (status != null) {
            jpql += "WHERE o.status = :status ";
        }
        jpql += "ORDER BY o.createdAt DESC";

        var query = entityManager.createQuery(jpql, OrderDTO.class);
        if (status != null) {
            query.setParameter("status", status);
        }
        return query.getResultList().stream().map(this::hydrateOrderDTO).toList();
    }

    @Override
    public List<OrderItemDTO> findItemsByOrderIdDTO(String orderId) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.OrderItemDTO(i.id, pv.id.productId, i.productName, pv.id.sizeId, i.sizeName, pv.id.colorId, i.colorName, i.quantity, i.unitPrice, i.subtotal) " +
                                "FROM OrderItem i JOIN i.productVariant pv WHERE i.order.id = :orderId",
                        OrderItemDTO.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public List<OrderTimelineDTO> findTimelineByOrderIdDTO(String orderId) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.OrderTimelineDTO(t.id, t.status, t.note, t.createdAt) " +
                                "FROM OrderTimeline t WHERE t.order.id = :orderId ORDER BY t.createdAt ASC",
                        OrderTimelineDTO.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private OrderDTO hydrateOrderDTO(OrderDTO dto) {
        dto.setItems(findItemsByOrderIdDTO(dto.getId()));
        dto.setTimeline(findTimelineByOrderIdDTO(dto.getId()));
        return dto;
    }

}
