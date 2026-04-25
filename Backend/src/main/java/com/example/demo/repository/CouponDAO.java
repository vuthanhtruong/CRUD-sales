package com.example.demo.repository;

import com.example.demo.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponDAO {
    Coupon save(Coupon coupon);
    Optional<Coupon> findById(String id);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findAll();
    void delete(String id);
}
