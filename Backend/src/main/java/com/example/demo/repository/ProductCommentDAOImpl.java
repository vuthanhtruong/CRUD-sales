package com.example.demo.repository;

import com.example.demo.model.CommentStatus;
import com.example.demo.model.ProductComment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductCommentDAOImpl implements ProductCommentDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProductComment save(ProductComment comment) {
        if (comment.getId() == null) {
            entityManager.persist(comment);
            return comment;
        }
        return entityManager.merge(comment);
    }

    @Override
    public Optional<ProductComment> findById(String id) {
        return entityManager.createQuery(
                        "SELECT c FROM ProductComment c " +
                                "LEFT JOIN FETCH c.product p " +
                                "LEFT JOIN FETCH c.user u " +
                                "LEFT JOIN FETCH c.parent par " +
                                "WHERE c.id = :id",
                        ProductComment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<ProductComment> findPublicByProduct(String productId) {
        return entityManager.createQuery(
                        "SELECT c FROM ProductComment c " +
                                "LEFT JOIN FETCH c.user u " +
                                "LEFT JOIN FETCH c.parent par " +
                                "WHERE c.product.productId = :productId AND c.status = :status " +
                                "ORDER BY c.createdAt ASC",
                        ProductComment.class)
                .setParameter("productId", productId)
                .setParameter("status", CommentStatus.PUBLISHED)
                .getResultList();
    }

    @Override
    public List<ProductComment> findAll(CommentStatus status) {
        String jpql = "SELECT c FROM ProductComment c LEFT JOIN FETCH c.product p LEFT JOIN FETCH c.user u LEFT JOIN FETCH c.parent par ";
        if (status != null) jpql += "WHERE c.status = :status ";
        jpql += "ORDER BY c.createdAt DESC";

        var query = entityManager.createQuery(jpql, ProductComment.class);
        if (status != null) query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<ProductComment> findMine(String userId) {
        return entityManager.createQuery(
                        "SELECT c FROM ProductComment c LEFT JOIN FETCH c.product p LEFT JOIN FETCH c.parent par " +
                                "WHERE c.user.id = :userId ORDER BY c.createdAt DESC",
                        ProductComment.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void delete(String id) {
        ProductComment comment = entityManager.find(ProductComment.class, id);
        if (comment != null) entityManager.remove(comment);
    }
}
