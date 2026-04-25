import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardChartDTO {
  label: string;
  value: number;
  count: number;
}

export interface DashboardStatsDTO {
  totalProducts: number;
  totalUsers: number;
  totalOrders: number;
  pendingOrders: number;
  completedOrders: number;
  revenue: number;
  lowStockVariants: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/dashboard';
  constructor(private http: HttpClient) {}
  getStats(): Observable<DashboardStatsDTO> {
    return this.http.get<DashboardStatsDTO>(`${this.apiUrl}/stats`);
  }

  revenue(days = 14): Observable<DashboardChartDTO[]> {
    return this.http.get<DashboardChartDTO[]>(`${this.apiUrl}/revenue`, { params: { days } as any });
  }

  orderStatus(): Observable<DashboardChartDTO[]> {
    return this.http.get<DashboardChartDTO[]>(`${this.apiUrl}/order-status`);
  }

  topProducts(limit = 8): Observable<DashboardChartDTO[]> {
    return this.http.get<DashboardChartDTO[]>(`${this.apiUrl}/top-products`, { params: { limit } as any });
  }

  lowStock(threshold = 5): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/low-stock`, { params: { threshold } as any });
  }
}
