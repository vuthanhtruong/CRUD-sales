import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductImageDTO {
  id?: string;
  imageData?: string;
  contentType?: string;
  isPrimary?: boolean;
  productId?: string;
}

@Injectable({ providedIn: 'root' })
export class ProductImageService {
  private apiUrl = '/api/product-images';

  constructor(private http: HttpClient) {}

  findByProductId(productId: string): Observable<ProductImageDTO[]> {
    return this.http.get<ProductImageDTO[]>(`${this.apiUrl}/product/${productId}`);
  }

  create(dto: ProductImageDTO): Observable<any> {
    return this.http.post(this.apiUrl, dto);
  }

  setPrimary(id: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/primary`, {});
  }

  deleteBatch(ids: string[]): Observable<any> {
    return this.http.delete(`${this.apiUrl}/batch`, { body: ids });
  }
}
