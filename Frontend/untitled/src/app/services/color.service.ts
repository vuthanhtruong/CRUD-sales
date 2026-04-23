import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ColorDTO {
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class ColorService {

  private apiUrl = 'http://localhost:8080/api/colors';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');

    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }

  getAll(): Observable<ColorDTO[]> {
    return this.http.get<ColorDTO[]>(
      this.apiUrl,
      this.getAuthHeaders()
    );
  }

  create(color: ColorDTO): Observable<any> {
    return this.http.post(
      this.apiUrl,
      color,
      this.getAuthHeaders()
    );
  }

  update(id: string, color: ColorDTO) {
    return this.http.put(
      `${this.apiUrl}/${id}`,
      color,
      {
        ...this.getAuthHeaders(),
        responseType: 'text'
      }
    );
  }

  delete(id: string): Observable<any> {
    return this.http.delete(
      `${this.apiUrl}/${id}`,
      {
        ...this.getAuthHeaders()
      }
    );
  }
  getById(id: string): Observable<ColorDTO> {
    return this.http.get<ColorDTO>(`${this.apiUrl}/${id}`, this.getAuthHeaders());
  }
}
