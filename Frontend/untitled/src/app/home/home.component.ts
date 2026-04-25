import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormsModule,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { AccountService } from '../services/account.service';
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

// ─── Toast ───────────────────────────────────────────────────────────────────
export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'info' | 'warning';
  title: string;
  message?: string;
}

// ─── Confirm dialog ──────────────────────────────────────────────────────────
export interface ConfirmDialog {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  resolve: (value: boolean) => void;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  registerForm: any;
  loginForm: any;

  showRegister = false;
  showLogin = false;

  currentUser: string | null = null;
  role: string | null = null;
  token: string | null = null;

  // ================= PRODUCT =================
  products: ProductUserDTO[] = [];
  pagedProducts: ProductUserDTO[] = [];
  currentPage = 1;
  totalPages = 1;
  pageSize = 8;

  // ================= SEARCH =================
  keyword: string = '';
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

  // ================= CART =================
  showCart = false;
  cartItems: CartItemDTO[] = [];
  cartLoading = false;
  cartError: string | null = null;
  selectedIds: Set<string> = new Set();

  // ================= STOCK =================
  // stockMap lưu tổng tồn kho của từng sản phẩm.
  // Key: productId
  // Value: tổng quantity của tất cả biến thể size + color.
  stockMap: Map<string, number> = new Map();
  stockLoading: Set<string> = new Set();

  // ================= QUICK-ADD MODAL =================
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
  quickAddToastMsg: string | null = null;
  quickAddToastType: 'success' | 'error' = 'success';
  private quickAddToastTimer: any;

  // ================= TOAST SYSTEM =================
  toasts: ToastMessage[] = [];
  private toastCounter = 0;

  // ================= CONFIRM DIALOG =================
  confirmDialog: ConfirmDialog | null = null;

