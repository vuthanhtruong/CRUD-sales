import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AddressBookDTO {
  id?: string;
  receiverName: string;
  receiverPhone: string;
  fullAddress: string;
  city?: string;
  district?: string;
  ward?: string;
  label?: string;
  defaultAddress: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class AddressBookService {
  private apiUrl = 'http://localhost:8080/api/addresses';
  constructor(private http: HttpClient) {}

  getMine(): Observable<AddressBookDTO[]> {
    return this.http.get<AddressBookDTO[]>(this.apiUrl);
  }

  create(dto: AddressBookDTO): Observable<AddressBookDTO> {
    return this.http.post<AddressBookDTO>(this.apiUrl, dto);
  }

  update(id: string, dto: AddressBookDTO): Observable<AddressBookDTO> {
    return this.http.put<AddressBookDTO>(`${this.apiUrl}/${id}`, dto);
  }

  setDefault(id: string): Observable<AddressBookDTO> {
    return this.http.put<AddressBookDTO>(`${this.apiUrl}/${id}/default`, {});
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
