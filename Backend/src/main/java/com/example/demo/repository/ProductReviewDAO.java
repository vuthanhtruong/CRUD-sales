package com.example.demo.repository;

import com.example.demo.model.ProductReview;
import com.example.demo.model.ReviewStatus;

import java.util.List;
import java.util.Optional;

public interface ProductReviewDAO {
    ProductReview save(ProductReview review);
    Optional<ProductReview> findById(String id);
    Optional<ProductReview> findByUserAndProduct(String userId, String productId);
    List<ProductReview> findApprovedByProduct(String productId);
    List<ProductReview> findMine(String userId);
    List<ProductReview> findAll(ReviewStatus status);
    long countByProductAndRating(String productId, int rating);
    long countApproved(String productId);
    double averageApproved(String productId);
    void delete(String id);
}
