package com.example.demo.repository;

import com.example.demo.model.OrderStatus;
import com.example.demo.model.SalesOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderDAO {
    SalesOrder save(SalesOrder order);
    Optional<SalesOrder> findById(String id);
    List<SalesOrder> findByCurrentUser(String username);
    List<SalesOrder> findAll(OrderStatus status);
    SalesOrder updateStatus(String id, OrderStatus status);
    long countAll();
    long countByStatus(OrderStatus status);
    BigDecimal sumRevenue();
}
