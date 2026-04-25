package com.example.demo.service;

import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.model.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDTO checkout(CheckoutRequestDTO request);
    List<OrderDTO> findMyOrders();
    OrderDTO findById(String id);
    List<OrderDTO> findAll(OrderStatus status);
    OrderDTO updateStatus(String id, OrderStatus status);
}
