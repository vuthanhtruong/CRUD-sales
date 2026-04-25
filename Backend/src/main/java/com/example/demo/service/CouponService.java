package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponPreviewDTO;
import com.example.demo.model.Coupon;

import java.math.BigDecimal;
import java.util.List;

public interface CouponService {
    List<CouponDTO> findAll();
    CouponDTO create(CouponDTO dto);
    CouponDTO update(String id, CouponDTO dto);
    void delete(String id);
    CouponPreviewDTO preview(String code, BigDecimal subtotal);
    Coupon findValidCoupon(String code, BigDecimal subtotal);
    BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal);
}
