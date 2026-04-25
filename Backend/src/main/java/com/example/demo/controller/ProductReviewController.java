package com.example.demo.controller;

import com.example.demo.dto.ProductReviewDTO;
import com.example.demo.dto.ReviewSummaryDTO;
import com.example.demo.model.ReviewStatus;
import com.example.demo.service.ProductReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductReviewController {
    private final ProductReviewService productReviewService;

    public ProductReviewController(ProductReviewService productReviewService) {
        this.productReviewService = productReviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewDTO>> publicReviews(@PathVariable String productId) { return ResponseEntity.ok(productReviewService.publicReviews(productId)); }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ReviewSummaryDTO> summary(@PathVariable String productId) { return ResponseEntity.ok(productReviewService.summary(productId)); }

    @GetMapping("/me")
    public ResponseEntity<List<ProductReviewDTO>> mine() { return ResponseEntity.ok(productReviewService.mine()); }

    @PostMapping
    public ResponseEntity<ProductReviewDTO> create(@RequestBody @Valid ProductReviewDTO dto) { return ResponseEntity.ok(productReviewService.create(dto)); }

    @GetMapping("/admin")
    public ResponseEntity<List<ProductReviewDTO>> adminFindAll(@RequestParam(required = false) ReviewStatus status) { return ResponseEntity.ok(productReviewService.adminFindAll(status)); }

    @PutMapping("/admin/{id}/moderate")
    public ResponseEntity<ProductReviewDTO> moderate(@PathVariable String id, @RequestBody Map<String, String> body) {
        ReviewStatus status = ReviewStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(productReviewService.moderate(id, status, body.get("adminReply")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) { productReviewService.delete(id); return ResponseEntity.noContent().build(); }
}
