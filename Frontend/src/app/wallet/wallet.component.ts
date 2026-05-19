import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { WalletDTO, WalletService } from '../services/wallet.service';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './wallet.component.html',
  styleUrls: ['./wallet.component.css'],
})
export class WalletComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  wallet: WalletDTO | null = null;
  amount = 100000;
  note = '';
  loading = true;
  submitting = false;
  message = '';
  error = '';

  constructor(private walletService: WalletService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.load(); }

  load() {
    this.loading = true;
    this.walletService.mine().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res) => { this.wallet = res; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.error = 'Could not load wallet.'; this.loading = false; this.cdr.detectChanges(); },
    });
  }

  requestTopUp() {
    if (!this.amount || this.amount < 1000) { this.error = 'Minimum top-up amount is 1,000.'; return; }
    this.submitting = true; this.error = ''; this.message = '';
    this.walletService.topUp(this.amount, this.note).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.submitting = false; this.message = 'Top-up request submitted. Admin approval is required.'; this.note = ''; this.load(); },
      error: (err) => { this.submitting = false; this.error = err?.error?.message || 'Top-up request failed.'; this.cdr.detectChanges(); },
    });
  }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
