import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import {
  ProductUserService,
  ProductUserDTO,
  SizeDTO,
  ColorDTO,
} from '../services/product-user.service';
import { CartService, CartItemDTO, AddToCartRequestDTO } from '../services/cart.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { ProductVariantService, ProductVariantDTO } from '../services/product-variant.service';
import { DetailAccountService } from '../services/detail-account.service';
import { CheckoutRequestDTO, PaymentMethod } from '../services/order.service';
import { CouponPreviewDTO, CouponService } from '../services/coupon.service';
import { ProductInsightService, ProductMetricDTO } from '../services/product-insight.service';

export interface ConfirmDialog {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  resolve: (value: boolean) => void;
}

type PopupType = 'success' | 'error' | 'info' | 'warning';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  currentUser: string | null = null;
  role: string | null = null;
  token: string | null = null;

  products: ProductUserDTO[] = [];
  trendingProducts: ProductMetricDTO[] = [];
  pagedProducts: ProductUserDTO[] = [];
  currentPage = 1;
  totalPages = 1;
  pageSize = 8;

  keyword = '';
  searchSubject = new Subject<string>();
  selectedMinPrice: number | null = null;
  selectedMaxPrice: number | null = null;

  priceRanges = [
    { label: 'All', min: null, max: null },
    { label: 'Under 100K', min: null, max: 100000 },
    { label: '100K – 300K', min: 100000, max: 300000 },
    { label: '300K – 500K', min: 300000, max: 500000 },
    { label: 'Above 500K', min: 500000, max: null },
  ];

  showCart = false;
  cartItems: CartItemDTO[] = [];
  cartLoading = false;
  cartError: string | null = null;
  checkoutProcessing = false;
  selectedIds: Set<string> = new Set();

  checkoutInfo = {
    receiverName: '',
    receiverPhone: '',
    shippingAddress: '',
    paymentMethod: 'COD' as PaymentMethod,
    note: '',
    couponCode: '',
  };
  couponPreview: CouponPreviewDTO | null = null;
  couponChecking = false;

  stockMap: Map<string, number> = new Map();
  stockLoading: Set<string> = new Set();

  showQuickAdd = false;
  quickAddProduct: ProductUserDTO | null = null;
  quickAddSizes: SizeDTO[] = [];
  quickAddColors: ColorDTO[] = [];
  quickAddVariants: ProductVariantDTO[] = [];
  quickAddSelectedSize: SizeDTO | null = null;
  quickAddSelectedColor: ColorDTO | null = null;
  quickAddQuantity = 1;
  quickAddLoading = false;
  quickAddAdding = false;

  confirmDialog: ConfirmDialog | null = null;
  popup: { type: PopupType; title: string; message: string } | null = null;

  constructor(
    private productUserService: ProductUserService,
    private cartService: CartService,
    private productVariantService: ProductVariantService,
    private detailAccountService: DetailAccountService,
    private couponService: CouponService,
    private cdr: ChangeDetectorRef,
    private productInsightService: ProductInsightService,
  ) {}

  ngOnInit() {
    this.refreshAuthState();

    window.addEventListener('storage', () => {
      this.refreshAuthState();
      this.cdr.detectChanges();
    });


    this.searchSubject.pipe(debounceTime(400)).subscribe(() => this.loadProducts());

    this.loadProducts();
    this.loadTrending();
  }

  refreshAuthState() {
    this.token = localStorage.getItem('token');
    this.role = localStorage.getItem('role');
    this.currentUser = localStorage.getItem('username');
  }

  showPopup(type: PopupType, title: string, message: string) {
    this.popup = { type, title, message };
    this.cdr.detectChanges();
  }

  closePopup() {
    this.popup = null;
    this.cdr.detectChanges();
  }

  openConfirm(
    title: string,
    message: string,
    confirmLabel = 'Confirm',
    cancelLabel = 'Cancel',
  ): Promise<boolean> {
    return new Promise((resolve) => {
      this.confirmDialog = { title, message, confirmLabel, cancelLabel, resolve };
      this.cdr.detectChanges();
    });
  }

  onConfirmYes() {
    this.confirmDialog?.resolve(true);
    this.confirmDialog = null;
    this.cdr.detectChanges();
  }

  onConfirmNo() {
    this.confirmDialog?.resolve(false);
    this.confirmDialog = null;
    this.cdr.detectChanges();
  }


  loadTrending() {
    this.productInsightService.trending(6).subscribe({
      next: (res) => { this.trendingProducts = res; this.cdr.detectChanges(); },
      error: () => undefined,
    });
  }

  loadProducts() {
    this.productUserService
      .searchProducts(
        this.keyword,
        this.selectedMinPrice ?? undefined,
        this.selectedMaxPrice ?? undefined,
        undefined,
      )
      .subscribe({
        next: (res) => {
          this.products = res;
          this.totalPages = Math.max(1, Math.ceil(this.products.length / this.pageSize));
          this.currentPage = 1;
          this.updatePagedProducts();
          this.stockMap.clear();
          this.stockLoading.clear();
          this.loadStockForPage();
          this.cdr.detectChanges();
        },
        error: () => {
          this.showPopup('error', 'Products unavailable', 'Could not load products. Please try again.');
        },
      });
  }

  onSearchInput() {
    this.searchSubject.next(this.keyword);
  }

  onSearchClick() {

    this.loadProducts();
    this.loadTrending();
  }

  selectPriceRange(range: any) {
    this.selectedMinPrice = range.min;
    this.selectedMaxPrice = range.max;

    this.loadProducts();
    this.loadTrending();
  }

  updatePagedProducts() {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedProducts = this.products.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePagedProducts();
    this.loadStockForPage();
    this.cdr.detectChanges();
  }

  buildImageSrc(image: string | null): string {
    if (!image) return '';
    if (image.startsWith('data:')) return image;
    return `data:image/jpeg;base64,${image}`;
  }

  loadStockForPage() {
    for (const product of this.pagedProducts) {
      const productId = product.productId;
      if (!productId || this.stockMap.has(productId) || this.stockLoading.has(productId)) continue;

      this.stockLoading.add(productId);

      this.productVariantService.getTotalQuantityByProductId(productId).subscribe({
        next: (totalQuantity) => {
          this.stockMap.set(productId, totalQuantity ?? 0);
          this.stockLoading.delete(productId);
          this.cdr.detectChanges();
        },
        error: () => {
          this.stockMap.set(productId, 0);
          this.stockLoading.delete(productId);
          this.cdr.detectChanges();
        },
      });
    }
  }

  getStock(productId: string): number | null {
    if (this.stockLoading.has(productId)) return null;
    return this.stockMap.get(productId) ?? null;
  }

  isOutOfStock(productId: string): boolean {
    const stock = this.getStock(productId);
    return stock !== null && stock === 0;
  }

  openQuickAdd(product: ProductUserDTO) {
    this.refreshAuthState();

    if (!this.currentUser || !this.token) {
      this.showPopup('warning', 'Login required', 'Please log in before adding products to your cart.');
      return;
    }

    if (this.isOutOfStock(product.productId)) {
      this.showPopup('error', 'Out of stock', 'This product is currently unavailable.');
      return;
    }

    this.quickAddProduct = product;
    this.quickAddSizes = [];
    this.quickAddColors = [];
    this.quickAddVariants = [];
    this.quickAddSelectedSize = null;
    this.quickAddSelectedColor = null;
    this.quickAddQuantity = 1;
    this.quickAddLoading = true;
    this.quickAddAdding = false;
    this.showQuickAdd = true;
    this.cdr.detectChanges();

    const productId = product.productId;
    let done = 0;

    const checkDone = () => {
      done++;
      if (done === 3) {
        this.quickAddLoading = false;
        this.cdr.detectChanges();
      }
    };

    this.productUserService.getSizesByProduct(productId).subscribe({
      next: (sizes) => {
        this.quickAddSizes = sizes;
        checkDone();
      },
      error: () => checkDone(),
    });

    this.productUserService.getColorsByProduct(productId).subscribe({
      next: (colors) => {
        this.quickAddColors = colors;
        checkDone();
      },
      error: () => checkDone(),
    });

    this.productVariantService.findByProduct(productId).subscribe({
      next: (variants) => {
        this.quickAddVariants = variants;
        checkDone();
      },
      error: () => checkDone(),
    });
  }

  closeQuickAdd() {
    this.showQuickAdd = false;
    this.quickAddProduct = null;
    this.cdr.detectChanges();
  }

  selectQuickSize(size: SizeDTO) {
    this.quickAddSelectedSize = size;
    this.quickAddQuantity = 1;
    this.cdr.detectChanges();
  }

  selectQuickColor(color: ColorDTO) {
    this.quickAddSelectedColor = color;
    this.quickAddQuantity = 1;
    this.cdr.detectChanges();
  }

  get quickAddCurrentStock(): number | null {
    if (!this.quickAddSelectedSize || !this.quickAddSelectedColor) return null;

    const variant = this.quickAddVariants.find(
      (item) =>
        item.sizeId === this.quickAddSelectedSize!.id &&
        item.colorId === this.quickAddSelectedColor!.id,
    );

    return variant?.quantity ?? 0;
  }

  get quickAddIsOutOfStock(): boolean {
    const stock = this.quickAddCurrentStock;
    return stock !== null && stock === 0;
  }

  quickIncreaseQty() {
    const max = this.quickAddCurrentStock;

    if (max !== null && this.quickAddQuantity >= max) {
      this.showPopup('error', 'Stock limit reached', `Only ${max} item(s) are available for this variant.`);
      return;
    }

    this.quickAddQuantity++;
    this.cdr.detectChanges();
  }

  quickDecreaseQty() {
    if (this.quickAddQuantity > 1) {
      this.quickAddQuantity--;
      this.cdr.detectChanges();
    }
  }

  submitQuickAdd() {
    if (!this.quickAddProduct) return;

    if (!this.quickAddSelectedSize) {
      this.showPopup('error', 'Size required', 'Please select a size.');
      return;
    }

    if (!this.quickAddSelectedColor) {
      this.showPopup('error', 'Color required', 'Please select a color.');
      return;
    }

    if (this.quickAddIsOutOfStock) {
      this.showPopup('error', 'Out of stock', 'This variant is currently unavailable.');
      return;
    }

    this.quickAddAdding = true;
    this.cdr.detectChanges();

    const request: AddToCartRequestDTO = {
      productId: this.quickAddProduct.productId,
      sizeId: this.quickAddSelectedSize.id,
      colorId: this.quickAddSelectedColor.id,
      quantity: this.quickAddQuantity,
    };

    this.cartService.addToCart(request).subscribe({
      next: () => {
        const productName = this.quickAddProduct?.productName || 'Product';
        const productId = this.quickAddProduct!.productId;

        this.quickAddAdding = false;
        this.closeQuickAdd();

        this.stockMap.delete(productId);
        this.loadStockForPage();

        this.showPopup('success', 'Added to cart', `${productName} was added to your cart successfully.`);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.quickAddAdding = false;
        const msg =
          typeof err.error === 'string' && err.error
            ? err.error
            : 'Failed to add item to cart.';
        this.showPopup('error', 'Add to cart failed', msg);
        this.cdr.detectChanges();
      },
    });
  }

  get cartTotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.subtotal, 0);
  }

  get cartCount(): number {
    return this.cartItems.reduce((sum, item) => sum + item.quantity, 0);
  }

  get selectedTotal(): number {
    return this.cartItems
      .filter((item) => this.selectedIds.has(item.cartItemId))
      .reduce((sum, item) => sum + item.subtotal, 0);
  }

  get payableTotal(): number {
    return this.couponPreview?.valid ? this.couponPreview.finalAmount : this.selectedTotal;
  }

  applyCoupon() {
    const code = (this.checkoutInfo.couponCode || '').trim();
    if (!code) {
      this.couponPreview = null;
      this.showPopup('warning', 'Coupon required', 'Enter a coupon code first.');
      return;
    }
    if (this.selectedTotal <= 0) {
      this.showPopup('warning', 'No items selected', 'Select cart items before applying a coupon.');
      return;
    }
    this.couponChecking = true;
    this.couponService.preview(code, this.selectedTotal).subscribe({
      next: (preview) => {
        this.couponPreview = preview;
        this.couponChecking = false;
        this.showPopup(preview.valid ? 'success' : 'warning', preview.valid ? 'Coupon applied' : 'Coupon unavailable', preview.message);
        this.cdr.detectChanges();
      },
      error: () => {
        this.couponChecking = false;
        this.showPopup('error', 'Coupon failed', 'Could not validate coupon.');
        this.cdr.detectChanges();
      },
    });
  }

  get selectedCount(): number {
    return this.selectedIds.size;
  }

  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }

  toggleSelect(id: string): void {
    if (this.selectedIds.has(id)) this.selectedIds.delete(id);
    else this.selectedIds.add(id);
    this.couponPreview = null;

    this.cdr.detectChanges();
  }

  isAllSelected(): boolean {
    return (
      this.cartItems.length > 0 &&
      this.cartItems.every((item) => this.selectedIds.has(item.cartItemId))
    );
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) this.selectedIds.clear();
    else this.cartItems.forEach((item) => this.selectedIds.add(item.cartItemId));
    this.couponPreview = null;

    this.cdr.detectChanges();
  }

  openCart() {
    this.refreshAuthState();

    if (!this.currentUser || !this.token) {
      this.showPopup('warning', 'Login required', 'Please log in to view your cart.');
      return;
    }

    this.showCart = true;
    this.prefillCheckoutInfo();
    this.loadMyCart(false);
  }

  prefillCheckoutInfo() {
    this.detailAccountService.getMe().subscribe({
      next: (profile) => {
        this.checkoutInfo.receiverName = `${profile.firstName} ${profile.lastName}`.trim();
        this.checkoutInfo.receiverPhone = profile.phone || '';
        this.checkoutInfo.shippingAddress = profile.address || '';
        this.cdr.detectChanges();
      },
      error: () => undefined,
    });
  }

  closeCart() {
    this.showCart = false;
    this.cartError = null;
  }

  loadMyCart(resetSelected = false) {
    if (this.checkoutProcessing) return;

    this.cartLoading = true;
    this.cartError = null;

    this.cartService.getMyCart().subscribe({
      next: (items) => {
        this.cartItems = items;
        this.cartLoading = false;

        if (resetSelected) {
          this.selectedIds.clear();
        this.couponPreview = null;
        this.checkoutInfo.couponCode = '';
        } else {
          const existingIds = new Set(items.map((item) => item.cartItemId));
          this.selectedIds.forEach((id) => {
            if (!existingIds.has(id)) this.selectedIds.delete(id);
          });
        }

        this.cdr.detectChanges();
      },
      error: () => {
        this.cartError = 'Failed to load cart.';
        this.cartLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  increaseQty(item: CartItemDTO) {
    if (this.checkoutProcessing) return;

    this.cartLoading = true;

    this.cartService.increaseQuantity(item.cartItemId, 1).subscribe({
      next: () => {
        this.loadMyCart(false);
        this.stockMap.clear();
        this.loadStockForPage();
      },
      error: (err) => {
        this.cartLoading = false;
        const msg = typeof err.error === 'string' && err.error ? err.error : 'Not enough stock.';
        this.showPopup('error', 'Cannot increase quantity', msg);
        this.cdr.detectChanges();
      },
    });
  }

  decreaseQty(item: CartItemDTO) {
    if (this.checkoutProcessing) return;

    this.cartLoading = true;

    this.cartService.decreaseQuantity(item.cartItemId, 1).subscribe({
      next: () => {
        this.loadMyCart(false);
        this.stockMap.clear();
        this.loadStockForPage();
      },
      error: (err) => {
        this.cartLoading = false;
        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Failed to decrease quantity.';
        this.showPopup('error', 'Quantity update failed', msg);
        this.cdr.detectChanges();
      },
    });
  }

  removeItem(item: CartItemDTO) {
    if (this.checkoutProcessing) return;

    this.cartLoading = true;

    this.cartService.deleteCartItem(item.cartItemId).subscribe({
      next: () => {
        this.selectedIds.delete(item.cartItemId);
        this.loadMyCart(false);
        this.showPopup('success', 'Item removed', 'The item was removed from your cart.');
      },
      error: (err) => {
        this.cartLoading = false;
        const msg = typeof err.error === 'string' && err.error ? err.error : 'Failed to remove item.';
        this.showPopup('error', 'Remove failed', msg);
        this.cdr.detectChanges();
      },
    });
  }

  async clearCart() {
    const ok = await this.openConfirm(
      'Clear cart',
      'Remove all items from your cart?',
      'Clear all',
      'Cancel',
    );

    if (!ok) return;

    const deletes = this.cartItems.map((item) =>
      this.cartService.deleteCartItem(item.cartItemId).toPromise(),
    );

    Promise.all(deletes)
      .then(() => {
        this.cartItems = [];
        this.selectedIds.clear();
        this.showPopup('success', 'Cart cleared', 'All items were removed from your cart.');
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.showPopup('error', 'Clear cart failed', 'Failed to clear cart.');
      });
  }

  async checkout() {
    if (this.checkoutProcessing) return;

    if (this.selectedIds.size === 0) {
      this.showPopup('warning', 'No items selected', 'Please select at least one item to checkout.');
      return;
    }

    if (!this.checkoutInfo.receiverName || !this.checkoutInfo.receiverPhone || !this.checkoutInfo.shippingAddress) {
      this.showPopup('warning', 'Shipping info required', 'Please enter receiver name, phone and shipping address.');
      return;
    }

    const selectedIdsSnapshot = Array.from(this.selectedIds);
    const selectedCountSnapshot = selectedIdsSnapshot.length;
    const totalSnapshot = this.payableTotal.toLocaleString('vi-VN');

    const ok = await this.openConfirm(
      'Confirm checkout',
      `Checkout ${selectedCountSnapshot} item(s) for a total of ${totalSnapshot}đ?`,
      'Checkout',
      'Cancel',
    );

    if (!ok) return;

    this.checkoutProcessing = true;
    this.cartLoading = true;
    this.cdr.detectChanges();

    const checkoutRequest: CheckoutRequestDTO = {
      cartItemIds: selectedIdsSnapshot,
      receiverName: this.checkoutInfo.receiverName,
      receiverPhone: this.checkoutInfo.receiverPhone,
      shippingAddress: this.checkoutInfo.shippingAddress,
      paymentMethod: this.checkoutInfo.paymentMethod,
      note: this.checkoutInfo.note,
      couponCode: this.checkoutInfo.couponCode || undefined,
    };

    this.cartService.checkout(checkoutRequest).subscribe({
      next: () => {
        this.cartItems = this.cartItems.filter(
          (item) => !selectedIdsSnapshot.includes(item.cartItemId),
        );
        this.selectedIds.clear();
        this.couponPreview = null;
        this.checkoutInfo.couponCode = '';

        this.checkoutProcessing = false;
        this.cartLoading = false;
        this.showCart = false;

        this.stockMap.clear();
        this.loadStockForPage();

        this.showPopup(
          'success',
          'Checkout successful',
          `Your order for ${selectedCountSnapshot} item(s) has been placed successfully.`,
        );

        this.cdr.detectChanges();
      },
      error: (err) => {
        this.checkoutProcessing = false;
        this.cartLoading = false;

        const msg = typeof err.error === 'string' && err.error ? err.error : (err.error?.message || 'Checkout failed.');
        this.showPopup('error', 'Checkout failed', msg);
        this.cdr.detectChanges();
      },
    });
  }

  isUser(): boolean {
    return this.isLoggedIn();
  }

  isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN';
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }
}
