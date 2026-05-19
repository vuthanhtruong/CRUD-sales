import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef, AfterViewInit, OnDestroy, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ProductReviewDTO, ReviewService, ReviewStatus } from '../services/review.service';

@Component({ selector: 'app-admin-reviews', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './admin-reviews.component.html', styleUrls: ['./admin-reviews.component.css'] })
export class AdminReviewsComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  reviews: ProductReviewDTO[] = [];
  status: ReviewStatus | '' = '';
  reply: Record<string, string> = {};
  constructor(private service: ReviewService, private cdr: ChangeDetectorRef) {}
  ngOnInit() { this.load(); }
  load() { this.service.adminFindAll(this.status).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: (x) => { this.reviews = x; this.cdr.detectChanges(); } }); }
  setReply(r: ProductReviewDTO, value: string) { if (r.id) this.reply[r.id] = value; }
  moderate(r: ProductReviewDTO, status: ReviewStatus) { if (!r.id) return; this.service.moderate(r.id, status, this.reply[r.id] || r.adminReply).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({ next: () => this.load() }); }
  stars(n: number) { return '★'.repeat(n) + '☆'.repeat(5 - n); }

  ngAfterViewInit(): void {
    this.cdr.detectChanges();
  }

  ngOnDestroy(): void {
    this.cdr.detach();
  }

}
