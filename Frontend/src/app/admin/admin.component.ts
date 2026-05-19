import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
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
export class AdminComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  stats: DashboardStatsDTO | null = null;
  revenue: DashboardChartDTO[] = [];
  statuses: DashboardChartDTO[] = [];
  topProducts: DashboardChartDTO[] = [];
  lowStock: any[] = [];
  loadingStats = false;

  constructor(private dashboardService: DashboardService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadingStats = true;
    this.dashboardService.getStats().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    this.dashboardService.revenue(14).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.revenue = x; this.cdr.detectChanges(); } });
    this.dashboardService.orderStatus().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.statuses = x; this.cdr.detectChanges(); } });
    this.dashboardService.topProducts(8).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.topProducts = x; this.cdr.detectChanges(); } });
    this.dashboardService.lowStock(5).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.lowStock = x; this.cdr.detectChanges(); } });
  }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}

