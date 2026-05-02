package com.example.demo.repository;

import com.example.demo.dto.ProductReviewDTO;
import com.example.demo.model.ProductReview;
import com.example.demo.model.ReviewStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductReviewDAOImpl implements ProductReviewDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ProductReview save(ProductReview review) {
        if (review.getId() == null) entityManager.persist(review);
        else review = entityManager.merge(review);
        return review;
    }

    @Override
    public Optional<ProductReview> findById(String id) {
        return entityManager.createQuery(
                        "SELECT r FROM ProductReview r LEFT JOIN FETCH r.product p LEFT JOIN FETCH r.user u LEFT JOIN FETCH r.order o WHERE r.id = :id",
                        ProductReview.class)
                .setParameter("id", id)
                .getResultStream().findFirst();
    }

    @Override
    public Optional<ProductReview> findByUserAndProduct(String userId, String productId) {
        return entityManager.createQuery(
                        "SELECT r FROM ProductReview r WHERE r.user.id = :userId AND r.product.productId = :productId",
                        ProductReview.class)
                .setParameter("userId", userId)
                .setParameter("productId", productId)
                .getResultStream().findFirst();
    }

    @Override
    public List<ProductReview> findApprovedByProduct(String productId) {
        return entityManager.createQuery(
                        "SELECT r FROM ProductReview r LEFT JOIN FETCH r.user u WHERE r.product.productId = :productId AND r.status = :status ORDER BY r.createdAt DESC",
                        ProductReview.class)
                .setParameter("productId", productId)
                .setParameter("status", ReviewStatus.APPROVED)
                .getResultList();
    }

    @Override
    public List<ProductReview> findMine(String userId) {
        return entityManager.createQuery(
                        "SELECT r FROM ProductReview r LEFT JOIN FETCH r.product p WHERE r.user.id = :userId ORDER BY r.createdAt DESC",
                        ProductReview.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<ProductReview> findAll(ReviewStatus status) {
        String jpql = "SELECT r FROM ProductReview r LEFT JOIN FETCH r.product p LEFT JOIN FETCH r.user u ";
        if (status != null) jpql += "WHERE r.status = :status ";
        jpql += "ORDER BY r.createdAt DESC";
        var q = entityManager.createQuery(jpql, ProductReview.class);
        if (status != null) q.setParameter("status", status);
        return q.getResultList();
    }

    @Override
    public long countByProductAndRating(String productId, int rating) {
        return entityManager.createQuery(
                        "SELECT COUNT(r) FROM ProductReview r WHERE r.product.productId = :productId AND r.status = :status AND r.rating = :rating",
                        Long.class)
                .setParameter("productId", productId)
                .setParameter("status", ReviewStatus.APPROVED)
                .setParameter("rating", rating)
                .getSingleResult();
    }

    @Override
    public long countApproved(String productId) {
        return entityManager.createQuery(
                        "SELECT COUNT(r) FROM ProductReview r WHERE r.product.productId = :productId AND r.status = :status",
                        Long.class)
                .setParameter("productId", productId)
                .setParameter("status", ReviewStatus.APPROVED)
                .getSingleResult();
    }

    @Override
    public double averageApproved(String productId) {
        Double avg = entityManager.createQuery(
                        "SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.productId = :productId AND r.status = :status",
                        Double.class)
                .setParameter("productId", productId)
                .setParameter("status", ReviewStatus.APPROVED)
                .getSingleResult();
        return avg == null ? 0.0 : avg;
    }

    @Override
    public void delete(String id) {
        ProductReview review = entityManager.find(ProductReview.class, id);
        if (review != null) entityManager.remove(review);
    }

    private static final String REVIEW_DTO_SELECT =
            "SELECT new com.example.demo.dto.ProductReviewDTO(" +
                    "r.id, p.productId, p.productName, a.username, " +
                    "CONCAT(CONCAT(COALESCE(u.firstName, ''), ' '), COALESCE(u.lastName, '')), " +
                    "o.id, r.rating, r.title, r.comment, r.status, r.adminReply, r.createdAt, r.updatedAt) " +
                    "FROM ProductReview r " +
                    "JOIN r.product p " +
                    "JOIN r.user u " +
                    "LEFT JOIN Account a ON a.user.id = u.id " +
                    "LEFT JOIN r.order o ";

    @Override
    public List<ProductReviewDTO> findApprovedByProductDTO(String productId) {
        return entityManager.createQuery(
                        REVIEW_DTO_SELECT +
                                "WHERE p.productId = :productId AND r.status = :status ORDER BY r.createdAt DESC",
                        ProductReviewDTO.class)
                .setParameter("productId", productId)
                .setParameter("status", ReviewStatus.APPROVED)
                .getResultList();
    }

    @Override
    public List<ProductReviewDTO> findMineDTO(String userId) {
        return entityManager.createQuery(
                        REVIEW_DTO_SELECT + "WHERE u.id = :userId ORDER BY r.createdAt DESC",
                        ProductReviewDTO.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<ProductReviewDTO> findAllDTO(ReviewStatus status) {
        String jpql = REVIEW_DTO_SELECT;
        if (status != null) jpql += "WHERE r.status = :status ";
        jpql += "ORDER BY r.createdAt DESC";
        var q = entityManager.createQuery(jpql, ProductReviewDTO.class);
        if (status != null) q.setParameter("status", status);
        return q.getResultList();
    }

}
