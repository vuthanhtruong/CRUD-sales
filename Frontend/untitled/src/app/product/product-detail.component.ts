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

import { ProductVariantService, ProductVariantDTO } from '../services/product-variant.service';

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

  variants: ProductVariantDTO[] = [];

  selectedImage: ProductImageDTO | null = null;
  selectedSize: SizeDTO | null = null;
  selectedColor: ColorDTO | null = null;

  quantity: number = 1;

  loading = true;
  addingToCart = false;

  // Tổng tồn kho của toàn bộ sản phẩm
  totalQuantity: number | null = null;
  stockLoading = false;

  // toast / popup
  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';
  private toastTimer: any;

  constructor(
    private route: ActivatedRoute,
    private productUserService: ProductUserService,
    private productVariantService: ProductVariantService,
    private cartService: CartService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id')!;
    this.loadAll(productId);
  }

  loadAll(productId: string) {
    this.loading = true;
    this.stockLoading = true;

    forkJoin({
      product: this.productUserService.getProductById(productId),
      images: this.productUserService.getImagesByProduct(productId),
      sizes: this.productUserService.getSizesByProduct(productId),
      colors: this.productUserService.getColorsByProduct(productId),
      variants: this.productVariantService.findByProduct(productId),
      totalQuantity: this.productVariantService.getTotalQuantityByProductId(productId),
    }).subscribe({
      next: (res) => {
        this.product = res.product;
        this.images = res.images;
        this.sizes = res.sizes;
        this.colors = res.colors;
        this.variants = res.variants;
        this.totalQuantity = res.totalQuantity ?? 0;

        this.selectedImage = this.images.find((img) => img.isPrimary) ?? this.images[0] ?? null;

        this.loading = false;
        this.stockLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);

        this.loading = false;
        this.stockLoading = false;
        this.totalQuantity = 0;

        this.showToast('Failed to load product detail.', 'error');
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
    this.quantity = 1;
    this.cdr.detectChanges();
  }

  selectColor(color: ColorDTO) {
    this.selectedColor = color;
    this.quantity = 1;
    this.cdr.detectChanges();
  }

  get selectedVariant(): ProductVariantDTO | null {
    if (!this.selectedSize || !this.selectedColor) return null;

    return (
      this.variants.find(
        (variant) =>
          variant.sizeId === this.selectedSize!.id && variant.colorId === this.selectedColor!.id,
      ) ?? null
    );
  }

  get selectedVariantQuantity(): number | null {
    if (!this.selectedSize || !this.selectedColor) return null;
    return this.selectedVariant?.quantity ?? 0;
  }

  get isProductOutOfStock(): boolean {
    return this.totalQuantity !== null && this.totalQuantity <= 0;
  }

  get isSelectedVariantOutOfStock(): boolean {
    const stock = this.selectedVariantQuantity;
    return stock !== null && stock <= 0;
  }

  get canAddToCart(): boolean {
    if (!this.product) return false;
    if (!this.selectedSize) return false;
    if (!this.selectedColor) return false;
    if (this.isProductOutOfStock) return false;
    if (this.isSelectedVariantOutOfStock) return false;
    if (this.addingToCart) return false;

    const variantStock = this.selectedVariantQuantity;

    if (variantStock !== null && this.quantity > variantStock) {
      return false;
    }

    return true;
  }

  increaseQty() {
    const max = this.selectedVariantQuantity;

    if (max !== null && this.quantity >= max) {
      this.showToast(`Chỉ còn ${max} sản phẩm cho biến thể này.`, 'error');
      return;
    }

    this.quantity++;
    this.cdr.detectChanges();
  }

  decreaseQty() {
    if (this.quantity > 1) {
      this.quantity--;
      this.cdr.detectChanges();
    }
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

  reloadStockAfterAddToCart() {
    if (!this.product) return;

    const productId = this.product.productId;

    forkJoin({
      variants: this.productVariantService.findByProduct(productId),
      totalQuantity: this.productVariantService.getTotalQuantityByProductId(productId),
    }).subscribe({
      next: (res) => {
        this.variants = res.variants;
        this.totalQuantity = res.totalQuantity ?? 0;

        const currentStock = this.selectedVariantQuantity;

        if (currentStock !== null && this.quantity > currentStock) {
          this.quantity = Math.max(1, currentStock);
        }

        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      },
    });
  }

  addToCart() {
    if (!this.isLoggedIn()) {
      this.showToast('Please login to add items to cart.', 'error');
      return;
    }

    if (!this.product) return;

    if (this.isProductOutOfStock) {
      this.showToast('Sản phẩm đã hết hàng.', 'error');
      return;
    }

    if (!this.selectedSize) {
      this.showToast('Please select a size.', 'error');
      return;
    }

    if (!this.selectedColor) {
      this.showToast('Please select a color.', 'error');
      return;
    }

    const variantStock = this.selectedVariantQuantity;

    if (variantStock === null) {
      this.showToast('Please select a valid variant.', 'error');
      return;
    }

    if (variantStock <= 0) {
      this.showToast('Biến thể này đã hết hàng.', 'error');
      return;
    }

    if (this.quantity > variantStock) {
      this.showToast(`Chỉ còn ${variantStock} sản phẩm cho biến thể này.`, 'error');
      return;
    }

    this.addingToCart = true;
    this.cdr.detectChanges();

    const request: AddToCartRequestDTO = {
      productId: this.product.productId,
      sizeId: this.selectedSize.id,
      colorId: this.selectedColor.id,
      quantity: this.quantity,
    };

    this.cartService.addToCart(request).subscribe({
      next: () => {
        this.addingToCart = false;

        this.showToast(`Đã thêm "${this.product!.productName}" vào giỏ hàng!`, 'success');

        this.reloadStockAfterAddToCart();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.addingToCart = false;

        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'Failed to add to cart.';

        this.showToast(msg, 'error');
        this.cdr.detectChanges();
      },
    });
  }
}
