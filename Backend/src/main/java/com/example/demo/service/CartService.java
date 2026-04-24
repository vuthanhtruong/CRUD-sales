package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    void create(CartDTO cart);

    Optional<CartDTO> findById(String id);

    CartDTO findByUserId(String userId);

    void update(CartDTO cart);

    void delete(String id);

    CartDTO getReferenceById(String id);
}
