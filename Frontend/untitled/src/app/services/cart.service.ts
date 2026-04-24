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

@Injectable({ providedIn: 'root' })
export class CartService {
  private baseUrl = 'http://localhost:8080/api/cart';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : '',
      }),
    };
  }

  // responseType: 'text' để đọc đúng error message string từ backend
  private getTextOptions() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : '',
      }),
      responseType: 'text' as const,
    };
  }

  // ================= ADD TO CART =================
  addToCart(request: AddToCartRequestDTO): Observable<string> {
    return this.http.post(`${this.baseUrl}/add`, request, this.getTextOptions());
  }

  // ================= GET MY CART =================
  getMyCart(): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/me`, this.getAuthHeaders());
  }

  // ================= GET CART ITEMS BY ID =================
  getCartItems(cartId: string): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/${cartId}`, this.getAuthHeaders());
  }

  // ================= INCREASE QUANTITY =================
  increaseQuantity(cartItemId: string, amount: number = 1): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/item/increase/${cartItemId}?amount=${amount}`,
      {},
      this.getTextOptions(),
    );
  }

  // ================= DECREASE QUANTITY =================
  decreaseQuantity(cartItemId: string, amount: number = 1): Observable<string> {
    return this.http.put(
      `${this.baseUrl}/item/decrease/${cartItemId}?amount=${amount}`,
      {},
      this.getTextOptions(),
    );
  }

  // ================= UPDATE ITEM =================
  updateCartItem(item: CartItemDTO): Observable<string> {
    return this.http.put(`${this.baseUrl}/item`, item, this.getTextOptions());
  }

  // ================= DELETE ITEM =================
  deleteCartItem(id: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/item/${id}`, this.getTextOptions());
  }

  // ================= CLEAR CART =================
  clearCart(cartId: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/clear/${cartId}`, this.getTextOptions());
  }

  // ================= CHECKOUT =================
  checkout(cartItemIds: string[]): Observable<string> {
    return this.http.post(`${this.baseUrl}/checkout`, cartItemIds, this.getTextOptions());
  }
}
