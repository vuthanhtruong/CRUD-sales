import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrderDTO, OrderService, OrderStatus } from '../services/order.service';

@Component({
  selector: 'app-admin-orders',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css'],
})
export class AdminOrdersComponent implements OnInit {
  orders: OrderDTO[] = [];
  statusFilter: OrderStatus | '' = '';
  statuses: (OrderStatus | '')[] = ['', 'PENDING', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED'];
  loading = false;
  message = '';
  error = '';

  constructor(private orderService: OrderService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadOrders(); }

  loadOrders() {
    this.loading = true;
    this.message = '';
    this.error = '';
    this.orderService.getAllOrders(this.statusFilter).subscribe({
      next: (orders) => { this.orders = orders; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Could not load orders.'; this.loading = false; this.cdr.detectChanges(); },
    });
  }

  updateStatus(order: OrderDTO, status: OrderStatus) {
    this.loading = true;
    this.orderService.updateStatus(order.id, status).subscribe({
      next: (updated) => {
        order.status = updated.status;
        this.message = `Order ${order.id} moved to ${updated.status}.`;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.error = 'Could not update order status.'; this.loading = false; this.cdr.detectChanges(); },
    });
  }

  statusClass(status: string): string { return `status ${status.toLowerCase()}`; }
}
