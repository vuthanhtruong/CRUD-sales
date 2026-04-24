package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartDTO {

    private String cartId;

    private String userId;

    private List<CartItemDTO> items;

}