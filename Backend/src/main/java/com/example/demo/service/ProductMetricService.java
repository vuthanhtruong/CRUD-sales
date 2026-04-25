package com.example.demo.service;

import com.example.demo.dto.ProductMetricDTO;

import java.util.List;

public interface ProductMetricService {
    ProductMetricDTO recordView(String productId);
    List<ProductMetricDTO> trending(int limit);
}
