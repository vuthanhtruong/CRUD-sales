import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Gender = 'MALE' | 'FEMALE';

export interface ProfileDTO {
  username: string;
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  gender: Gender;
  birthday: string; // LocalDate → 'YYYY-MM-DD'
}

@Injectable({
  providedIn: 'root'
})
export class DetailAccountService {

  private apiUrl = 'http://localhost:8080/api/detail-account';

  constructor(private http: HttpClient) {}

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
      headers: new HttpHeaders({
        Authorization: token ? `Bearer ${token}` : ''
      })
    };
  }

  getMe(): Observable<ProfileDTO> {
    return this.http.get<ProfileDTO>(`${this.apiUrl}/me`, this.getAuthHeaders());
  }

  getMyRole(): Observable<string> {
    return this.http.get(`${this.apiUrl}/role`, {
      ...this.getAuthHeaders(),
      responseType: 'text'
    });
  }

  updateAccount(dto: ProfileDTO): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/update/${dto.username}`,
      dto,
      this.getAuthHeaders()
    );
  }
}
