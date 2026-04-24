package com.example.demo.repository;

import com.example.demo.model.Cart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public class CartDAOImpl implements CartDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // CREATE
    @Override
    public void create(Cart cart) {
        entityManager.persist(cart);
    }

    // FIND BY ID
    @Override
    public Optional<Cart> findById(String id) {
        Cart cart = entityManager.find(Cart.class, id);
        return Optional.ofNullable(cart);
    }

    // FIND BY USER ID
    @Override
    public Cart findByUserId(String userId) {
        return entityManager.createQuery(
                        "SELECT c FROM Cart c WHERE c.user.id = :userId",
                        Cart.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Cart getReferenceById(String id) {
        return entityManager.getReference(Cart.class, id);
    }

    // UPDATE
    @Override
    public void update(Cart cart) {
        entityManager.merge(cart);
    }

    // DELETE
    @Override
    public void delete(String id) {
        Cart cart = entityManager.find(Cart.class, id);
        if (cart != null) {
            entityManager.remove(cart);
        }
    }
}