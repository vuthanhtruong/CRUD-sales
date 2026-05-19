import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterModule } from '@angular/router';
import { OrderDTO, OrderService } from '../services/order.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css'],
})
export class OrdersComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  orders: OrderDTO[] = [];
  loading = true;
  error = '';
  expanded: Set<string> = new Set();

  constructor(private orderService: OrderService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadOrders(); }

  loadOrders() {
    this.loading = true;
    this.error = '';
    this.orderService.getMyOrders().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Could not load orders.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  toggle(id: string) {
    if (this.expanded.has(id)) this.expanded.delete(id);
    else this.expanded.add(id);
    this.cdr.detectChanges();
  }

  isExpanded(id: string): boolean { return this.expanded.has(id); }

  statusClass(status: string): string { return `status ${status.toLowerCase()}`; }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
