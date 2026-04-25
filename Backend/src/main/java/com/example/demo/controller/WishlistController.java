package com.example.demo.controller;

import com.example.demo.dto.WishlistItemDTO;
import com.example.demo.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "http://localhost:4200")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<WishlistItemDTO>> findMine() { return ResponseEntity.ok(wishlistService.findMine()); }

    @GetMapping("/{productId}/status")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String productId) { return ResponseEntity.ok(wishlistService.status(productId)); }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistItemDTO> add(@PathVariable String productId) { return ResponseEntity.ok(wishlistService.add(productId)); }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> remove(@PathVariable String productId) { wishlistService.remove(productId); return ResponseEntity.noContent().build(); }
}
