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

type PopupType = 'success' | 'error' | 'info' | 'warning';

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
  quantity = 1;

  loading = true;
  addingToCart = false;
  totalQuantity: number | null = null;
  stockLoading = false;

  popup: { type: PopupType; title: string; message: string } | null = null;

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
      error: () => {
        this.loading = false;
        this.stockLoading = false;
        this.totalQuantity = 0;
        this.showPopup('error', 'Product unavailable', 'Failed to load product detail.');
        this.cdr.detectChanges();
      },
    });
  }

  showPopup(type: PopupType, title: string, message: string) {
    this.popup = { type, title, message };
    this.cdr.detectChanges();
  }

  closePopup() {
    this.popup = null;
    this.cdr.detectChanges();
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
    return this.variants.find(
      (variant) =>
        variant.sizeId === this.selectedSize!.id &&
        variant.colorId === this.selectedColor!.id
    ) ?? null;
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
    if (variantStock !== null && this.quantity > variantStock) return false;

    return true;
  }

  increaseQty() {
    const max = this.selectedVariantQuantity;

    if (max !== null && this.quantity >= max) {
      this.showPopup('error', 'Stock limit reached', `Only ${max} item(s) are available for this variant.`);
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
      error: () => this.cdr.detectChanges(),
    });
  }

  addToCart() {
    if (!this.isLoggedIn()) {
      this.showPopup('warning', 'Login required', 'Please log in before adding products to your cart.');
      return;
    }

    if (!this.product) return;

    if (this.isProductOutOfStock) {
      this.showPopup('error', 'Out of stock', 'This product is currently unavailable.');
      return;
    }

    if (!this.selectedSize) {
      this.showPopup('error', 'Size required', 'Please select a size.');
      return;
    }

    if (!this.selectedColor) {
      this.showPopup('error', 'Color required', 'Please select a color.');
      return;
    }

    const variantStock = this.selectedVariantQuantity;

    if (variantStock === null) {
      this.showPopup('error', 'Invalid variant', 'Please select a valid product variant.');
      return;
    }

    if (variantStock <= 0) {
      this.showPopup('error', 'Out of stock', 'This variant is currently unavailable.');
      return;
    }

    if (this.quantity > variantStock) {
      this.showPopup('error', 'Stock limit reached', `Only ${variantStock} item(s) are available for this variant.`);
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
        this.showPopup(
          'success',
          'Added to cart',
          `${this.product!.productName} was added to your cart successfully.`,
        );
        this.reloadStockAfterAddToCart();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.addingToCart = false;
        const msg =
          typeof err?.error === 'string'
            ? err.error
            : err?.error?.message || 'Failed to add item to cart.';

        this.showPopup('error', 'Add to cart failed', msg);
        this.cdr.detectChanges();
      },
    });
  }
}
