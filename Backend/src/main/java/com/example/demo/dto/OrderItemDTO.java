package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String id;
    private String productId;
    private String productName;
    private String sizeId;
    private String sizeName;
    private String colorId;
    private String colorName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
