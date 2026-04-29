import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProductImageDTO } from './product-image.service';

export interface ProductDTO {
  productId: string;
  productName: string;
  status: string;
  productTypeId: string;
  createdBy?: string;
  price?: number;
  description?: string;
  image?: string;
  images?: ProductImageDTO[];
}

export interface ProductSearchParams {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  productTypeId?: string;
  status?: string;
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  hasPrevious: boolean;
  hasNext: boolean;
}

export type ProductExportFormat = 'excel' | 'word' | 'pdf';
export type ProductExportScope = 'current' | 'selected' | 'all';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: token ? `Bearer ${token}` : '' });
  }

  private appendSearchParams(httpParams: HttpParams, params: ProductSearchParams): HttpParams {
    if (params.keyword?.trim()) httpParams = httpParams.set('keyword', params.keyword.trim());
    if (params.minPrice != null) httpParams = httpParams.set('minPrice', params.minPrice.toString());
    if (params.maxPrice != null) httpParams = httpParams.set('maxPrice', params.maxPrice.toString());
    if (params.productTypeId?.trim()) httpParams = httpParams.set('productTypeId', params.productTypeId.trim());
    if (params.status?.trim()) httpParams = httpParams.set('status', params.status.trim());
    return httpParams;
  }

  findAll(): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  findProductsPage(
    params: ProductSearchParams,
    page: number,
    pageSize: number,
  ): Observable<PageResponse<ProductDTO>> {
    let httpParams = new HttpParams()
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());
    httpParams = this.appendSearchParams(httpParams, params);

    return this.http.get<PageResponse<ProductDTO>>(`${this.apiUrl}/page`, {
      headers: this.getAuthHeaders(),
      params: httpParams,
    });
  }

  findAllPaged(page: number, pageSize: number): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(`${this.apiUrl}/paged?page=${page}&pageSize=${pageSize}`, {
      headers: this.getAuthHeaders(),
    });
  }

  existsByProductType(productTypeId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-type/${productTypeId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  countTotalPages(pageSize: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-pages?pageSize=${pageSize}`, {
      headers: this.getAuthHeaders(),
    });
  }

  create(product: ProductDTO): Observable<any> {
    return this.http.post(this.apiUrl, product, { headers: this.getAuthHeaders() });
  }

  edit(id: string, product: ProductDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, product, { headers: this.getAuthHeaders() });
  }

  delete(productId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${productId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  getStatuses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/statuses`, { headers: this.getAuthHeaders() });
  }

  findById(id: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  existsByColor(colorId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-color/${colorId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  existsBySize(sizeId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-size/${sizeId}`, {
      headers: this.getAuthHeaders(),
    });
  }

  searchProducts(params: ProductSearchParams): Observable<ProductDTO[]> {
    let httpParams = new HttpParams();
    httpParams = this.appendSearchParams(httpParams, params);

    return this.http.get<ProductDTO[]>(`${this.apiUrl}/search`, {
      headers: this.getAuthHeaders(),
      params: httpParams,
    });
  }

  exportProducts(
    format: ProductExportFormat,
    scope: ProductExportScope,
    searchParams: ProductSearchParams,
    page: number,
    pageSize: number,
    pages: number[] = [],
  ): Observable<HttpResponse<Blob>> {
    let httpParams = new HttpParams()
      .set('format', format)
      .set('scope', scope)
      .set('page', page.toString())
      .set('pageSize', pageSize.toString());

    if (scope === 'selected' && pages.length > 0) {
      httpParams = httpParams.set('pages', pages.join(','));
    }

    httpParams = this.appendSearchParams(httpParams, searchParams);

    return this.http.get(`${this.apiUrl}/export`, {
      headers: this.getAuthHeaders(),
      params: httpParams,
      observe: 'response',
      responseType: 'blob',
    });
  }
}
