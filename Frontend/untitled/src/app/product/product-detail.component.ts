// product-detail.component.ts
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ProductUserService, SizeDTO, ColorDTO } from '../services/product-user.service';
import { ProductImageDTO } from '../services/product-image.service';
import { ProductDTO } from '../services/product.service';
import { CartService, AddToCartRequestDTO } from '../services/cart.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css'],
})
export class ProductDetailComponent implements OnInit {
  product: ProductDTO | null = null;
  images: ProductImageDTO[] = [];
  sizes: SizeDTO[] = [];
  colors: ColorDTO[] = [];

  selectedImage: ProductImageDTO | null = null;
  selectedSize: SizeDTO | null = null;
  selectedColor: ColorDTO | null = null;
  quantity: number = 1;

  loading = true;
  addingToCart = false;

  // toast
  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';
  private toastTimer: any;

  constructor(
    private route: ActivatedRoute,
    private productUserService: ProductUserService,
    private cartService: CartService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id')!;
    this.loadAll(productId);
  }

  loadAll(productId: string) {
    this.loading = true;
    forkJoin({
      product: this.productUserService.getProductById(productId),
      images: this.productUserService.getImagesByProduct(productId),
      sizes: this.productUserService.getSizesByProduct(productId),
      colors: this.productUserService.getColorsByProduct(productId),
    }).subscribe({
      next: (res) => {
        this.product = res.product;
        this.images = res.images;
        this.sizes = res.sizes;
        this.colors = res.colors;
        this.selectedImage = this.images.find((img) => img.isPrimary) ?? this.images[0] ?? null;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  selectImage(img: ProductImageDTO) {
    this.selectedImage = img;
    this.cdr.detectChanges();
  }

  selectSize(size: SizeDTO) {
    this.selectedSize = size;
    this.cdr.detectChanges();
  }

  selectColor(color: ColorDTO) {
    this.selectedColor = color;
    this.cdr.detectChanges();
  }

  increaseQty() {
    this.quantity++;
  }

  decreaseQty() {
    if (this.quantity > 1) this.quantity--;
  }

  buildImageSrc(img: ProductImageDTO): string {
    if (!img.imageData || !img.contentType) return '';
    if (img.imageData.startsWith('data:')) return img.imageData;
    return `data:${img.contentType};base64,${img.imageData}`;
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage = message;
    this.toastType = type;
    this.cdr.detectChanges();
    clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => {
      this.toastMessage = null;
      this.cdr.detectChanges();
    }, 3000);
  }

  addToCart() {
    if (!this.isLoggedIn()) {
      this.showToast('Please login to add items to cart.', 'error');
      return;
    }
    if (!this.product) return;
    if (!this.selectedSize) {
      this.showToast('Please select a size.', 'error');
      return;
    }
    if (!this.selectedColor) {
      this.showToast('Please select a color.', 'error');
      return;
    }

    this.addingToCart = true;

    const request: AddToCartRequestDTO = {
      productId: this.product.productId,
      sizeId: this.selectedSize.id,
      colorId: this.selectedColor.id,
      quantity: this.quantity,
    };

    this.cartService.addToCart(request).subscribe({
      next: () => {
        this.addingToCart = false;
        this.showToast(`"${this.product!.productName}" added to cart!`, 'success');
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.addingToCart = false;
        const msg = err?.error?.message || err?.error || 'Failed to add to cart.';
        this.showToast(typeof msg === 'string' ? msg : 'Failed to add to cart.', 'error');
        this.cdr.detectChanges();
      },
    });
  }
}
