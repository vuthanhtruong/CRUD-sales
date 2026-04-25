import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type DiscountType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface CouponDTO {
  id?: string;
  code: string;
  name: string;
  discountType: DiscountType;
  discountValue: number;
  minOrderAmount?: number;
  maxDiscountAmount?: number;
  usageLimit?: number;
  usedCount?: number;
  active: boolean;
  startsAt?: string;
  expiresAt?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CouponPreviewDTO {
  code: string;
  valid: boolean;
  message: string;
  subtotal: number;
  discountAmount: number;
  finalAmount: number;
}

@Injectable({ providedIn: 'root' })
export class CouponService {
  private apiUrl = 'http://localhost:8080/api/coupons';
  constructor(private http: HttpClient) {}

  findAll(): Observable<CouponDTO[]> {
    return this.http.get<CouponDTO[]>(`${this.apiUrl}/admin`);
  }

  create(dto: CouponDTO): Observable<CouponDTO> {
    return this.http.post<CouponDTO>(`${this.apiUrl}/admin`, dto);
  }

  update(id: string, dto: CouponDTO): Observable<CouponDTO> {
    return this.http.put<CouponDTO>(`${this.apiUrl}/admin/${id}`, dto);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/${id}`);
  }

  preview(code: string, subtotal: number): Observable<CouponPreviewDTO> {
    return this.http.get<CouponPreviewDTO>(`${this.apiUrl}/preview`, { params: { code, subtotal } as any });
  }
}
