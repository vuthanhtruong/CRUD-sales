import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductImageDTO } from './product-image.service';
import { ProductDTO } from './product.service';

export interface ProductUserDTO {
  productId: string;
  productName: string;
  price: number;
  image: string;
}

export interface SizeDTO {
  id: string;
  name: string;
}

export interface ColorDTO {
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductUserService {

  private apiUrl = 'http://localhost:8080/api/products/user';

  constructor(private http: HttpClient) {}

  getProducts(): Observable<ProductUserDTO[]> {
    return this.http.get<ProductUserDTO[]>(this.apiUrl);
  }

  getProductById(productId: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/${productId}`);
  }

  getImagesByProduct(productId: string): Observable<ProductImageDTO[]> {
    return this.http.get<ProductImageDTO[]>(`${this.apiUrl}/${productId}/images`);
  }

  getSizesByProduct(productId: string): Observable<SizeDTO[]> {
    return this.http.get<SizeDTO[]>(`${this.apiUrl}/${productId}/sizes`);
  }

  getColorsByProduct(productId: string): Observable<ColorDTO[]> {
    return this.http.get<ColorDTO[]>(`${this.apiUrl}/${productId}/colors`);
  }

  filterProducts(minPrice?: number, maxPrice?: number): Observable<ProductUserDTO[]> {

    let params: any = {};

    if (minPrice !== undefined && minPrice !== null) {
      params.minPrice = minPrice;
    }

    if (maxPrice !== undefined && maxPrice !== null) {
      params.maxPrice = maxPrice;
    }

    return this.http.get<ProductUserDTO[]>(`${this.apiUrl}/filter`, { params });
  }

  searchProducts(
    keyword?: string,
    minPrice?: number,
    maxPrice?: number,
    productTypeId?: string
  ): Observable<ProductUserDTO[]> {

    let params: any = {};

    if (keyword && keyword.trim() !== '') {
      params.keyword = keyword.trim();
    }

    if (minPrice !== undefined && minPrice !== null) {
      params.minPrice = minPrice;
    }

    if (maxPrice !== undefined && maxPrice !== null) {
      params.maxPrice = maxPrice;
    }

    if (productTypeId && productTypeId.trim() !== '') {
      params.productTypeId = productTypeId;
    }

    return this.http.get<ProductUserDTO[]>(
      `${this.apiUrl}/search`,
      { params }
    );
  }
}
