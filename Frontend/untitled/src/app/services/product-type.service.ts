import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductType {
  id: string;
  typeName: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductTypeService {

  private apiUrl = 'http://localhost:8080/api/product-types';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ProductType[]> {
    return this.http.get<ProductType[]>(this.apiUrl, this.getAuthHeaders());
  }

  getById(id: string): Observable<ProductType> {
    return this.http.get<ProductType>(`${this.apiUrl}/${id}`, this.getAuthHeaders());
  }

  create(data: ProductType): Observable<any> {
    return this.http.post(this.apiUrl, data, this.getAuthHeaders());
  }

  update(id: string, data: ProductType): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data, this.getAuthHeaders());
  }

  delete(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, this.getAuthHeaders());
  }

  private getAuthHeaders() {
    const token = localStorage.getItem('token');

    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }
}
