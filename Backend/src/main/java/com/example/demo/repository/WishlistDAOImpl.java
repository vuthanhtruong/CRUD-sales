package com.example.demo.repository;

import com.example.demo.model.WishlistItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class WishlistDAOImpl implements WishlistDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public WishlistItem save(WishlistItem item) {
        if (item.getId() == null) entityManager.persist(item);
        else item = entityManager.merge(item);
        return item;
    }

    @Override
    public List<WishlistItem> findByUserId(String userId) {
        return entityManager.createQuery(
                        "SELECT DISTINCT w FROM WishlistItem w LEFT JOIN FETCH w.product p LEFT JOIN FETCH p.images WHERE w.user.id = :userId ORDER BY w.createdAt DESC",
                        WishlistItem.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public Optional<WishlistItem> findByUserAndProduct(String userId, String productId) {
        return entityManager.createQuery(
                        "SELECT w FROM WishlistItem w WHERE w.user.id = :userId AND w.product.productId = :productId",
                        WishlistItem.class)
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .getResultStream().findFirst();
    }

    @Override
    public void delete(String id) {
        WishlistItem item = entityManager.find(WishlistItem.class, id);
        if (item != null) entityManager.remove(item);
    }

    @Override
    public void deleteByUserAndProduct(String userId, String productId) {
        findByUserAndProduct(userId, productId).ifPresent(item -> delete(item.getId()));
    }

    @Override
    public long countByProduct(String productId) {
        return entityManager.createQuery("SELECT COUNT(w) FROM WishlistItem w WHERE w.product.productId = :productId", Long.class)
                .setParameter("productId", productId)
                .getSingleResult();
    }

    @Override
    public long countByUser(String userId) {
        return entityManager.createQuery("SELECT COUNT(w) FROM WishlistItem w WHERE w.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
