import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductImageDTO {
  id?: string;
  imageData?: string;
  contentType?: string;
  isPrimary?: boolean;
  productId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductImageService {

  private apiUrl = 'http://localhost:8080/api/product-images';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }

  findByProductId(productId: string): Observable<ProductImageDTO[]> {
    return this.http.get<ProductImageDTO[]>(
      `${this.apiUrl}/product/${productId}`,
      this.getAuthHeaders()
    );
  }

  create(dto: ProductImageDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}`, dto, this.getAuthHeaders());
  }

  deleteBatch(ids: string[]): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.delete(`${this.apiUrl}/batch`, {
      headers: new HttpHeaders({ Authorization: token ? `Bearer ${token}` : '' }),
      body: ids
    });
  }
}
