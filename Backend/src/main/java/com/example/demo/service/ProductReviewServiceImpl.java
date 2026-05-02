package com.example.demo.service;

import com.example.demo.dto.ProductReviewDTO;
import com.example.demo.dto.ReviewSummaryDTO;
import com.example.demo.model.*;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductReviewDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductReviewServiceImpl implements ProductReviewService {
    private final ProductReviewDAO reviewDAO;
    private final ProductDAO productDAO;
    private final AccountDAO accountDAO;

    public ProductReviewServiceImpl(ProductReviewDAO reviewDAO, ProductDAO productDAO, AccountDAO accountDAO) {
        this.reviewDAO = reviewDAO;
        this.productDAO = productDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public List<ProductReviewDTO> publicReviews(String productId) {
        return reviewDAO.findApprovedByProductDTO(productId);
    }

    @Override
    public ReviewSummaryDTO summary(String productId) {
        return new ReviewSummaryDTO(
                productId,
                Math.round(reviewDAO.averageApproved(productId) * 10.0) / 10.0,
                reviewDAO.countApproved(productId),
                reviewDAO.countByProductAndRating(productId, 5),
                reviewDAO.countByProductAndRating(productId, 4),
                reviewDAO.countByProductAndRating(productId, 3),
                reviewDAO.countByProductAndRating(productId, 2),
                reviewDAO.countByProductAndRating(productId, 1)
        );
    }

    @Override
    public List<ProductReviewDTO> mine() {
        return reviewDAO.findMineDTO(currentUser().getId());
    }

    @Override
    public ProductReviewDTO create(ProductReviewDTO dto) {
        Person user = currentUser();
        Product product = productDAO.findById(dto.getProductId());
        if (product == null) throw new RuntimeException("Product not found");
        ProductReview review = reviewDAO.findByUserAndProduct(user.getId(), dto.getProductId()).orElse(new ProductReview());
        review.setUser(user);
        review.setProduct(product);
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());
        review.setStatus(ReviewStatus.PENDING);
        review.setAdminReply(null);
        return toDTO(reviewDAO.save(review));
    }

    @Override
    public List<ProductReviewDTO> adminFindAll(ReviewStatus status) {
        return reviewDAO.findAllDTO(status);
    }

    @Override
    public ProductReviewDTO moderate(String id, ReviewStatus status, String adminReply) {
        if (status == null) throw new IllegalArgumentException("Review status is required");
        ProductReview review = reviewDAO.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(status);
        review.setAdminReply(adminReply);
        return toDTO(reviewDAO.save(review));
    }

    @Override
    public void delete(String id) {
        ProductReview review = reviewDAO.findById(id).orElseThrow(() -> new RuntimeException("Review not found"));
        Person user = currentUser();
        String role = accountDAO.getCurrentAccountRole(accountDAO.getCurrentAccountUsername());
        if (!"ADMIN".equals(role) && (review.getUser() == null || !user.getId().equals(review.getUser().getId()))) {
            throw new RuntimeException("You cannot delete this review");
        }
        reviewDAO.delete(id);
    }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private ProductReviewDTO toDTO(ProductReview r) {
        String customerName = r.getUser() == null ? null : (safe(r.getUser().getFirstName()) + " " + safe(r.getUser().getLastName())).trim();
        String username = null;
        if (r.getUser() != null) {
            Account account = accountDAO.getAccountById(r.getUser().getId());
            if (account != null) username = account.getUsername();
        }
        return new ProductReviewDTO(
                r.getId(),
                r.getProduct() == null ? null : r.getProduct().getProductId(),
                r.getProduct() == null ? null : r.getProduct().getProductName(),
                username,
                customerName,
                r.getOrder() == null ? null : r.getOrder().getId(),
                r.getRating(),
                r.getTitle(),
                r.getComment(),
                r.getStatus(),
                r.getAdminReply(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private String safe(String value) { return value == null ? "" : value; }
}
