package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalProducts;
    private long totalUsers;
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private BigDecimal revenue;
    private long lowStockVariants;
}
