package com.example.demo.repository;

import com.example.demo.model.Cart;

import java.util.Optional;

public interface CartDAO {

    void create(Cart cart);

    Optional<Cart> findById(String id);

    Cart findByUserId(String userId);

    void update(Cart cart);

    void delete(String id);

    Cart getReferenceById(String id);
}