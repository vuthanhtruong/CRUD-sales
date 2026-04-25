import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type WalletTransactionType = 'TOP_UP' | 'PAYMENT' | 'REFUND' | 'ADJUSTMENT';
export type WalletTopUpStatus = 'NONE' | 'PENDING' | 'APPROVED' | 'REJECTED';

export interface WalletTransactionDTO {
  id: string;
  type: WalletTransactionType;
  topUpStatus: WalletTopUpStatus;
  amount: number;
  balanceAfter: number;
  referenceId?: string;
  description?: string;
  createdAt?: string;
  approvedAt?: string;
  username?: string;
  customerName?: string;
}

export interface WalletDTO {
  walletId: string;
  balance: number;
  updatedAt?: string;
  transactions: WalletTransactionDTO[];
}

@Injectable({ providedIn: 'root' })
export class WalletService {
  private apiUrl = 'http://localhost:8080/api/wallet';
  constructor(private http: HttpClient) {}

  mine(): Observable<WalletDTO> {
    return this.http.get<WalletDTO>(`${this.apiUrl}/me`);
  }

  topUp(amount: number, note?: string): Observable<WalletTransactionDTO> {
    return this.http.post<WalletTransactionDTO>(`${this.apiUrl}/top-up`, { amount, note });
  }

  adminTopUps(status: WalletTopUpStatus = 'PENDING'): Observable<WalletTransactionDTO[]> {
    return this.http.get<WalletTransactionDTO[]>(`${this.apiUrl}/admin/top-ups`, {
      params: new HttpParams().set('status', status),
    });
  }

  approve(id: string): Observable<WalletTransactionDTO> {
    return this.http.patch<WalletTransactionDTO>(`${this.apiUrl}/admin/top-ups/${id}/approve`, {});
  }

  reject(id: string): Observable<WalletTransactionDTO> {
    return this.http.patch<WalletTransactionDTO>(`${this.apiUrl}/admin/top-ups/${id}/reject`, {});
  }
}
