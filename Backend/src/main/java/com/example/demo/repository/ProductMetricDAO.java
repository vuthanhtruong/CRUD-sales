package com.example.demo.repository;

import com.example.demo.dto.ProductMetricDTO;
import com.example.demo.model.ProductMetric;

import java.util.List;
import java.util.Optional;

public interface ProductMetricDAO {
    ProductMetric save(ProductMetric metric);
    Optional<ProductMetric> findByProductId(String productId);
    List<ProductMetric> topViewed(int limit);
    List<ProductMetricDTO> topViewedDTO(int limit);
}
