import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormsModule } from '@angular/forms';
import { AccountService } from '../services/account.service';
import { RouterModule } from '@angular/router';
import { ProductUserService, ProductUserDTO } from '../services/product-user.service';
import { CartService, CartItemDTO } from '../services/cart.service';
import { Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

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

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private productUserService: ProductUserService,
    private cartService: CartService,
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

    this.searchSubject.pipe(debounceTime(400)).subscribe(() => {
      this.loadProducts();
    });

    this.loadProducts();
  }

  // ================= LOAD PRODUCTS =================
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
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err),
      });
  }

  // ================= SEARCH =================
  onSearchInput() {
    this.searchSubject.next(this.keyword);
  }

  onSearchClick() {
    this.loadProducts();
  }

  // ================= FILTER PRICE =================
  selectPriceRange(range: any) {
    this.selectedMinPrice = range.min;
    this.selectedMaxPrice = range.max;
    this.loadProducts();
  }

  // ================= PAGINATION =================
  updatePagedProducts() {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedProducts = this.products.slice(start, start + this.pageSize);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePagedProducts();
    this.cdr.detectChanges();
  }

  // ================= IMAGE =================
  buildImageSrc(image: string | null): string {
    if (!image) return '';
    if (image.startsWith('data:')) return image;
    return `data:image/jpeg;base64,${image}`;
  }

  // ================= CART =================
  get cartTotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.subtotal, 0);
  }

  get cartCount(): number {
    return this.cartItems.reduce((sum, item) => sum + item.quantity, 0);
  }

  openCart() {
    if (!this.currentUser) {
      alert('Please login to view your cart.');
      return;
    }
    this.showCart = true;
    this.loadMyCart();
  }

  closeCart() {
    this.showCart = false;
    this.cartError = null;
  }

  // ✅ dùng /me thay vì cartId
  loadMyCart() {
    this.cartLoading = true;
    this.cartError = null;
    this.cartService.getMyCart().subscribe({
      next: (items) => {
        this.cartItems = items;
        this.cartLoading = false;
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
    const prev = item.quantity;
    item.quantity += 1;
    item.subtotal = item.quantity * item.price;
    this.cartService.updateCartItem(item).subscribe({
      error: () => {
        item.quantity = prev;
        item.subtotal = item.quantity * item.price;
        this.cdr.detectChanges();
      },
    });
    this.cdr.detectChanges();
  }

  decreaseQty(item: CartItemDTO) {
    if (item.quantity <= 1) {
      this.removeItem(item);
      return;
    }
    const prev = item.quantity;
    item.quantity -= 1;
    item.subtotal = item.quantity * item.price;
    this.cartService.updateCartItem(item).subscribe({
      error: () => {
        item.quantity = prev;
        item.subtotal = item.quantity * item.price;
        this.cdr.detectChanges();
      },
    });
    this.cdr.detectChanges();
  }

  removeItem(item: CartItemDTO) {
    this.cartService.deleteCartItem(item.cartItemId).subscribe({
      next: () => {
        this.cartItems = this.cartItems.filter((i) => i.cartItemId !== item.cartItemId);
        this.cdr.detectChanges();
      },
      error: () => alert('Failed to remove item.'),
    });
  }

  clearCart() {
    if (!confirm('Clear all items from cart?')) return;
    // gọi /me nếu backend có, hoặc dùng cartId từ item đầu tiên
    const firstItem = this.cartItems[0];
    if (!firstItem) return;
    // clear bằng cách xóa từng item vì dùng /me
    const deletes = this.cartItems.map((i) =>
      this.cartService.deleteCartItem(i.cartItemId).toPromise(),
    );
    Promise.all(deletes)
      .then(() => {
        this.cartItems = [];
        this.cdr.detectChanges();
      })
      .catch(() => alert('Failed to clear cart.'));
  }

  addToCart(product: ProductUserDTO) {
    if (!this.currentUser) {
      alert('Please login to add items to cart.');
      return;
    }
    alert('Please go to product detail to select size & color before adding to cart.');
  }

  buyNow(product: ProductUserDTO) {
    if (!this.currentUser) {
      alert('Please login first.');
      return;
    }
    alert(`Redirecting to checkout for: ${product.productName}`);
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
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
  }

  isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN';
  }

  isAdminOrUser(): boolean {
    return (
      this.role === 'ROLE_ADMIN' ||
      this.role === 'ADMIN' ||
      this.role === 'ROLE_USER' ||
      this.role === 'USER'
    );
  }
}
