package com.example.demo.controller;

import com.example.demo.dto.DashboardChartDTO;
import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {
    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() { return ResponseEntity.ok(dashboardService.getStats()); }
    @GetMapping("/revenue")
    public ResponseEntity<List<DashboardChartDTO>> revenue(@RequestParam(defaultValue = "14") int days) { return ResponseEntity.ok(dashboardService.revenueLastDays(days)); }
    @GetMapping("/order-status")
    public ResponseEntity<List<DashboardChartDTO>> status() { return ResponseEntity.ok(dashboardService.orderStatusDistribution()); }
    @GetMapping("/top-products")
    public ResponseEntity<List<DashboardChartDTO>> topProducts(@RequestParam(defaultValue = "8") int limit) { return ResponseEntity.ok(dashboardService.topSellingProducts(limit)); }
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductVariantDTO>> lowStock(@RequestParam(defaultValue = "5") int threshold) { return ResponseEntity.ok(dashboardService.lowStockVariants(threshold)); }
}
