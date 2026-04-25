package com.example.demo.service;

import com.example.demo.dto.DashboardChartDTO;
import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.DashboardDAO;
import com.example.demo.repository.OrderDAO;
import com.example.demo.repository.ProductDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final DashboardDAO dashboardDAO;

    public DashboardServiceImpl(ProductDAO productDAO, OrderDAO orderDAO, DashboardDAO dashboardDAO) {
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.dashboardDAO = dashboardDAO;
    }

    @Override
    public DashboardStatsDTO getStats() {
        return new DashboardStatsDTO(
                productDAO.countProducts(),
                dashboardDAO.countUsers(),
                orderDAO.countAll(),
                orderDAO.countByStatus(OrderStatus.PENDING),
                orderDAO.countByStatus(OrderStatus.COMPLETED),
                orderDAO.sumRevenue(),
                dashboardDAO.countLowStockVariants(5)
        );
    }

    @Override
    public List<DashboardChartDTO> revenueLastDays(int days) { return dashboardDAO.revenueLastDays(days); }

    @Override
    public List<DashboardChartDTO> orderStatusDistribution() { return dashboardDAO.orderStatusDistribution(); }

    @Override
    public List<DashboardChartDTO> topSellingProducts(int limit) { return dashboardDAO.topSellingProducts(limit); }

    @Override
    public List<ProductVariantDTO> lowStockVariants(int threshold) { return dashboardDAO.lowStockVariants(threshold); }
}
