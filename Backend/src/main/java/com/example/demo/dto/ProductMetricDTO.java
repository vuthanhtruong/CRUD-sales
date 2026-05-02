package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Base64;
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
    public ProductMetricDTO(String productId, String productName, BigDecimal price, byte[] imageData, Long viewCount, LocalDateTime lastViewedAt) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
        this.viewCount = viewCount;
        this.lastViewedAt = lastViewedAt;
    }

}
