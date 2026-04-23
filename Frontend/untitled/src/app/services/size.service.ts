import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Size {
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class SizeService {

  private apiUrl = 'http://localhost:8080/api/sizes';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');

    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }

  getAll(): Observable<Size[]> {
    return this.http.get<Size[]>(this.apiUrl, this.getAuthHeaders());
  }

  create(size: Size): Observable<Size> {
    return this.http.post<Size>(this.apiUrl, size, this.getAuthHeaders());
  }

  update(id: string, size: Size): Observable<Size> {
    return this.http.put<Size>(`${this.apiUrl}/${id}`, size, this.getAuthHeaders());
  }

  delete(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, this.getAuthHeaders());
  }
  getById(id: string): Observable<Size> {
    return this.http.get<Size>(`${this.apiUrl}/${id}`, this.getAuthHeaders());
  }
}
