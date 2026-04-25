package com.example.demo.controller;

import com.example.demo.dto.CouponDTO;
import com.example.demo.dto.CouponPreviewDTO;
import com.example.demo.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = "http://localhost:4200")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) { this.couponService = couponService; }

    @GetMapping("/admin")
    public ResponseEntity<List<CouponDTO>> findAll() { return ResponseEntity.ok(couponService.findAll()); }

    @PostMapping("/admin")
    public ResponseEntity<CouponDTO> create(@RequestBody @Valid CouponDTO dto) { return ResponseEntity.ok(couponService.create(dto)); }

    @PutMapping("/admin/{id}")
    public ResponseEntity<CouponDTO> update(@PathVariable String id, @RequestBody @Valid CouponDTO dto) { return ResponseEntity.ok(couponService.update(id, dto)); }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) { couponService.delete(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/preview")
    public ResponseEntity<CouponPreviewDTO> preview(@RequestParam String code, @RequestParam BigDecimal subtotal) { return ResponseEntity.ok(couponService.preview(code, subtotal)); }
}
