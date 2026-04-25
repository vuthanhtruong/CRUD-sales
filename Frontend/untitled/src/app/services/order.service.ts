import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPING' | 'COMPLETED' | 'CANCELLED';
export type PaymentMethod = 'COD' | 'BANK_TRANSFER' | 'CARD';

export interface CheckoutRequestDTO {
  cartItemIds: string[];
  receiverName: string;
  receiverPhone: string;
  shippingAddress: string;
  note?: string;
  couponCode?: string;
  paymentMethod: PaymentMethod;
}

export interface OrderItemDTO {
  id: string;
  productId: string;
  productName: string;
  sizeId: string;
  sizeName: string;
  colorId: string;
  colorName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderTimelineDTO {
  id: string;
  status: OrderStatus;
  note: string;
  createdAt: string;
}

export interface OrderDTO {
  id: string;
  username?: string;
  customerName?: string;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  subtotalAmount: number;
  discountAmount: number;
  totalAmount: number;
  couponCode?: string;
  receiverName: string;
  receiverPhone: string;
  shippingAddress: string;
  note?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItemDTO[];
  timeline?: OrderTimelineDTO[];
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  private apiUrl = 'http://localhost:8080/api/orders';
  constructor(private http: HttpClient) {}

  checkout(request: CheckoutRequestDTO): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(`${this.apiUrl}/checkout`, request);
  }

  getMyOrders(): Observable<OrderDTO[]> {
    return this.http.get<OrderDTO[]>(`${this.apiUrl}/me`);
  }

  getById(orderId: string): Observable<OrderDTO> {
    return this.http.get<OrderDTO>(`${this.apiUrl}/${orderId}`);
  }

  getAllOrders(status?: OrderStatus | ''): Observable<OrderDTO[]> {
    const options = status ? { params: { status } } : {};
    return this.http.get<OrderDTO[]>(this.apiUrl, options);
  }

  updateStatus(orderId: string, status: OrderStatus): Observable<OrderDTO> {
    return this.http.put<OrderDTO>(`${this.apiUrl}/${orderId}/status`, { status });
  }
}
