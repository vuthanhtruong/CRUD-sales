package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductMetricDTO {
    private String productId;
    private String productName;
    private BigDecimal price;
    private String image;
    private Long viewCount;
    private LocalDateTime lastViewedAt;
}
