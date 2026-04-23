import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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
  images?: ProductImageDTO[];
}

export interface ProductSearchParams {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  productTypeId?: string;
  status?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductService {

  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders({ Authorization: token ? `Bearer ${token}` : '' });
  }

  findAll(): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(this.apiUrl, { headers: this.getAuthHeaders() });
  }

  findAllPaged(page: number, pageSize: number): Observable<ProductDTO[]> {
    return this.http.get<ProductDTO[]>(
      `${this.apiUrl}/paged?page=${page}&pageSize=${pageSize}`,
      { headers: this.getAuthHeaders() }
    );
  }

  countTotalPages(pageSize: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/total-pages?pageSize=${pageSize}`,
      { headers: this.getAuthHeaders() }
    );
  }

  create(product: ProductDTO): Observable<any> {
    return this.http.post(this.apiUrl, product, { headers: this.getAuthHeaders() });
  }

  edit(id: string, product: ProductDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, product, { headers: this.getAuthHeaders() });
  }

  delete(productId: string): Observable<any> {
    return this.http.delete(this.apiUrl, {
      headers: this.getAuthHeaders(),
      body: { productId }
    });
  }

  getStatuses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/statuses`, { headers: this.getAuthHeaders() });
  }

  findById(id: string): Observable<ProductDTO> {
    return this.http.get<ProductDTO>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() });
  }

  searchProducts(params: ProductSearchParams): Observable<ProductDTO[]> {
    let httpParams = new HttpParams();
    if (params.keyword?.trim())       httpParams = httpParams.set('keyword', params.keyword.trim());
    if (params.minPrice != null)      httpParams = httpParams.set('minPrice', params.minPrice.toString());
    if (params.maxPrice != null)      httpParams = httpParams.set('maxPrice', params.maxPrice.toString());
    if (params.productTypeId?.trim()) httpParams = httpParams.set('productTypeId', params.productTypeId.trim());
    if (params.status?.trim())        httpParams = httpParams.set('status', params.status.trim());

    return this.http.get<ProductDTO[]>(`${this.apiUrl}/search`, {
      headers: this.getAuthHeaders(),
      params: httpParams
    });
  }
}
