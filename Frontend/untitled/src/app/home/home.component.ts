import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  FormsModule
} from '@angular/forms';
import { AccountService } from '../services/account.service';
import { RouterModule } from '@angular/router';
import { ProductUserService, ProductUserDTO } from '../services/product-user.service';
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
  // ================= AUTH =================
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

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private productUserService: ProductUserService,
    private cdr: ChangeDetectorRef,
  ) {
    // ================= REGISTER FORM =================
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

    // ================= LOGIN FORM =================
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

    // debounce search
    this.searchSubject.pipe(debounceTime(400)).subscribe(() => {
      this.loadProducts();
    });

    // load lần đầu
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

  // ================= ACTION =================
  buyNow(product: ProductUserDTO) {
    alert(`Buy now: ${product.productName}`);
  }

  addToCart(product: ProductUserDTO) {
    alert(`Added to cart: ${product.productName}`);
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
      error: (err) => alert(JSON.stringify(err.error)),
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
