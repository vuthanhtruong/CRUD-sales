import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AccountDTO {
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  avatarUrl?: string;
  address: string;
  username: string;
  password: string;
  gender: string;
  birthday: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AccountService {
  private baseUrl = 'http://localhost:8080/api/accounts';
  constructor(private http: HttpClient) {}

  register(data: AccountDTO): Observable<any> { return this.http.post(`${this.baseUrl}/register`, data); }
  login(data: LoginRequest): Observable<LoginResponse> { return this.http.post<LoginResponse>(`${this.baseUrl}/login`, data); }
  forgotPassword(email: string): Observable<any> { return this.http.post(`${this.baseUrl}/forgot-password`, { email }); }
  resetPassword(token: string, newPassword: string): Observable<any> { return this.http.post(`${this.baseUrl}/reset-password`, { token, newPassword }); }
  checkUsername(username: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/username`, { params: { username } }); }
  checkPhone(phone: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/phone`, { params: { phone } }); }
  checkEmail(email: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/email`, { params: { email } }); }
}
