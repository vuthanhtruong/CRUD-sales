import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductImageDTO } from './product-image.service';
import { PageResponse, ProductDTO } from './product.service';

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

  private buildSearchParams(
    keyword?: string,
    minPrice?: number,
    maxPrice?: number,
    productTypeId?: string,
  ): HttpParams {
    let params = new HttpParams();

    if (keyword && keyword.trim() !== '') params = params.set('keyword', keyword.trim());
    if (minPrice !== undefined && minPrice !== null) params = params.set('minPrice', minPrice.toString());
    if (maxPrice !== undefined && maxPrice !== null) params = params.set('maxPrice', maxPrice.toString());
    if (productTypeId && productTypeId.trim() !== '') params = params.set('productTypeId', productTypeId.trim());

    return params;
  }

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
    let params = new HttpParams();
    if (minPrice !== undefined && minPrice !== null) params = params.set('minPrice', minPrice.toString());
    if (maxPrice !== undefined && maxPrice !== null) params = params.set('maxPrice', maxPrice.toString());
    return this.http.get<ProductUserDTO[]>(`${this.apiUrl}/filter`, { params });
  }

  searchProducts(
    keyword?: string,
    minPrice?: number,
    maxPrice?: number,
    productTypeId?: string
  ): Observable<ProductUserDTO[]> {
    const params = this.buildSearchParams(keyword, minPrice, maxPrice, productTypeId);
    return this.http.get<ProductUserDTO[]>(`${this.apiUrl}/search`, { params });
  }

  searchProductsPage(
    keyword?: string,
    minPrice?: number,
    maxPrice?: number,
    productTypeId?: string,
    page = 1,
    pageSize = 8,
  ): Observable<PageResponse<ProductUserDTO>> {
    let params = this.buildSearchParams(keyword, minPrice, maxPrice, productTypeId)
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());

    return this.http.get<PageResponse<ProductUserDTO>>(`${this.apiUrl}/page`, { params });
  }
}
