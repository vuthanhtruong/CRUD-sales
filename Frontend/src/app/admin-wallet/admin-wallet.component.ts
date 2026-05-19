import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { WalletService, WalletTopUpStatus, WalletTransactionDTO } from '../services/wallet.service';

@Component({
  selector: 'app-admin-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-wallet.component.html',
  styleUrls: ['./admin-wallet.component.css'],
})
export class AdminWalletComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  status: WalletTopUpStatus = 'PENDING';
  rows: WalletTransactionDTO[] = [];
  loading = false;

  constructor(private walletService: WalletService, private cdr: ChangeDetectorRef) {}
  ngOnInit(): void { this.load(); }
  load() { this.loading = true; this.walletService.adminTopUps(this.status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: r => { this.rows = r; this.loading = false; this.cdr.detectChanges(); }, error: () => { this.loading = false; this.cdr.detectChanges(); }}); }
  approve(id: string) { this.walletService.approve(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }
  reject(id: string) { this.walletService.reject(id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
