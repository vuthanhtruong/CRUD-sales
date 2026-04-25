import { CommonModule } from '@angular/common';
import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { AccountService } from './services/account.service';

type PopupType = 'success' | 'error' | 'info';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterOutlet, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  protected readonly title = signal('CRUD Sales');
  protected readonly currentYear = new Date().getFullYear();

  protected isMobileMenuOpen = false;

  currentUser: string | null = null;
  role: string | null = null;
  token: string | null = null;

  showLogin = false;
  showRegister = false;
  registerSubmitted = false;

  popup: { type: PopupType; title: string; message: string } | null = null;

  loginForm: any;
  registerForm: any;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
    });

    this.registerForm = this.fb.group(
      {
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        phone: ['', [Validators.required, Validators.pattern(/^(0|\+84)[0-9]{9}$/)]],
        address: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        username: [
          '',
          [Validators.required, Validators.minLength(4), Validators.pattern(/^[a-zA-Z0-9_]+$/)],
        ],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
        gender: ['MALE'],
        birthday: ['', Validators.required],
      },
      { validators: this.passwordMatchValidator },
    );
  }

  ngOnInit() {
    this.refreshAuthState();

    if (this.token) {
      this.accountService.getCurrentUser().subscribe({
        next: (res: any) => {
          this.currentUser = res.username;
          localStorage.setItem('username', res.username);
          this.cdr.detectChanges();
        },
        error: () => this.logout(false),
      });

      this.accountService.getRole().subscribe({
        next: (res) => {
          this.role = res.role;
          localStorage.setItem('role', res.role);
          this.cdr.detectChanges();
        },
      });
    }
  }

  refreshAuthState() {
    this.token = localStorage.getItem('token');
    this.role = localStorage.getItem('role');
    this.currentUser = localStorage.getItem('username');
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const pw = group.get('password')?.value;
    const cpw = group.get('confirmPassword')?.value;
    return pw && cpw && pw !== cpw ? { passwordMismatch: true } : null;
  }

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
    if (ctrl.errors['minlength']) {
      return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    }
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

  openLogin() {
    this.closeMobileMenu();
    this.showRegister = false;
    this.showLogin = true;
  }

  openRegister() {
    this.closeMobileMenu();
    this.showLogin = false;
    this.registerSubmitted = false;
    this.registerForm.reset({ gender: 'MALE' });
    this.showRegister = true;
  }

  closeAuthPopup() {
    this.showLogin = false;
    this.showRegister = false;
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
        }

        this.accountService.getRole().subscribe({
          next: (roleRes) => {
            this.role = roleRes.role;
            localStorage.setItem('role', roleRes.role);
            this.cdr.detectChanges();
          },
        });

        this.closeAuthPopup();
        this.showPopup('success', 'Welcome back', `You are signed in as ${username}.`);
        this.cdr.detectChanges();
      },
      error: () => {
        this.showPopup('error', 'Login failed', 'Invalid username or password.');
      },
    });
  }

  submitRegister() {
    this.registerSubmitted = true;

    if (this.registerForm.invalid) {
      this.showPopup('error', 'Form incomplete', 'Please fix the highlighted fields before submitting.');
      return;
    }

    const { confirmPassword, ...payload } = this.registerForm.value;

    this.accountService.register(payload).subscribe({
      next: () => {
        this.closeAuthPopup();
        this.registerForm.reset({ gender: 'MALE' });
        this.registerSubmitted = false;
        this.showPopup('success', 'Account created', 'Your account has been created. You can now sign in.');
      },
      error: (err: any) => {
        const msg =
          typeof err.error === 'string'
            ? err.error
            : (err.error?.message || 'Registration failed. Please check your information.');
        this.showPopup('error', 'Registration failed', msg);
      },
    });
  }

  logout(showMessage = true) {
    this.currentUser = null;
    this.role = null;
    this.token = null;

    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');

    this.router.navigate(['/home']);

    if (showMessage) {
      this.showPopup('info', 'Signed out', 'You have been signed out successfully.');
    }
  }

  showPopup(type: PopupType, title: string, message: string) {
    this.popup = { type, title, message };
    this.cdr.detectChanges();
  }

  closePopup() {
    this.popup = null;
    this.cdr.detectChanges();
  }

  isUser(): boolean {
    return this.role === 'ROLE_USER' || this.role === 'USER';
  }

  isAdmin(): boolean {
    return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN';
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu() {
    this.isMobileMenuOpen = false;
  }
}
