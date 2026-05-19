import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterModule } from '@angular/router';
import { NotificationDTO, NotificationService } from '../services/notification.service';

@Component({ selector: 'app-notifications', standalone: true, imports: [CommonModule, RouterModule], templateUrl: './notifications.component.html', styleUrls: ['./notifications.component.css'] })
export class NotificationsComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  notifications: NotificationDTO[] = [];
  loading = false;
  constructor(private service: NotificationService, private cdr: ChangeDetectorRef) {}
  ngOnInit() { this.load(); }
  load() { this.loading = true; this.service.mine().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.notifications = x; this.loading = false; this.cdr.detectChanges(); }, error: () => { this.loading = false; this.cdr.detectChanges(); } }); }
  markRead(n: NotificationDTO) { this.service.markRead(n.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }
  markAll() { this.service.markAllRead().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
