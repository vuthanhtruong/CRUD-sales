import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WishlistItemDTO {
  id: string;
  productId: string;
  productName: string;
  price: number;
  image?: string | null;
  createdAt: string;
}

export interface WishlistStatusDTO {
  liked: boolean;
  count: number;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private apiUrl = 'http://localhost:8080/api/wishlist';
  constructor(private http: HttpClient) {}

  getMine(): Observable<WishlistItemDTO[]> {
    return this.http.get<WishlistItemDTO[]>(this.apiUrl);
  }

  add(productId: string): Observable<WishlistItemDTO> {
    return this.http.post<WishlistItemDTO>(`${this.apiUrl}/${productId}`, {});
  }

  remove(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`);
  }

  status(productId: string): Observable<WishlistStatusDTO> {
    return this.http.get<WishlistStatusDTO>(`${this.apiUrl}/${productId}/status`);
  }
}
