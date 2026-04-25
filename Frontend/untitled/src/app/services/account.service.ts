import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
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

@Injectable({ providedIn: 'root' })
export class AccountService {
  private baseUrl = 'http://localhost:8080/api/accounts';
  constructor(private http: HttpClient) {}

  register(data: AccountDTO): Observable<any> { return this.http.post(`${this.baseUrl}/register`, data); }
  login(data: any): Observable<any> { return this.http.post(`${this.baseUrl}/login`, data); }
  forgotPassword(email: string): Observable<any> { return this.http.post(`${this.baseUrl}/forgot-password`, { email }); }
  resetPassword(token: string, newPassword: string): Observable<any> { return this.http.post(`${this.baseUrl}/reset-password`, { token, newPassword }); }
  getRole(): Observable<{ role: string }> { return this.http.get<{ role: string }>(`${this.baseUrl}/role`, this.getAuthHeaders()); }
  getCurrentUser(): Observable<any> { return this.http.get(`${this.baseUrl}/current`, this.getAuthHeaders()); }
  checkUsername(username: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/username`, { params: { username } }); }
  checkPhone(phone: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/phone`, { params: { phone } }); }
  checkEmail(email: string): Observable<boolean> { return this.http.get<boolean>(`${this.baseUrl}/exists/email`, { params: { email } }); }

  private getAuthHeaders() {
    const token = localStorage.getItem('token');
    return { headers: new HttpHeaders({ Authorization: token ? `Bearer ${token}` : '' }) };
  }
}
