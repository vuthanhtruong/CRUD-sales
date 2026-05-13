import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

  getAll(): Observable<Size[]> {
    return this.http.get<Size[]>(this.apiUrl);
  }

  create(size: Size): Observable<Size> {
    return this.http.post<Size>(this.apiUrl, size);
  }

  update(id: string, size: Size): Observable<Size> {
    return this.http.put<Size>(`${this.apiUrl}/${id}`, size);
  }

  delete(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getById(id: string): Observable<Size> {
    return this.http.get<Size>(`${this.apiUrl}/${id}`);
  }
}
