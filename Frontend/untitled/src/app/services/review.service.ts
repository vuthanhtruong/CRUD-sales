import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ReviewStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ProductReviewDTO {
  id?: string;
  productId: string;
  productName?: string;
  username?: string;
  customerName?: string;
  orderId?: string;
  rating: number;
  title?: string;
  comment: string;
  status?: ReviewStatus;
  adminReply?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ReviewSummaryDTO {
  productId: string;
  averageRating: number;
  totalReviews: number;
  fiveStars: number;
  fourStars: number;
  threeStars: number;
  twoStars: number;
  oneStar: number;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private apiUrl = 'http://localhost:8080/api/reviews';
  constructor(private http: HttpClient) {}

  productReviews(productId: string): Observable<ProductReviewDTO[]> {
    return this.http.get<ProductReviewDTO[]>(`${this.apiUrl}/product/${productId}`);
  }

  summary(productId: string): Observable<ReviewSummaryDTO> {
    return this.http.get<ReviewSummaryDTO>(`${this.apiUrl}/product/${productId}/summary`);
  }

  mine(): Observable<ProductReviewDTO[]> {
    return this.http.get<ProductReviewDTO[]>(`${this.apiUrl}/me`);
  }

  create(dto: ProductReviewDTO): Observable<ProductReviewDTO> {
    return this.http.post<ProductReviewDTO>(this.apiUrl, dto);
  }

  adminFindAll(status?: ReviewStatus | ''): Observable<ProductReviewDTO[]> {
    const options = status ? { params: { status } } : {};
    return this.http.get<ProductReviewDTO[]>(`${this.apiUrl}/admin`, options);
  }

  moderate(id: string, status: ReviewStatus, adminReply?: string): Observable<ProductReviewDTO> {
    return this.http.put<ProductReviewDTO>(`${this.apiUrl}/admin/${id}/moderate`, { status, adminReply });
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
