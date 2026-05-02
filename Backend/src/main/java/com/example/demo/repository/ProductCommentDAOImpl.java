package com.example.demo.repository;

import com.example.demo.dto.ProductCommentDTO;
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

    private static final String COMMENT_DTO_SELECT =
            "SELECT new com.example.demo.dto.ProductCommentDTO(" +
                    "c.id, p.productId, p.productName, par.id, a.username, " +
                    "CONCAT(CONCAT(COALESCE(u.firstName, ''), ' '), COALESCE(u.lastName, '')), " +
                    "c.content, c.status, c.helpfulCount, c.createdAt, c.updatedAt) " +
                    "FROM ProductComment c " +
                    "JOIN c.product p " +
                    "JOIN c.user u " +
                    "LEFT JOIN Account a ON a.user.id = u.id " +
                    "LEFT JOIN c.parent par ";

    @Override
    public List<ProductCommentDTO> findPublicByProductDTO(String productId) {
        return entityManager.createQuery(
                        COMMENT_DTO_SELECT +
                                "WHERE p.productId = :productId AND c.status = :status ORDER BY c.createdAt ASC",
                        ProductCommentDTO.class)
                .setParameter("productId", productId)
                .setParameter("status", CommentStatus.PUBLISHED)
                .getResultList();
    }

    @Override
    public List<ProductCommentDTO> findAllDTO(CommentStatus status) {
        String jpql = COMMENT_DTO_SELECT;
        if (status != null) jpql += "WHERE c.status = :status ";
        jpql += "ORDER BY c.createdAt DESC";

        var query = entityManager.createQuery(jpql, ProductCommentDTO.class);
        if (status != null) query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<ProductCommentDTO> findMineDTO(String userId) {
        return entityManager.createQuery(
                        COMMENT_DTO_SELECT + "WHERE u.id = :userId ORDER BY c.createdAt DESC",
                        ProductCommentDTO.class)
                .setParameter("userId", userId)
                .getResultList();
    }

}
