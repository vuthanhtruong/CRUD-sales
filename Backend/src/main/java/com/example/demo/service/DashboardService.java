package com.example.demo.service;

import com.example.demo.dto.DashboardChartDTO;
import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.ProductVariantDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getStats();
    List<DashboardChartDTO> revenueLastDays(int days);
    List<DashboardChartDTO> orderStatusDistribution();
    List<DashboardChartDTO> topSellingProducts(int limit);
    List<ProductVariantDTO> lowStockVariants(int threshold);
}
