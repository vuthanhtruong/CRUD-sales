package com.example.demo.repository;

import com.example.demo.model.CartItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public

class CartItemDAOImpl implements CartItemDAO {

    @Override
    public void increaseQuantity(String cartItemId, int amount) {

        CartItem item = entityManager.find(CartItem.class, cartItemId);

        if (item == null) {
            throw new RuntimeException("Cart item not found");
        }

        item.setQuantity(item.getQuantity() + amount);

        // không cần merge vì entity đang managed
    }

    @Override
    public void decreaseQuantity(String cartItemId, int amount) {

        CartItem item = entityManager.find(CartItem.class, cartItemId);

        if (item == null) {
            throw new RuntimeException("Cart item not found");
        }

        int newQuantity = item.getQuantity() - amount;

        // 🔥 nếu <= 0 → xoá luôn
        if (newQuantity <= 0) {
            entityManager.remove(item);
        } else {
            item.setQuantity(newQuantity);
            entityManager.merge(item);
        }
    }

    @Override
    public List<CartItem> findCurrentUserCartItems() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }

        String username = authentication.getName();

        String jpql = """
        SELECT ci
        FROM CartItem ci
        JOIN ci.cart c
        JOIN c.user u
        JOIN Account a ON a.user.id = u.id
        WHERE a.username = :username
    """;

        return entityManager.createQuery(jpql, CartItem.class)
                .setParameter("username", username)
                .getResultList();
    }

    @PersistenceContext
    private EntityManager entityManager;

    // CREATE
    @Override
    public void create(CartItem cartItem) {
        entityManager.persist(cartItem);
    }

    // FIND BY ID
    @Override
    public Optional<CartItem> findById(String id) {
        return Optional.ofNullable(entityManager.find(CartItem.class, id));
    }

    // GET ALL ITEMS BY CART
    @Override
    public List<CartItem> findByCartId(String cartId) {
        return entityManager.createQuery(
                        "SELECT ci FROM CartItem ci " +
                                "JOIN FETCH ci.productVariant pv " +
                                "WHERE ci.cart.id = :cartId",
                        CartItem.class)
                .setParameter("cartId", cartId)
                .getResultList();
    }

    // FIND EXISTING ITEM (TRÁNH DUPLICATE)
    @Override
    public Optional<CartItem> findByCartAndVariant(String cartId, String productId, String sizeId, String colorId) {
        return entityManager.createQuery(
                        "SELECT ci FROM CartItem ci " +
                                "WHERE ci.cart.id = :cartId " +
                                "AND ci.productVariant.product.id = :productId " +
                                "AND ci.productVariant.size.id = :sizeId " +
                                "AND ci.productVariant.color.id = :colorId",
                        CartItem.class)
                .setParameter("cartId", cartId)
                .setParameter("productId", productId)
                .setParameter("sizeId", sizeId)
                .setParameter("colorId", colorId)
                .getResultStream()
                .findFirst();
    }

    // UPDATE
    @Override
    public void update(CartItem cartItem) {
        entityManager.merge(cartItem);
    }

    // DELETE ONE ITEM
    @Override
    public void delete(String id) {
        CartItem item = entityManager.find(CartItem.class, id);
        if (item != null) {
            entityManager.remove(item);
        }
    }

    // DELETE ALL ITEMS IN CART
    @Override
    public void deleteByCartId(String cartId) {
        entityManager.createQuery(
                        "DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
                .setParameter("cartId", cartId)
                .executeUpdate();
    }
}