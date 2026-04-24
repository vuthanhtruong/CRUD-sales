package com.example.demo.service;

import com.example.demo.dto.AddToCartRequestDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemService {
    void create(AddToCartRequestDTO cartItem);

    Optional<CartItemDTO> findById(String id);

    List<CartItemDTO> findByCartId(String cartId);

    Optional<CartItemDTO> findByCartAndVariant(String cartId, String productId, String sizeId, String colorId);

    void update(CartItemDTO cartItem);

    void delete(String id);

    void deleteByCartId(String cartId);

    List<CartItemDTO> findCurrentUserCartItems();
}
