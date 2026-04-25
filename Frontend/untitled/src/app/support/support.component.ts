import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SupportTicketDTO, SupportTicketService } from '../services/support-ticket.service';

type PopupType = 'success' | 'error' | 'info';

@Component({
  selector: 'app-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.css'],
})
export class SupportComponent implements OnInit {
  tickets: SupportTicketDTO[] = [];
  selected: SupportTicketDTO | null = null;
  loading = false;
  sending = false;
  creating = false;

  newTicket: SupportTicketDTO = {
    subject: '',
    category: 'ORDER',
    priority: 'NORMAL',
    initialMessage: '',
  };

  replyText = '';
  popup: { type: PopupType; title: string; message: string } | null = null;

  constructor(private supportService: SupportTicketService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loading = true;
    this.supportService.mine().subscribe({
      next: (res) => {
        this.tickets = res;
        this.selected = this.selected ? this.tickets.find(t => t.id === this.selected?.id) || this.tickets[0] || null : this.tickets[0] || null;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.showPopup('error', 'Support unavailable', 'Could not load your support tickets.');
      },
    });
  }

  select(ticket: SupportTicketDTO): void {
    this.selected = ticket;
  }

  createTicket(): void {
    if (!this.newTicket.subject.trim() || !this.newTicket.initialMessage?.trim()) {
      this.showPopup('error', 'Missing information', 'Please add a subject and message.');
      return;
    }
    this.creating = true;
    this.supportService.create(this.newTicket).subscribe({
      next: (ticket) => {
        this.creating = false;
        this.newTicket = { subject: '', category: 'ORDER', priority: 'NORMAL', initialMessage: '' };
        this.tickets = [ticket, ...this.tickets];
        this.selected = ticket;
        this.showPopup('success', 'Ticket created', 'Our team can now reply in this thread.');
        this.cdr.detectChanges();
      },
      error: () => {
        this.creating = false;
        this.showPopup('error', 'Create failed', 'Could not create a support ticket.');
      },
    });
  }

  sendReply(): void {
    if (!this.selected?.id || !this.replyText.trim()) return;
    this.sending = true;
    this.supportService.addMessage(this.selected.id, { message: this.replyText }).subscribe({
      next: (ticket) => {
        this.sending = false;
        this.replyText = '';
        this.selected = ticket;
        this.tickets = this.tickets.map(t => t.id === ticket.id ? ticket : t);
        this.cdr.detectChanges();
      },
      error: () => {
        this.sending = false;
        this.showPopup('error', 'Reply failed', 'Could not send your message.');
      },
    });
  }

  badge(status?: string): string {
    return (status || 'OPEN').toLowerCase().replace('_', '-');
  }

  showPopup(type: PopupType, title: string, message: string): void {
    this.popup = { type, title, message };
    this.cdr.detectChanges();
  }

  closePopup(): void {
    this.popup = null;
    this.cdr.detectChanges();
  }
}
