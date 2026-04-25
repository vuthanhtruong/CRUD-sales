package com.example.demo.repository;

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
}
