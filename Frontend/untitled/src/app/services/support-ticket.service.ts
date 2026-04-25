import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TicketStatus = 'OPEN' | 'WAITING_ADMIN' | 'WAITING_CUSTOMER' | 'RESOLVED' | 'CLOSED';

export interface SupportMessageDTO {
  id?: string;
  ticketId?: string;
  username?: string;
  senderName?: string;
  message: string;
  internalNote?: boolean;
  createdAt?: string;
}

export interface SupportTicketDTO {
  id?: string;
  username?: string;
  customerName?: string;
  subject: string;
  category?: string;
  priority?: string;
  status?: TicketStatus;
  initialMessage?: string;
  lastMessage?: string;
  createdAt?: string;
  updatedAt?: string;
  messages?: SupportMessageDTO[];
}

@Injectable({ providedIn: 'root' })
export class SupportTicketService {
  private apiUrl = 'http://localhost:8080/api/support';
  constructor(private http: HttpClient) {}

  mine(): Observable<SupportTicketDTO[]> {
    return this.http.get<SupportTicketDTO[]>(`${this.apiUrl}/me`);
  }

  create(dto: SupportTicketDTO): Observable<SupportTicketDTO> {
    return this.http.post<SupportTicketDTO>(this.apiUrl, dto);
  }

  addMessage(ticketId: string, dto: SupportMessageDTO): Observable<SupportTicketDTO> {
    return this.http.post<SupportTicketDTO>(`${this.apiUrl}/${ticketId}/messages`, dto);
  }

  adminFindAll(status?: TicketStatus | ''): Observable<SupportTicketDTO[]> {
    const options = status ? { params: { status } } : {};
    return this.http.get<SupportTicketDTO[]>(`${this.apiUrl}/admin`, options);
  }

  updateStatus(ticketId: string, status: TicketStatus): Observable<SupportTicketDTO> {
    return this.http.put<SupportTicketDTO>(`${this.apiUrl}/admin/${ticketId}/status`, { status });
  }
}
