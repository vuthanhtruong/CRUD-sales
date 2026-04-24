package com.example.demo.dto;

import lombok.*;

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
}