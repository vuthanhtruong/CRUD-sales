// cart.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheckoutRequestDTO, OrderDTO } from './order.service';

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
  private orderUrl = 'http://localhost:8080/api/orders';

  constructor(private http: HttpClient) {}

  addToCart(request: AddToCartRequestDTO): Observable<string> {
    return this.http.post(`${this.baseUrl}/add`, request, { responseType: 'text' });
  }

  getMyCart(): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/me`);
  }

  getCartItems(cartId: string): Observable<CartItemDTO[]> {
    return this.http.get<CartItemDTO[]>(`${this.baseUrl}/${cartId}`);
  }

  increaseQuantity(cartItemId: string, amount: number = 1): Observable<string> {
    return this.http.put(`${this.baseUrl}/item/increase/${cartItemId}?amount=${amount}`, {}, { responseType: 'text' });
  }

  decreaseQuantity(cartItemId: string, amount: number = 1): Observable<string> {
    return this.http.put(`${this.baseUrl}/item/decrease/${cartItemId}?amount=${amount}`, {}, { responseType: 'text' });
  }

  updateCartItem(item: CartItemDTO): Observable<string> {
    return this.http.put(`${this.baseUrl}/item`, item, { responseType: 'text' });
  }

  deleteCartItem(id: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/item/${id}`, { responseType: 'text' });
  }

  clearCart(cartId: string): Observable<string> {
    return this.http.delete(`${this.baseUrl}/clear/${cartId}`, { responseType: 'text' });
  }

  checkout(request: CheckoutRequestDTO): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(`${this.orderUrl}/checkout`, request);
  }
}
