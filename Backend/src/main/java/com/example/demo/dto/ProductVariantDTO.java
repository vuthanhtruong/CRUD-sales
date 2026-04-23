package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {

    private String productId;
    private String sizeId;
    private String colorId;

    private String productName;
    private String sizeName;
    private String colorName;

    private Integer quantity;
}