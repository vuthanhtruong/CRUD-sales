import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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
    return this.http.get<ProductType[]>(this.apiUrl);
  }

  getById(id: string): Observable<ProductType> {
    return this.http.get<ProductType>(`${this.apiUrl}/${id}`);
  }

  create(data: ProductType): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }

  update(id: string, data: ProductType): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
