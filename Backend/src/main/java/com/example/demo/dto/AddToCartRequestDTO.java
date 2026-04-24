package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequestDTO {
    private String productId;
    private String sizeId;
    private String colorId;
    private Integer quantity;
}