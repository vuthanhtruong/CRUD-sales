package com.example.demo.repository;

import com.example.demo.dto.ProductMetricDTO;
import com.example.demo.model.ProductMetric;
import com.example.demo.model.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductMetricDAOImpl implements ProductMetricDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProductMetric save(ProductMetric metric) {
        if (metric.getId() == null) {
            entityManager.persist(metric);
            return metric;
        }
        return entityManager.merge(metric);
    }

    @Override
    public Optional<ProductMetric> findByProductId(String productId) {
        return entityManager.createQuery(
                        "SELECT m FROM ProductMetric m JOIN FETCH m.product p WHERE p.productId = :productId",
                        ProductMetric.class)
                .setParameter("productId", productId)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<ProductMetric> topViewed(int limit) {
        return entityManager.createQuery(
                        "SELECT m FROM ProductMetric m JOIN FETCH m.product p " +
                                "WHERE p.status = :status " +
                                "ORDER BY m.viewCount DESC, m.lastViewedAt DESC",
                        ProductMetric.class)
                .setParameter("status", ProductStatus.ACTIVE)
                .setMaxResults(Math.max(1, Math.min(limit, 20)))
                .getResultList();
    }

    @Override
    public List<ProductMetricDTO> topViewedDTO(int limit) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.ProductMetricDTO(p.productId, p.productName, p.price, img.imageData, m.viewCount, m.lastViewedAt) " +
                                "FROM ProductMetric m JOIN m.product p LEFT JOIN p.images img WITH img.isPrimary = true " +
                                "WHERE p.status = :status ORDER BY m.viewCount DESC, m.lastViewedAt DESC",
                        ProductMetricDTO.class)
                .setParameter("status", ProductStatus.ACTIVE)
                .setMaxResults(Math.max(1, Math.min(limit, 20)))
                .getResultList();
    }

}