  // ================= REGISTER VALIDATION =================
  registerSubmitted = false;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private productUserService: ProductUserService,
    private cartService: CartService,
    private productVariantService: ProductVariantService,
    private cdr: ChangeDetectorRef,
  ) {
    this.registerForm = this.fb.group(
      {
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        phone: ['', [Validators.required, Validators.pattern(/^[0-9]{9,11}$/)]],
        address: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        username: [
          '',
          [Validators.required, Validators.minLength(4), Validators.pattern(/^[a-zA-Z0-9_]+$/)],
        ],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
        gender: ['MALE'],
        birthday: [''],
      },
      { validators: this.passwordMatchValidator },
    );

    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
  }

  // ─── Custom validator ─────────────────────────────────────────────────────
  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const pw = group.get('password')?.value;
    const cpw = group.get('confirmPassword')?.value;
    return pw && cpw && pw !== cpw ? { passwordMismatch: true } : null;
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────
  getRegField(name: string) {
    return this.registerForm.get(name);
  }

  regFieldInvalid(name: string): boolean {
    const ctrl = this.getRegField(name);
    return ctrl && (ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.invalid;
  }

  regFieldError(name: string): string {
    const ctrl = this.getRegField(name);

    if (!ctrl || !ctrl.errors) return '';

    if (ctrl.errors['required']) return 'This field is required.';
    if (ctrl.errors['minlength'])
      return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    if (ctrl.errors['email']) return 'Enter a valid email address.';

    if (ctrl.errors['pattern']) {
      if (name === 'phone') return 'Phone must be 9–11 digits.';
      if (name === 'username') return 'Username may only contain letters, numbers and underscores.';
    }

    return 'Invalid value.';
  }

  get confirmPasswordError(): string {
    const ctrl = this.getRegField('confirmPassword');

    if (!ctrl) return '';

    if ((ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.errors?.['required']) {
      return 'Please confirm your password.';
    }

    if (
      (ctrl.dirty || ctrl.touched || this.registerSubmitted) &&
      this.registerForm.errors?.['passwordMismatch']
    ) {
      return 'Passwords do not match.';
    }

    return '';
  }

  // ================= TOAST =================
  showToast(type: ToastMessage['type'], title: string, message?: string, duration = 3500) {
    const id = ++this.toastCounter;

    this.toasts.push({ id, type, title, message });
    this.cdr.detectChanges();

    setTimeout(() => this.dismissToast(id), duration);
  }

  dismissToast(id: number) {
    this.toasts = this.toasts.filter((t) => t.id !== id);
    this.cdr.detectChanges();
  }

  toastIcon(type: ToastMessage['type']): string {
    const map: Record<string, string> = {
      success: 'bi-check-circle-fill',
      error: 'bi-x-circle-fill',
      info: 'bi-info-circle-fill',
      warning: 'bi-exclamation-triangle-fill',
    };

    return map[type] ?? 'bi-bell-fill';
  }

  // ================= CONFIRM DIALOG =================
  openConfirm(
    title: string,
    message: string,
    confirmLabel = 'Confirm',
    cancelLabel = 'Cancel',
  ): Promise<boolean> {
    return new Promise((resolve) => {
      this.confirmDialog = {
        title,
        message,
        confirmLabel,
        cancelLabel,
        resolve,
      };

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

  // ================= INIT =================
  ngOnInit() {
    this.token = localStorage.getItem('token');
    this.role = localStorage.getItem('role');
    this.currentUser = localStorage.getItem('username');

    if (this.token) {
      this.loadCurrentUser();
      this.loadRole();
    }

    this.searchSubject.pipe(debounceTime(400)).subscribe(() => this.loadProducts());

    this.loadProducts();
  }

  // ================= PRODUCTS =================
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

          // Xóa cache stock cũ để tránh hiển thị sai khi search/filter.
          this.stockMap.clear();
          this.stockLoading.clear();

          this.loadStockForPage();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error(err);
          this.showToast('error', 'Failed to load products', 'Please try again later.');
        },
      });
  }

  onSearchInput() {
    this.searchSubject.next(this.keyword);
  }

  onSearchClick() {
    this.loadProducts();
  }

  selectPriceRange(range: any) {
    this.selectedMinPrice = range.min;
    this.selectedMaxPrice = range.max;
    this.loadProducts();
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

  // ================= STOCK =================
  loadStockForPage() {
    for (const product of this.pagedProducts) {
      const productId = product.productId;

      if (!productId) continue;
      if (this.stockMap.has(productId)) continue;
      if (this.stockLoading.has(productId)) continue;

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

  hasStock(productId: string): boolean {
    const stock = this.getStock(productId);
    return stock !== null && stock > 0;
  }

  // ================= QUICK-ADD MODAL =================
  openQuickAdd(product: ProductUserDTO) {
    if (!this.currentUser) {
      this.showToast('warning', 'Login required', 'Please log in to add items to your cart.');
      return;
    }

    if (this.isOutOfStock(product.productId)) {
      this.showToast('error', 'Out of stock', 'This product is currently unavailable.');
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
    this.quickAddToastMsg = null;
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
    this.quickAddToastMsg = null;
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
      this.showQuickToast(`Only ${max} item(s) left in stock.`, 'error');
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

  showQuickToast(msg: string, type: 'success' | 'error') {
    this.quickAddToastMsg = msg;
    this.quickAddToastType = type;

    this.cdr.detectChanges();

    clearTimeout(this.quickAddToastTimer);

    this.quickAddToastTimer = setTimeout(() => {
      this.quickAddToastMsg = null;
      this.cdr.detectChanges();
    }, 3000);
  }

  submitQuickAdd() {
    if (!this.quickAddProduct) return;

    if (!this.quickAddSelectedSize) {
      this.showQuickToast('Please select a size.', 'error');
      return;
    }

    if (!this.quickAddSelectedColor) {
      this.showQuickToast('Please select a color.', 'error');
      return;
    }

    if (this.quickAddIsOutOfStock) {
      this.showQuickToast('Out of stock.', 'error');
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
        this.quickAddAdding = false;
        this.showQuickToast('Added to cart! 🛒', 'success');

        const productId = this.quickAddProduct!.productId;

        // Reload lại stock tổng của sản phẩm này.
        this.stockMap.delete(productId);
        this.loadStockForPage();

        this.cdr.detectChanges();
      },
      error: (err) => {
        this.quickAddAdding = false;

        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Failed to add item to cart.';

        this.showQuickToast(msg, 'error');
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: COMPUTED =================
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

  get selectedCount(): number {
    return this.selectedIds.size;
  }

  // ================= CART: CHECKBOX =================
  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }

  toggleSelect(id: string): void {
    if (this.selectedIds.has(id)) {
      this.selectedIds.delete(id);
    } else {
      this.selectedIds.add(id);
    }

    this.cdr.detectChanges();
  }

  isAllSelected(): boolean {
    return (
      this.cartItems.length > 0 &&
      this.cartItems.every((item) => this.selectedIds.has(item.cartItemId))
    );
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.selectedIds.clear();
    } else {
      this.cartItems.forEach((item) => this.selectedIds.add(item.cartItemId));
    }

    this.cdr.detectChanges();
  }

  // ================= CART: OPEN / CLOSE =================
  openCart() {
    if (!this.currentUser) {
      this.showToast('warning', 'Login required', 'Please log in to view your cart.');
      return;
    }

    this.showCart = true;
    this.loadMyCart(false);
  }

  closeCart() {
    this.showCart = false;
    this.cartError = null;
  }

  loadMyCart(resetSelected: boolean = false) {
    this.cartLoading = true;
    this.cartError = null;

    this.cartService.getMyCart().subscribe({
      next: (items) => {
        this.cartItems = items;
        this.cartLoading = false;

        if (resetSelected) {
          this.selectedIds.clear();
        } else {
          const existingIds = new Set(items.map((item) => item.cartItemId));

          this.selectedIds.forEach((id) => {
            if (!existingIds.has(id)) {
              this.selectedIds.delete(id);
            }
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

  // ================= CART: INCREASE =================
  increaseQty(item: CartItemDTO) {
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

        this.showToast('error', 'Cannot increase quantity', msg);
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: DECREASE =================
  decreaseQty(item: CartItemDTO) {
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

        this.showToast('error', 'Error', msg);
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: REMOVE =================
  removeItem(item: CartItemDTO) {
    this.cartLoading = true;

    this.cartService.deleteCartItem(item.cartItemId).subscribe({
      next: () => {
        this.selectedIds.delete(item.cartItemId);
        this.loadMyCart(false);
        this.showToast('success', 'Item removed');
      },
      error: (err) => {
        this.cartLoading = false;

        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Failed to remove item.';

        this.showToast('error', 'Error', msg);
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: CLEAR =================
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
        this.showToast('success', 'Cart cleared');
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.showToast('error', 'Error', 'Failed to clear cart.');
      });
  }

  // ================= CART: CHECKOUT =================
  async checkout() {
    if (this.selectedIds.size === 0) {
      this.showToast(
        'warning',
        'No items selected',
        'Please select at least one item to checkout.',
      );
      return;
    }

    const totalStr = this.selectedTotal.toLocaleString('vi-VN');

    const ok = await this.openConfirm(
      'Confirm checkout',
      `Checkout ${this.selectedCount} item(s) for a total of ${totalStr}đ?`,
      'Checkout',
      'Cancel',
    );

    if (!ok) return;

    const ids = Array.from(this.selectedIds);

    this.cartService.checkout(ids).subscribe({
      next: () => {
        this.showToast('success', 'Order placed! 🎉', 'Your order has been placed successfully.');

        this.selectedIds.clear();
        this.loadMyCart(false);

        // Checkout làm thay đổi tồn kho nên clear cache stock.
        this.stockMap.clear();
        this.loadStockForPage();
      },
      error: (err) => {
        const msg = typeof err.error === 'string' && err.error ? err.error : 'Unknown error.';

        this.showToast('error', 'Checkout failed', msg);
      },
    });
  }

  // ================= AUTH =================
  loadCurrentUser() {
    this.accountService.getCurrentUser().subscribe({
      next: (res: any) => {
        this.currentUser = res.username;
        localStorage.setItem('username', res.username);
        this.cdr.detectChanges();
      },
      error: () => this.logout(),
    });
  }

  loadRole() {
    this.accountService.getRole().subscribe({
      next: (res: any) => {
        this.role = res.role;
        localStorage.setItem('role', res.role);
        this.cdr.detectChanges();
      },
    });
  }

  openRegister() {
    this.registerSubmitted = false;
    this.registerForm.reset({ gender: 'MALE' });
    this.showRegister = true;
  }

  openLogin() {
    this.showLogin = true;
  }

  closePopup() {
    this.showRegister = false;
    this.showLogin = false;
  }

  submitRegister() {
    this.registerSubmitted = true;

    if (this.registerForm.invalid) {
      this.showToast('error', 'Form incomplete', 'Please fix the errors before submitting.');
      return;
    }

    const { confirmPassword, ...payload } = this.registerForm.value;

    this.accountService.register(payload).subscribe({
      next: () => {
        this.showToast('success', 'Account created!', 'You can now log in with your credentials.');
        this.closePopup();
        this.registerForm.reset({ gender: 'MALE' });
        this.registerSubmitted = false;
      },
      error: (err: any) => {
        const msg = typeof err.error === 'string' ? err.error : JSON.stringify(err.error);
        this.showToast('error', 'Registration failed', msg);
      },
    });
  }

  submitLogin() {
    if (this.loginForm.invalid) return;

    this.accountService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        this.token = res.token;
        localStorage.setItem('token', res.token);

        const username = this.loginForm.value.username;
        this.currentUser = username;
        localStorage.setItem('username', username);

        if (res.role) {
          this.role = res.role;
          localStorage.setItem('role', res.role);
        } else {
          this.loadRole();
        }

        this.loadCurrentUser();
        this.cdr.detectChanges();
        this.closePopup();

        this.showToast('success', `Welcome back, ${username}!`);
      },
      error: () => {
        this.showToast('error', 'Login failed', 'Invalid username or password.');
      },
    });
  }

  logout() {
    this.currentUser = null;
    this.role = null;
    this.token = null;
    this.cartItems = [];

    this.selectedIds.clear();
    this.stockMap.clear();

    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');

    this.showToast('info', 'Logged out', 'See you next time!');
  }

  isUser(): boolean {
    return this.role === 'ROLE_USER' || this.role === 'USER';
  }

  isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN';
  }

  isAdminOrUser(): boolean {
    return ['ROLE_ADMIN', 'ADMIN', 'ROLE_USER', 'USER'].includes(this.role || '');
  }
}
