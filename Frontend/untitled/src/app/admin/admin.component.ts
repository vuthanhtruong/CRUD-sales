import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardChartDTO, DashboardService, DashboardStatsDTO } from '../services/dashboard.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  stats: DashboardStatsDTO | null = null;
  revenue: DashboardChartDTO[] = [];
  statuses: DashboardChartDTO[] = [];
  topProducts: DashboardChartDTO[] = [];
  lowStock: any[] = [];
  loadingStats = false;

  constructor(private dashboardService: DashboardService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadingStats = true;
    this.dashboardService.getStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.loadingStats = false;
        this.cdr.detectChanges();
        this.loadCharts();
      },
      error: () => {
        this.loadingStats = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadCharts() {
    this.dashboardService.revenue(14).subscribe({ next: (x) => { this.revenue = x; this.cdr.detectChanges(); } });
    this.dashboardService.orderStatus().subscribe({ next: (x) => { this.statuses = x; this.cdr.detectChanges(); } });
    this.dashboardService.topProducts(8).subscribe({ next: (x) => { this.topProducts = x; this.cdr.detectChanges(); } });
    this.dashboardService.lowStock(5).subscribe({ next: (x) => { this.lowStock = x; this.cdr.detectChanges(); } });
  }
}

