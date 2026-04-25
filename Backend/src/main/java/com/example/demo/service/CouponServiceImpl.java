package com.example.demo.service;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponPreviewDTO;
import com.example.demo.model.Coupon;
import com.example.demo.model.DiscountType;
import com.example.demo.repository.CouponDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CouponServiceImpl implements CouponService {
    private final CouponDAO couponDAO;

    public CouponServiceImpl(CouponDAO couponDAO) { this.couponDAO = couponDAO; }

    @Override
    public List<CouponDTO> findAll() { return couponDAO.findAll().stream().map(this::toDTO).toList(); }

    @Override
    public CouponDTO create(CouponDTO dto) {
        Coupon coupon = new Coupon();
        copy(dto, coupon);
        return toDTO(couponDAO.save(coupon));
    }

    @Override
    public CouponDTO update(String id, CouponDTO dto) {
        Coupon coupon = couponDAO.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
        copy(dto, coupon);
        return toDTO(couponDAO.save(coupon));
    }

    @Override
    public void delete(String id) { couponDAO.delete(id); }

    @Override
    public CouponPreviewDTO preview(String code, BigDecimal subtotal) {
        BigDecimal safeSubtotal = subtotal == null ? BigDecimal.ZERO : subtotal;
        try {
            Coupon coupon = findValidCoupon(code, safeSubtotal);
            if (coupon == null) {
                return new CouponPreviewDTO(code, false, "Coupon code is required", safeSubtotal, BigDecimal.ZERO, safeSubtotal);
            }
            BigDecimal discount = calculateDiscount(coupon, safeSubtotal);
            return new CouponPreviewDTO(coupon.getCode(), true, "Coupon applied", safeSubtotal, discount, safeSubtotal.subtract(discount));
        } catch (RuntimeException ex) {
            return new CouponPreviewDTO(code, false, ex.getMessage(), safeSubtotal, BigDecimal.ZERO, safeSubtotal);
        }
    }

    @Override
    public Coupon findValidCoupon(String code, BigDecimal subtotal) {
        if (code == null || code.isBlank()) return null;
        Coupon c = couponDAO.findByCode(code).orElseThrow(() -> new RuntimeException("Coupon not found"));
        LocalDateTime now = LocalDateTime.now();
        if (!c.isActive()) throw new RuntimeException("Coupon is inactive");
        if (c.getStartsAt() != null && now.isBefore(c.getStartsAt())) throw new RuntimeException("Coupon has not started yet");
        if (c.getExpiresAt() != null && now.isAfter(c.getExpiresAt())) throw new RuntimeException("Coupon expired");
        if (c.getUsageLimit() != null && c.getUsedCount() != null && c.getUsedCount() >= c.getUsageLimit()) throw new RuntimeException("Coupon usage limit reached");
        BigDecimal min = c.getMinOrderAmount() == null ? BigDecimal.ZERO : c.getMinOrderAmount();
        if (subtotal != null && subtotal.compareTo(min) < 0) throw new RuntimeException("Order total does not meet coupon minimum");
        return c;
    }

    @Override
    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon == null || subtotal == null) return BigDecimal.ZERO;
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) discount = coupon.getMaxDiscountAmount();
        if (discount.compareTo(subtotal) > 0) discount = subtotal;
        return discount.max(BigDecimal.ZERO);
    }

    private void copy(CouponDTO dto, Coupon c) {
        String code = dto.getCode() == null ? null : dto.getCode().trim().toUpperCase();
        c.setCode(code);
        c.setName(dto.getName());
        c.setDiscountType(dto.getDiscountType() == null ? DiscountType.PERCENTAGE : dto.getDiscountType());
        c.setDiscountValue(dto.getDiscountValue());
        c.setMinOrderAmount(dto.getMinOrderAmount() == null ? BigDecimal.ZERO : dto.getMinOrderAmount());
        c.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        c.setUsageLimit(dto.getUsageLimit());
        c.setUsedCount(dto.getUsedCount() == null ? 0 : dto.getUsedCount());
        c.setActive(dto.isActive());
        c.setStartsAt(dto.getStartsAt());
        c.setExpiresAt(dto.getExpiresAt());
    }

    private CouponDTO toDTO(Coupon c) {
        return new CouponDTO(c.getId(), c.getCode(), c.getName(), c.getDiscountType(), c.getDiscountValue(), c.getMinOrderAmount(), c.getMaxDiscountAmount(), c.getUsageLimit(), c.getUsedCount(), c.isActive(), c.getStartsAt(), c.getExpiresAt(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
