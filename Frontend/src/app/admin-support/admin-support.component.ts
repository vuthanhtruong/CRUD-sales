import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { SupportTicketDTO, SupportTicketService, TicketStatus } from '../services/support-ticket.service';

@Component({
  selector: 'app-admin-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support.component.html',
  styleUrls: ['./admin-support.component.css'],
})
export class AdminSupportComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  tickets: SupportTicketDTO[] = [];
  selected: SupportTicketDTO | null = null;
  statusFilter: TicketStatus | '' = '';
  replyText = '';
  internalNote = false;
  loading = false;
  saving = false;
  statuses: TicketStatus[] = ['OPEN', 'WAITING_ADMIN', 'WAITING_CUSTOMER', 'RESOLVED', 'CLOSED'];

  constructor(private supportService: SupportTicketService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadTickets(); }

  loadTickets(): void {
    this.loading = true;
    this.supportService.adminFindAll(this.statusFilter).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => {
        this.tickets = res;
        this.selected = this.selected ? this.tickets.find(t => t.id === this.selected?.id) || this.tickets[0] || null : this.tickets[0] || null;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); },
    });
  }

  select(ticket: SupportTicketDTO): void { this.selected = ticket; this.replyText = ''; }

  sendReply(): void {
    if (!this.selected?.id || !this.replyText.trim()) return;
    this.saving = true;
    this.supportService.addMessage(this.selected.id, { message: this.replyText, internalNote: this.internalNote }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (ticket) => {
        this.saving = false;
        this.replyText = '';
        this.internalNote = false;
        this.selected = ticket;
        this.tickets = this.tickets.map(t => t.id === ticket.id ? ticket : t);
        this.cdr.detectChanges();
      },
      error: () => { this.saving = false; this.cdr.detectChanges(); },
    });
  }

  updateStatus(status: TicketStatus): void {
    if (!this.selected?.id) return;
    this.saving = true;
    this.supportService.updateStatus(this.selected.id, status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (ticket) => {
        this.saving = false;
        this.selected = ticket;
        this.tickets = this.tickets.map(t => t.id === ticket.id ? ticket : t);
        this.cdr.detectChanges();
      },
      error: () => { this.saving = false; this.cdr.detectChanges(); },
    });
  }

  badge(status?: string): string { return (status || 'OPEN').toLowerCase().replace('_', '-'); }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
