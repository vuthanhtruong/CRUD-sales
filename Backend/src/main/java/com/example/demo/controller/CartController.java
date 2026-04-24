package com.example.demo.controller;

import com.example.demo.dto.AddToCartRequestDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.service.CartItemService;
import com.example.demo.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    public CartController(CartItemService cartItemService, CartService cartService) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<CartItemDTO>> getMyCart() {

        List<CartItemDTO> items = cartItemService.findCurrentUserCartItems();

        return ResponseEntity.ok(items);
    }

    // ================= ADD TO CART =================
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartRequestDTO request) {

        cartItemService.create(request);

        return ResponseEntity.ok("Added to cart successfully");
    }

    // ================= GET CART ITEMS =================
    @GetMapping("/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable String cartId) {

        List<CartItemDTO> items = cartItemService.findByCartId(cartId);

        return ResponseEntity.ok(items);
    }

    // ================= GET SINGLE ITEM =================
    @GetMapping("/item/{id}")
    public ResponseEntity<CartItemDTO> getCartItem(@PathVariable String id) {

        CartItemDTO item = cartItemService.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        return ResponseEntity.ok(item);
    }

    // ================= UPDATE QUANTITY =================
    @PutMapping("/item")
    public ResponseEntity<String> updateCartItem(@RequestBody CartItemDTO dto) {

        cartItemService.update(dto);

        return ResponseEntity.ok("Cart item updated");
    }

    // ================= DELETE ITEM =================
    @DeleteMapping("/item/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable String id) {

        cartItemService.delete(id);

        return ResponseEntity.ok("Item deleted");
    }

    // ================= CLEAR CART =================
    @DeleteMapping("/clear/{cartId}")
    public ResponseEntity<String> clearCart(@PathVariable String cartId) {

        cartItemService.deleteByCartId(cartId);

        return ResponseEntity.ok("Cart cleared");
    }
}