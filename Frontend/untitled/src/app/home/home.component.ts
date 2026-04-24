import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
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
    { label: '100K - 300K', min: 100000, max: 300000 },
    { label: '300K - 500K', min: 300000, max: 500000 },
    { label: 'Above 500K', min: 500000, max: null },
  ];

  // ================= CART =================
  showCart = false;
  cartItems: CartItemDTO[] = [];
  cartLoading = false;
  cartError: string | null = null;
  selectedIds: Set<string> = new Set();

  // ================= STOCK =================
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

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private productUserService: ProductUserService,
    private cartService: CartService,
    private productVariantService: ProductVariantService,
    private cdr: ChangeDetectorRef,
  ) {
    this.registerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phone: ['', Validators.required],
      address: ['', Validators.required],
      email: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', Validators.required],
      gender: ['MALE'],
      birthday: [''],
    });

    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });
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
          this.loadStockForPage();
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err),
      });
  }

  onSearchInput() {
    this.searchSubject.next(this.keyword);
  }
  onSearchClick() {
    this.loadProducts();
  }

  // Tương tự cho Add to Cart, Register...

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
      const pid = product.productId;
      if (this.stockMap.has(pid)) continue;
      this.stockLoading.add(pid);
      this.productVariantService.findByProduct(pid).subscribe({
        next: (variants) => {
          const total = variants.reduce((sum, v) => sum + (v.quantity ?? 0), 0);
          this.stockMap.set(pid, total);
          this.stockLoading.delete(pid);
          this.cdr.detectChanges();
        },
        error: () => {
          this.stockMap.set(pid, 0);
          this.stockLoading.delete(pid);
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

  // ================= QUICK-ADD MODAL =================
  openQuickAdd(product: ProductUserDTO) {
    if (!this.currentUser) {
      alert('Vui lòng đăng nhập để thêm vào giỏ hàng.');
      return;
    }
    if (this.isOutOfStock(product.productId)) {
      alert('Sản phẩm này đã hết hàng.');
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

    const pid = product.productId;
    let done = 0;
    const check = () => {
      if (++done === 3) {
        this.quickAddLoading = false;
        this.cdr.detectChanges();
      }
    };

    this.productUserService.getSizesByProduct(pid).subscribe({
      next: (s) => {
        this.quickAddSizes = s;
        check();
      },
      error: () => check(),
    });
    this.productUserService.getColorsByProduct(pid).subscribe({
      next: (c) => {
        this.quickAddColors = c;
        check();
      },
      error: () => check(),
    });
    this.productVariantService.findByProduct(pid).subscribe({
      next: (v) => {
        this.quickAddVariants = v;
        check();
      },
      error: () => check(),
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
    const v = this.quickAddVariants.find(
      (x) =>
        x.sizeId === this.quickAddSelectedSize!.id && x.colorId === this.quickAddSelectedColor!.id,
    );
    return v?.quantity ?? 0;
  }

  get quickAddIsOutOfStock(): boolean {
    const s = this.quickAddCurrentStock;
    return s !== null && s === 0;
  }

  quickIncreaseQty() {
    const max = this.quickAddCurrentStock;
    if (max !== null && this.quickAddQuantity >= max) {
      this.showQuickToast(`Chỉ còn ${max} sản phẩm trong kho.`, 'error');
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
      this.showQuickToast('Vui lòng chọn size.', 'error');
      return;
    }
    if (!this.quickAddSelectedColor) {
      this.showQuickToast('Vui lòng chọn màu.', 'error');
      return;
    }
    if (this.quickAddIsOutOfStock) {
      this.showQuickToast('Hết hàng.', 'error');
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
        this.showQuickToast('Đã thêm vào giỏ hàng! 🛒', 'success');
        // Refresh stock for this product
        const pid = this.quickAddProduct!.productId;
        this.stockMap.delete(pid);
        this.loadStockForPage();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.quickAddAdding = false;
        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Thêm vào giỏ thất bại.';
        this.showQuickToast(msg, 'error');
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: COMPUTED =================
  get cartTotal(): number {
    return this.cartItems.reduce((sum, i) => sum + i.subtotal, 0);
  }
  get cartCount(): number {
    return this.cartItems.reduce((sum, i) => sum + i.quantity, 0);
  }
  get selectedTotal(): number {
    return this.cartItems
      .filter((i) => this.selectedIds.has(i.cartItemId))
      .reduce((sum, i) => sum + i.subtotal, 0);
  }
  get selectedCount(): number {
    return this.selectedIds.size;
  }

  // ================= CART: CHECKBOX =================
  isSelected(id: string): boolean {
    return this.selectedIds.has(id);
  }
  toggleSelect(id: string): void {
    if (this.selectedIds.has(id)) this.selectedIds.delete(id);
    else this.selectedIds.add(id);
    this.cdr.detectChanges();
  }
  isAllSelected(): boolean {
    return (
      this.cartItems.length > 0 && this.cartItems.every((i) => this.selectedIds.has(i.cartItemId))
    );
  }
  toggleSelectAll(): void {
    if (this.isAllSelected()) this.selectedIds.clear();
    else this.cartItems.forEach((i) => this.selectedIds.add(i.cartItemId));
    this.cdr.detectChanges();
  }

  // ================= CART: OPEN / CLOSE =================
  openCart() {
    if (!this.currentUser) {
      alert('Please login to view your cart.');
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
          const existingIds = new Set(items.map((i) => i.cartItemId));
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

  // ================= CART: INCREASE =================
  increaseQty(item: CartItemDTO) {
    this.cartLoading = true;
    this.cartService.increaseQuantity(item.cartItemId, 1).subscribe({
      next: () => this.loadMyCart(false),
      error: (err) => {
        this.cartLoading = false;
        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Không đủ hàng trong kho';
        alert(msg);
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: DECREASE =================
  decreaseQty(item: CartItemDTO) {
    this.cartLoading = true;
    this.cartService.decreaseQuantity(item.cartItemId, 1).subscribe({
      next: () => this.loadMyCart(false),
      error: (err) => {
        this.cartLoading = false;
        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Lỗi khi giảm số lượng';
        alert(msg);
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
      },
      error: (err) => {
        this.cartLoading = false;
        const msg =
          typeof err.error === 'string' && err.error ? err.error : 'Failed to remove item.';
        alert(msg);
        this.cdr.detectChanges();
      },
    });
  }

  // ================= CART: CLEAR =================
  clearCart() {
    if (!confirm('Xóa toàn bộ giỏ hàng?')) return;
    const deletes = this.cartItems.map((i) =>
      this.cartService.deleteCartItem(i.cartItemId).toPromise(),
    );
    Promise.all(deletes)
      .then(() => {
        this.cartItems = [];
        this.selectedIds.clear();
        this.cdr.detectChanges();
      })
      .catch(() => alert('Failed to clear cart.'));
  }

  // ================= CART: CHECKOUT =================
  checkout() {
    if (this.selectedIds.size === 0) {
      alert('Vui lòng chọn ít nhất một sản phẩm để thanh toán.');
      return;
    }
    const totalStr = this.selectedTotal.toLocaleString('vi-VN');
    if (!confirm(`Thanh toán ${this.selectedCount} sản phẩm — tổng ${totalStr}đ?`)) return;
    const ids = Array.from(this.selectedIds);
    this.cartService.checkout(ids).subscribe({
      next: () => {
        alert('Thanh toán thành công! 🎉');
        this.selectedIds.clear();
        this.loadMyCart(false);
        this.stockMap.clear();
        this.loadStockForPage();
      },
      error: (err) => {
        const msg = typeof err.error === 'string' && err.error ? err.error : 'Lỗi không xác định';
        alert('Thanh toán thất bại: ' + msg);
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
    if (this.registerForm.invalid) return;
    this.accountService.register(this.registerForm.value).subscribe({
      next: () => {
        alert('Register success');
        this.closePopup();
        this.registerForm.reset({ gender: 'MALE' });
      },
      error: (err: any) => alert(JSON.stringify(err.error)),
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
        alert('Login success');
      },
      error: () => alert('Login failed'),
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
  }

  isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN';
  }
  isAdminOrUser(): boolean {
    return ['ROLE_ADMIN', 'ADMIN', 'ROLE_USER', 'USER'].includes(this.role || '');
  }
}
