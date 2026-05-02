package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUserDTO {

    private String productId;
    private String productName;
    private BigDecimal price;
    private String image; // 🔥 đổi tên đúng bản chất
    public ProductUserDTO(String productId, String productName, BigDecimal price, byte[] imageData) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.image = imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
    }

}