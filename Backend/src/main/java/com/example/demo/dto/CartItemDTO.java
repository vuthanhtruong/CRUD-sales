package com.example.demo.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItemDTO {

    private String cartItemId;

    private String productId;

    private String productName;

    private String sizeName;

    private String colorName;

    private Integer quantity;

    private Double price;

    private Double subtotal;
    public CartItemDTO(String cartItemId, String productId, String productName, String sizeName, String colorName, Integer quantity, BigDecimal price) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.sizeName = sizeName;
        this.colorName = colorName;
        this.quantity = quantity;
        this.price = price == null ? null : price.doubleValue();
        this.subtotal = price == null || quantity == null ? 0.0 : price.multiply(BigDecimal.valueOf(quantity)).doubleValue();
    }

}