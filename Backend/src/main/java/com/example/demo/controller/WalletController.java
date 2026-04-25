package com.example.demo.controller;

import com.example.demo.dto.TopUpRequestDTO;
import com.example.demo.model.WalletTopUpStatus;
import com.example.demo.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "http://localhost:4200")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> mine() {
        return ResponseEntity.ok(walletService.mine());
    }

    @PostMapping("/top-up")
    public ResponseEntity<?> topUp(@RequestBody @Valid TopUpRequestDTO request) {
        return ResponseEntity.ok(walletService.requestTopUp(request));
    }

    @GetMapping("/admin/top-ups")
    public ResponseEntity<?> topUps(@RequestParam(required = false) WalletTopUpStatus status) {
        return ResponseEntity.ok(walletService.adminTopUps(status));
    }

    @PatchMapping("/admin/top-ups/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        return ResponseEntity.ok(walletService.approveTopUp(id));
    }

    @PatchMapping("/admin/top-ups/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        return ResponseEntity.ok(walletService.rejectTopUp(id));
    }
}
