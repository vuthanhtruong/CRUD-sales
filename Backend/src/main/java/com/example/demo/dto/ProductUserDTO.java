package com.example.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUserDTO {

    private String productId;
    private String productName;
    private BigDecimal price;
    private String image; // 🔥 đổi tên đúng bản chất
}