package com.example.demo.controller;

import com.example.demo.dto.ProductMetricDTO;
import com.example.demo.service.ProductMetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "http://localhost:4200")
public class ProductMetricController {
    private final ProductMetricService metricService;

    public ProductMetricController(ProductMetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping("/products/{productId}/view")
    public ResponseEntity<ProductMetricDTO> recordView(@PathVariable String productId) {
        return ResponseEntity.ok(metricService.recordView(productId));
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ProductMetricDTO>> trending(@RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(metricService.trending(limit));
    }
}
