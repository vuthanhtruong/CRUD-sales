// CartController.java - FULL FILE
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
@CrossOrigin(origins = "http://localhost:4200")
public class CartController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    public CartController(CartItemService cartItemService, CartService cartService) {
        this.cartItemService = cartItemService;
        this.cartService = cartService;
    }

    // ================= INCREASE QUANTITY (check kho) =================
    @PutMapping("/item/increase/{id}")
    public ResponseEntity<String> increaseQuantity(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int amount
    ) {
        try {
            cartItemService.increaseQuantity(id, amount);
            return ResponseEntity.ok("Quantity increased");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ================= DECREASE QUANTITY =================
    @PutMapping("/item/decrease/{id}")
    public ResponseEntity<String> decreaseQuantity(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int amount
    ) {
        try {
            cartItemService.decreaseQuantity(id, amount);
            return ResponseEntity.ok("Quantity decreased");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ================= CHECKOUT =================
    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestBody List<String> cartItemIds) {
        try {
            cartItemService.checkout(cartItemIds);
            return ResponseEntity.ok("Thanh toán thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ================= GET MY CART =================
    @GetMapping("/me")
    public ResponseEntity<List<CartItemDTO>> getMyCart() {
        return ResponseEntity.ok(cartItemService.findCurrentUserCartItems());
    }

    // ================= ADD TO CART =================
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody AddToCartRequestDTO request) {
        try {
            cartItemService.create(request);
            return ResponseEntity.ok("Added to cart successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ================= GET CART ITEMS =================
    @GetMapping("/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable String cartId) {
        return ResponseEntity.ok(cartItemService.findByCartId(cartId));
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