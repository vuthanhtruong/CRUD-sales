import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { WishlistItemDTO, WishlistService } from '../services/wishlist.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './wishlist.component.html',
  styleUrls: ['./wishlist.component.css'],
})
export class WishlistComponent implements OnInit {
  items: WishlistItemDTO[] = [];
  loading = false;
  message = '';

  constructor(private wishlistService: WishlistService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.wishlistService.getMine().subscribe({
      next: (items) => { this.items = items; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.message = 'Could not load wishlist.'; this.loading = false; this.cdr.detectChanges(); },
    });
  }

  remove(productId: string) {
    this.wishlistService.remove(productId).subscribe({ next: () => this.load() });
  }

  img(image?: string | null) { return image ? `data:image/jpeg;base64,${image}` : 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=700&q=80'; }
}
