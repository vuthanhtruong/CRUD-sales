package com.example.demo.repository;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.OrderItemDTO;
import com.example.demo.dto.OrderTimelineDTO;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.SalesOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    SalesOrder save(SalesOrder order);
    Optional<SalesOrder> findById(String id);
    Optional<OrderDTO> findByIdDTO(String id);
    List<SalesOrder> findByCurrentUser(String username);
    List<OrderDTO> findByCurrentUserDTO(String username);
    List<SalesOrder> findAll(OrderStatus status);
    List<OrderDTO> findAllDTO(OrderStatus status);
    List<OrderItemDTO> findItemsByOrderIdDTO(String orderId);
    List<OrderTimelineDTO> findTimelineByOrderIdDTO(String orderId);
    SalesOrder updateStatus(String id, OrderStatus status);
    long countAll();
    long countByStatus(OrderStatus status);
    BigDecimal sumRevenue();
}
