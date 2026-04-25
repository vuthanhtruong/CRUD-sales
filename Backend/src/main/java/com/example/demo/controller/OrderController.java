package com.example.demo.controller;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.model.OrderStatus;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:4200")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(@RequestBody @Valid CheckoutRequestDTO request) {
        return ResponseEntity.ok(orderService.checkout(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderDTO>> myOrders() { return ResponseEntity.ok(orderService.findMyOrders()); }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> findById(@PathVariable String id) { return ResponseEntity.ok(orderService.findById(id)); }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAll(@RequestParam(required = false) OrderStatus status) {
        return ResponseEntity.ok(orderService.findAll(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String value = body.get("status");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Status is required");
        return ResponseEntity.ok(orderService.updateStatus(id, OrderStatus.valueOf(value)));
    }
}
