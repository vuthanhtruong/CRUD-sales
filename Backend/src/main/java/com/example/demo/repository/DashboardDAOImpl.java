package com.example.demo.repository;

import com.example.demo.dto.DashboardChartDTO;
import com.example.demo.dto.ProductVariantDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DashboardDAOImpl implements DashboardDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public long countUsers() {
        return entityManager.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
    }

    @Override
    public long countLowStockVariants(int threshold) {
        return entityManager.createQuery(
                        "SELECT COUNT(v) FROM ProductVariant v WHERE v.quantity > 0 AND v.quantity <= :threshold",
                        Long.class)
                .setParameter("threshold", threshold)
                .getSingleResult();
    }

    @Override
    public List<DashboardChartDTO> revenueLastDays(int days) {
        LocalDateTime from = LocalDateTime.now().minusDays(Math.max(1, days));
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT FUNCTION('DATE', o.createdAt), COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
                                "FROM SalesOrder o WHERE o.createdAt >= :from AND o.status = com.example.demo.model.OrderStatus.COMPLETED " +
                                "GROUP BY FUNCTION('DATE', o.createdAt) ORDER BY FUNCTION('DATE', o.createdAt)",
                        Object[].class)
                .setParameter("from", from)
                .getResultList();
        List<DashboardChartDTO> result = new ArrayList<>();
        for (Object[] row : rows) result.add(new DashboardChartDTO(String.valueOf(row[0]), (BigDecimal) row[1], (Long) row[2]));
        return result;
    }

    @Override
    public List<DashboardChartDTO> orderStatusDistribution() {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT o.status, COUNT(o) FROM SalesOrder o GROUP BY o.status",
                        Object[].class)
                .getResultList();
        List<DashboardChartDTO> result = new ArrayList<>();
        for (Object[] row : rows) result.add(new DashboardChartDTO(String.valueOf(row[0]), BigDecimal.ZERO, (Long) row[1]));
        return result;
    }

    @Override
    public List<DashboardChartDTO> topSellingProducts(int limit) {
        List<Object[]> rows = entityManager.createQuery(
                        "SELECT i.productName, COALESCE(SUM(i.subtotal), 0), COALESCE(SUM(i.quantity), 0) " +
                                "FROM OrderItem i GROUP BY i.productName ORDER BY COALESCE(SUM(i.quantity), 0) DESC",
                        Object[].class)
                .setMaxResults(Math.max(1, limit))
                .getResultList();
        List<DashboardChartDTO> result = new ArrayList<>();
        for (Object[] row : rows) result.add(new DashboardChartDTO(String.valueOf(row[0]), (BigDecimal) row[1], ((Number) row[2]).longValue()));
        return result;
    }

    @Override
    public List<ProductVariantDTO> lowStockVariants(int threshold) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.ProductVariantDTO(v.product.productId, v.size.id, v.color.id, v.product.productName, v.size.name, v.color.name, v.quantity) " +
                                "FROM ProductVariant v WHERE v.quantity > 0 AND v.quantity <= :threshold ORDER BY v.quantity ASC",
                        ProductVariantDTO.class)
                .setParameter("threshold", threshold)
                .setMaxResults(50)
                .getResultList();
    }
}
