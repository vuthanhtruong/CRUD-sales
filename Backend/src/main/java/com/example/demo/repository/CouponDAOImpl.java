package com.example.demo.repository;

import com.example.demo.model.Coupon;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CouponDAOImpl implements CouponDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Coupon save(Coupon coupon) {
        if (coupon.getId() == null) entityManager.persist(coupon);
        else coupon = entityManager.merge(coupon);
        return coupon;
    }

    @Override
    public Optional<Coupon> findById(String id) { return Optional.ofNullable(entityManager.find(Coupon.class, id)); }

    @Override
    public Optional<Coupon> findByCode(String code) {
        if (code == null) return Optional.empty();
        return entityManager.createQuery("SELECT c FROM Coupon c WHERE UPPER(c.code) = :code", Coupon.class)
                .setParameter("code", code.trim().toUpperCase())
                .getResultStream().findFirst();
    }

    @Override
    public List<Coupon> findAll() {
        return entityManager.createQuery("SELECT c FROM Coupon c ORDER BY c.createdAt DESC", Coupon.class).getResultList();
    }

    @Override
    public void delete(String id) {
        Coupon coupon = entityManager.find(Coupon.class, id);
        if (coupon != null) entityManager.remove(coupon);
    }
}
