import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
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
  private apiUrl = '/api/products';

  constructor(private http: HttpClient) {}

  private appendSearchParams(httpParams: HttpParams, params: ProductSearchParams): HttpParams {
    if (params.keyword?.trim()) httpParams = httpParams.set('keyword', params.keyword.trim());
    if (params.minPrice != null) httpParams = httpParams.set('minPrice', params.minPrice.toString());
    if (params.maxPrice != null) httpParams = httpParams.set('maxPrice', params.maxPrice.toString());
    if (params.productTypeId?.trim()) httpParams = httpParams.set('productTypeId', params.productTypeId.trim());
    if (params.status?.trim()) httpParams = httpParams.set('status', params.status.trim());
    return httpParams;
  }

  findAll(): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(this.apiUrl);
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

    return this.http.get<PageResponse<ProductDTO>>(`${this.apiUrl}/page`, { params: httpParams });
  }

  findAllPaged(page: number, pageSize: number): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(`${this.apiUrl}/paged?page=${page}&pageSize=${pageSize}`);
  }

  existsByProductType(productTypeId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-type/${productTypeId}`);
  }

  countTotalPages(pageSize: number): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/total-pages?pageSize=${pageSize}`);
  }

  create(product: ProductDTO): Observable<any> {
    return this.http.post(this.apiUrl, product);
  }

  edit(id: string, product: ProductDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, product);
  }

  delete(productId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${productId}`);
  }

  getStatuses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/statuses`);
  }

  findById(id: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/${id}`);
  }

  existsByColor(colorId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-color/${colorId}`);
  }

  existsBySize(sizeId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/exists-by-size/${sizeId}`);
  }

  searchProducts(params: ProductSearchParams): Observable<ProductDTO[]> {
    let httpParams = new HttpParams();
    httpParams = this.appendSearchParams(httpParams, params);

    return this.http.get<ProductDTO[]>(`${this.apiUrl}/search`, { params: httpParams });
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
      params: httpParams,
      observe: 'response',
      responseType: 'blob',
    });
  }
}
