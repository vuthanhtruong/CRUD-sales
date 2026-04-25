package com.example.demo.service;

import com.example.demo.dto.ProductReviewDTO;
import com.example.demo.dto.ReviewSummaryDTO;
import com.example.demo.model.ReviewStatus;

import java.util.List;

public interface ProductReviewService {
    List<ProductReviewDTO> publicReviews(String productId);
    ReviewSummaryDTO summary(String productId);
    List<ProductReviewDTO> mine();
    ProductReviewDTO create(ProductReviewDTO dto);
    List<ProductReviewDTO> adminFindAll(ReviewStatus status);
    ProductReviewDTO moderate(String id, ReviewStatus status, String adminReply);
    void delete(String id);
}
