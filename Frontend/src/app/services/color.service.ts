import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ColorDTO {
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class ColorService {

  private apiUrl = '/api/colors';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ColorDTO[]> {
    return this.http.get<ColorDTO[]>(this.apiUrl);
  }

  create(color: ColorDTO): Observable<any> {
    return this.http.post(this.apiUrl, color);
  }

  update(id: string, color: ColorDTO) {
    return this.http.put(`${this.apiUrl}/${id}`, color, { responseType: 'text' });
  }

  delete(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getById(id: string): Observable<ColorDTO> {
    return this.http.get<ColorDTO>(`${this.apiUrl}/${id}`);
  }
}
