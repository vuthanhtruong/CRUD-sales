import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { WalletService, WalletTopUpStatus, WalletTransactionDTO } from '../services/wallet.service';

@Component({
  selector: 'app-admin-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-wallet.component.html',
  styleUrls: ['./admin-wallet.component.css'],
})
export class AdminWalletComponent implements OnInit {
  status: WalletTopUpStatus = 'PENDING';
  rows: WalletTransactionDTO[] = [];
  loading = false;

  constructor(private walletService: WalletService, private cdr: ChangeDetectorRef) {}
  ngOnInit(): void { this.load(); }
  load() { this.loading = true; this.walletService.adminTopUps(this.status).subscribe({ next: r => { this.rows = r; this.loading = false; this.cdr.detectChanges(); }, error: () => { this.loading = false; this.cdr.detectChanges(); }}); }
  approve(id: string) { this.walletService.approve(id).subscribe({ next: () => this.load() }); }
  reject(id: string) { this.walletService.reject(id).subscribe({ next: () => this.load() }); }
}
