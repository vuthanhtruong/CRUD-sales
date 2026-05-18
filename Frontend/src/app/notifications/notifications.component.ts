import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NotificationDTO, NotificationService } from '../services/notification.service';

@Component({ selector: 'app-notifications', standalone: true, imports: [CommonModule, RouterModule], templateUrl: './notifications.component.html', styleUrls: ['./notifications.component.css'] })
export class NotificationsComponent implements OnInit {
  notifications: NotificationDTO[] = [];
  loading = false;
  constructor(private service: NotificationService, private cdr: ChangeDetectorRef) {}
  ngOnInit() { this.load(); }
  load() { this.loading = true; this.service.mine().subscribe({ next: (x) => { this.notifications = x; this.loading = false; this.cdr.detectChanges(); }, error: () => { this.loading = false; this.cdr.detectChanges(); } }); }
  markRead(n: NotificationDTO) { this.service.markRead(n.id).subscribe({ next: () => this.load() }); }
  markAll() { this.service.markAllRead().subscribe({ next: () => this.load() }); }
}
