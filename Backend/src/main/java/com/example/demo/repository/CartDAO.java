package com.example.demo.repository;

import com.example.demo.dto.CartDTO;
import com.example.demo.model.Cart;

import java.util.Optional;

public interface CartDAO {

    void create(Cart cart);

    Optional<Cart> findById(String id);

    Optional<CartDTO> findByIdDTO(String id);

    Cart findByUserId(String userId);

    CartDTO findByUserIdDTO(String userId);

    void update(Cart cart);

    void delete(String id);

    Cart getReferenceById(String id);

    CartDTO getReferenceByIdDTO(String id);
}