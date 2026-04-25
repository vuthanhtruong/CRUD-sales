import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type CommentStatus = 'PUBLISHED' | 'HIDDEN' | 'PENDING';

export interface ProductCommentDTO {
  id?: string;
  productId: string;
  productName?: string;
  parentId?: string | null;
  username?: string;
  customerName?: string;
  content: string;
  status?: CommentStatus;
  helpfulCount?: number;
  createdAt?: string;
  updatedAt?: string;
  replies?: ProductCommentDTO[];
}

@Injectable({ providedIn: 'root' })
export class ProductCommentService {
  private apiUrl = 'http://localhost:8080/api/comments';
  constructor(private http: HttpClient) {}

  productThread(productId: string): Observable<ProductCommentDTO[]> {
    return this.http.get<ProductCommentDTO[]>(`${this.apiUrl}/product/${productId}`);
  }

  mine(): Observable<ProductCommentDTO[]> {
    return this.http.get<ProductCommentDTO[]>(`${this.apiUrl}/me`);
  }

  create(dto: ProductCommentDTO): Observable<ProductCommentDTO> {
    return this.http.post<ProductCommentDTO>(this.apiUrl, dto);
  }

  helpful(id: string): Observable<ProductCommentDTO> {
    return this.http.post<ProductCommentDTO>(`${this.apiUrl}/${id}/helpful`, {});
  }

  adminFindAll(status?: CommentStatus | ''): Observable<ProductCommentDTO[]> {
    const options = status ? { params: { status } } : {};
    return this.http.get<ProductCommentDTO[]>(`${this.apiUrl}/admin`, options);
  }

  moderate(id: string, status: CommentStatus): Observable<ProductCommentDTO> {
    return this.http.put<ProductCommentDTO>(`${this.apiUrl}/admin/${id}/moderate`, { status });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
