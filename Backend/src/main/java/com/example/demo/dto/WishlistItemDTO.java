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
public class WishlistItemDTO {
    private String id;
    private String productId;
    private String productName;
    private BigDecimal price;
    private String image;
    private LocalDateTime createdAt;
    public WishlistItemDTO(String id, String productId, String productName, BigDecimal price, byte[] imageData, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
        this.createdAt = createdAt;
    }

}
