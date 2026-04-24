// cart.service.ts

import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AddToCartRequestDTO {
  productId: string;
  sizeId: string;
  colorId: string;
  quantity: number;
}

export interface CartItemDTO {
  cartItemId: string;
  productId: string;
  productName: string;
  sizeName: string;
  colorName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private baseUrl = 'http://localhost:8080/api/cart';

  constructor(private http: HttpClient) {}

  // ================= AUTH HEADER =================
  private getAuthHeaders() {
    const token = localStorage.getItem('token');

    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : '',
      }),
    };
  }

  // ================= ADD TO CART =================
  addToCart(request: AddToCartRequestDTO): Observable<string> {
    return this.http.post<string>(`${this.baseUrl}/add`, request, this.getAuthHeaders());
  }

  // ================= GET CART ITEMS =================
  getCartItems(cartId: string): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/${cartId}`, this.getAuthHeaders());
  }

  // ================= GET SINGLE ITEM =================
  getCartItem(id: string): Observable<CartItemDTO> {
    return this.http.get<CartItemDTO>(`${this.baseUrl}/item/${id}`, this.getAuthHeaders());
  }

  // ================= UPDATE ITEM =================
  updateCartItem(item: CartItemDTO): Observable<string> {
    return this.http.put<string>(`${this.baseUrl}/item`, item, this.getAuthHeaders());
  }

  // ================= DELETE ITEM =================
  deleteCartItem(id: string): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/item/${id}`, this.getAuthHeaders());
  }

  // ================= CLEAR CART =================
  clearCart(cartId: string): Observable<string> {
    return this.http.delete<string>(`${this.baseUrl}/clear/${cartId}`, this.getAuthHeaders());
  }

  getMyCart(): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/me`, this.getAuthHeaders());
  }
}
