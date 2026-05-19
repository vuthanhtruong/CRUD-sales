import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { CouponDTO, CouponService } from '../services/coupon.service';

@Component({ selector: 'app-admin-coupons', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './admin-coupons.component.html', styleUrls: ['./admin-coupons.component.css'] })
export class AdminCouponsComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  coupons: CouponDTO[] = [];
  editingId: string | null = null;
  form: CouponDTO = this.blank();
  constructor(private service: CouponService, private cdr: ChangeDetectorRef) {}
  ngOnInit() { this.load(); }
  blank(): CouponDTO { return { code: '', name: '', discountType: 'PERCENTAGE', discountValue: 10, minOrderAmount: 0, active: true }; }
  load() { this.service.findAll().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.coupons = x; this.cdr.detectChanges(); } }); }
  edit(c: CouponDTO) { this.editingId = c.id || null; this.form = { ...c }; }
  reset() { this.editingId = null; this.form = this.blank(); }
  save() { const req = this.editingId ? this.service.update(this.editingId, this.form) : this.service.create(this.form); req.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => { this.reset(); this.load(); } }); }
  delete(c: CouponDTO) { if (!c.id) return; this.service.delete(c.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
