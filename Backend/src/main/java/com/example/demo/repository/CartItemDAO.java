package com.example.demo.repository;

import com.example.demo.model.CartItem;
import com.example.demo.model.ProductVariantId;

import java.util.List;
import java.util.Optional;

public interface CartItemDAO {

    void create(CartItem cartItem);

    Optional<CartItem> findById(String id);

    List<CartItem> findByCartId(String cartId);

    Optional<CartItem> findByCartAndVariant(String cartId, String productId, String sizeId, String colorId);

    void update(CartItem cartItem);

    void delete(String id);

    void deleteByCartId(String cartId);

    List<CartItem> findCurrentUserCartItems();

    void decreaseQuantity(String cartItemId, int amount);

    void increaseQuantity(String cartItemId, int amount);

    // CartItemDAO.java - thêm method
    List<CartItem> findByIds(List<String> ids);

}