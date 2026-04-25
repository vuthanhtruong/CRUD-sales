import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductMetricDTO {
  productId: string;
  productName: string;
  price: number;
  image?: string;
  viewCount: number;
  lastViewedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductInsightService {
  private apiUrl = 'http://localhost:8080/api/insights';
  constructor(private http: HttpClient) {}

  recordView(productId: string): Observable<ProductMetricDTO> {
    return this.http.post<ProductMetricDTO>(`${this.apiUrl}/products/${productId}/view`, {});
  }

  trending(limit = 8): Observable<ProductMetricDTO[]> {
    return this.http.get<ProductMetricDTO[]>(`${this.apiUrl}/trending`, { params: { limit } });
  }
}
