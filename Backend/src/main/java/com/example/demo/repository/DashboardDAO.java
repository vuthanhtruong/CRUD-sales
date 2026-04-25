package com.example.demo.repository;

import com.example.demo.dto.DashboardChartDTO;
import com.example.demo.dto.ProductVariantDTO;

import java.util.List;

public interface DashboardDAO {
    long countUsers();
    long countLowStockVariants(int threshold);
    List<DashboardChartDTO> revenueLastDays(int days);
    List<DashboardChartDTO> orderStatusDistribution();
    List<DashboardChartDTO> topSellingProducts(int limit);
    List<ProductVariantDTO> lowStockVariants(int threshold);
}
