package com.example.demo.controller;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-images")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductImageController {

    private final ProductImageService productImageService;

    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteBatch(@RequestBody List<String> ids) {

        productImageService.deleteImagesByIds(ids);

        return ResponseEntity.ok(Map.of(
                "message", "Selected images deleted successfully"
        ));
    }

    // ================= CREATE SINGLE =================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid ProductImageDTO dto) {

        productImageService.create(dto);

        return ResponseEntity.ok(Map.of(
                "message", "Image created successfully"
        ));
    }

    // ================= CREATE BATCH =================
    @PostMapping("/batch")
    public ResponseEntity<?> createBatch(@RequestBody @Valid List<ProductImageDTO> dtos) {

        productImageService.createBatch(dtos);

        return ResponseEntity.ok(Map.of(
                "message", "Images created successfully"
        ));
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable String id) {

        ProductImageDTO dto = productImageService.findById(id);

        return ResponseEntity.ok(dto);
    }

    // ================= GET BY PRODUCT =================
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> findByProductId(@PathVariable String productId) {

        List<ProductImageDTO> images = productImageService.findByProductId(productId);

        return ResponseEntity.ok(images);
    }

    @PatchMapping("/{id}/primary")
    public ResponseEntity<?> setPrimary(@PathVariable String id) {
        productImageService.setPrimary(id);
        return ResponseEntity.ok(Map.of(
                "message", "Primary image updated successfully"
        ));
    }

    // ================= DELETE SINGLE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {

        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(id);

        productImageService.delete(dto);

        return ResponseEntity.ok(Map.of(
                "message", "Image deleted successfully"
        ));
    }

    // ================= DELETE BY PRODUCT =================
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> deleteByProductId(@PathVariable String productId) {

        productImageService.deleteByProductId(productId);

        return ResponseEntity.ok(Map.of(
                "message", "All images deleted for product"
        ));
    }
}