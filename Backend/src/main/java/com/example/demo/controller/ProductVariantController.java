package com.example.demo.controller;

import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductVariantController {

    @Autowired
    private ProductVariantService service;

    @GetMapping
    public ResponseEntity<List<ProductVariantDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{productId}/{sizeId}/{colorId}")
    public ResponseEntity<ProductVariantDTO> findById(
            @PathVariable String productId,
            @PathVariable String sizeId,
            @PathVariable String colorId
    ) {
        ProductVariantDTO result = service.findById(productId, sizeId, colorId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariantDTO>> findByProductId(
            @PathVariable String productId
    ) {
        return ResponseEntity.ok(service.findByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<ProductVariantDTO> create(
            @RequestBody ProductVariantDTO dto
    ) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ProductVariantDTO>> createBatch(
            @RequestBody List<ProductVariantDTO> dtos
    ) {
        return ResponseEntity.ok(service.createBatch(dtos));
    }

    @PutMapping
    public ResponseEntity<ProductVariantDTO> update(
            @RequestBody ProductVariantDTO dto
    ) {
        return ResponseEntity.ok(service.update(dto));
    }


    @DeleteMapping("/{productId}/{sizeId}/{colorId}")
    public ResponseEntity<?> delete(
            @PathVariable String productId,
            @PathVariable String sizeId,
            @PathVariable String colorId
    ) {
        boolean deleted = service.delete(productId, sizeId, colorId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/sizes/unused/{productId}")
    public ResponseEntity<?> findUnusedSizes(@PathVariable String productId) {
        return ResponseEntity.ok(
                service.findUnusedSizesByProductId(productId)
        );
    }

    @GetMapping("/colors/unused/{productId}")
    public ResponseEntity<?> findUnusedColors(@PathVariable String productId) {
        return ResponseEntity.ok(
                service.findUnusedColorsByProductId(productId)
        );
    }
}