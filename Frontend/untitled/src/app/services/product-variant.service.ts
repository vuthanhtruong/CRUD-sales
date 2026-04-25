import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Size} from './size.service';
import {ColorDTO} from './color.service';

export interface ProductVariantDTO {
  productId: string;
  sizeId: string;
  colorId: string;

  quantity?: number;

  productName?: string;
  sizeName?: string;
  colorName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductVariantService {

  private apiUrl = 'http://localhost:8080/api/variants';

  constructor(private http: HttpClient) {}

  // =========================
  // AUTH HEADER
  // =========================
  private getAuthHeaders() {
    const token = localStorage.getItem('token');

    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }


  // =========================
// CHECK PRODUCT AVAILABLE STOCK
// =========================
  existsAvailableStockByProductId(productId: string): Observable<boolean> {
    return this.http.get<boolean>(
      `${this.apiUrl}/product/${productId}/available-stock`,
      this.getAuthHeaders()
    );
  }

// =========================
// GET TOTAL PRODUCT QUANTITY
// =========================
  getTotalQuantityByProductId(productId: string): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/product/${productId}/total-quantity`,
      this.getAuthHeaders()
    );
  }

  getQuantity(
    productId: string,
    sizeId: string,
    colorId: string
  ): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/quantity?productId=${productId}&sizeId=${sizeId}&colorId=${colorId}`,
      this.getAuthHeaders()
    );
  }

  decreaseStock(
    productId: string,
    sizeId: string,
    colorId: string,
    amount: number
  ): Observable<string> {
    return this.http.patch<string>(
      `${this.apiUrl}/decrease?productId=${productId}&sizeId=${sizeId}&colorId=${colorId}&amount=${amount}`,
      {}, // body rỗng vì dùng request param
      this.getAuthHeaders()
    );
  }

  // =========================
  // GET ALL
  // =========================
  findAll(): Observable<ProductVariantDTO[]> {
    return this.http.get<ProductVariantDTO[]>(
      this.apiUrl,
      this.getAuthHeaders()
    );
  }

  // =========================
  // GET BY COMPOSITE ID
  // =========================
  findById(
    productId: string,
    sizeId: string,
    colorId: string
  ): Observable<ProductVariantDTO> {
    return this.http.get<ProductVariantDTO>(
      `${this.apiUrl}/${productId}/${sizeId}/${colorId}`,
      this.getAuthHeaders()
    );
  }

  // =========================
  // GET BY PRODUCT
  // =========================
  findByProduct(productId: string): Observable<ProductVariantDTO[]> {
    return this.http.get<ProductVariantDTO[]>(
      `${this.apiUrl}/product/${productId}`,
      this.getAuthHeaders()
    );
  }

  // =========================
  // CREATE SINGLE
  // =========================
  create(data: ProductVariantDTO): Observable<ProductVariantDTO> {
    return this.http.post<ProductVariantDTO>(
      this.apiUrl,
      data,
      this.getAuthHeaders()
    );
  }

  // =========================
  // CREATE BATCH
  // =========================
  createBatch(data: ProductVariantDTO[]): Observable<ProductVariantDTO[]> {
    return this.http.post<ProductVariantDTO[]>(
      `${this.apiUrl}/batch`,
      data,
      this.getAuthHeaders()
    );
  }

  // =========================
  // UPDATE
  // =========================
  update(data: ProductVariantDTO): Observable<ProductVariantDTO> {
    return this.http.put<ProductVariantDTO>(
      this.apiUrl,
      data,
      this.getAuthHeaders()
    );
  }

  findUnusedSizes(productId: string): Observable<Size[]> {
    return this.http.get<Size[]>(
      `${this.apiUrl}/sizes/unused/${productId}`,
      this.getAuthHeaders()
    );
  }

  findUnusedColors(productId: string): Observable<ColorDTO[]> {
    return this.http.get<ColorDTO[]>(
      `${this.apiUrl}/colors/unused/${productId}`,
      this.getAuthHeaders()
    );
  }

  // =========================
  // DELETE
  // =========================
  delete(
    productId: string,
    sizeId: string,
    colorId: string
  ): Observable<any> {
    return this.http.delete(
      `${this.apiUrl}/${productId}/${sizeId}/${colorId}`,
      this.getAuthHeaders()
    );
  }
}
