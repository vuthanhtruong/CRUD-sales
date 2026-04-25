import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type Gender = 'MALE' | 'FEMALE';

export interface ProfileDTO {
  username: string;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  address: string;
  gender: Gender;
  birthday: string;
}

@Injectable({ providedIn: 'root' })
export class DetailAccountService {
  private apiUrl = 'http://localhost:8080/api/detail-account';
  constructor(private http: HttpClient) {}

  getMe(): Observable<ProfileDTO> {
    return this.http.get<ProfileDTO>(`${this.apiUrl}/me`);
  }

  getMyRole(): Observable<string> {
    return this.http.get(`${this.apiUrl}/role`, { responseType: 'text' });
  }

  updateAccount(dto: ProfileDTO): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me`, dto);
  }
}
