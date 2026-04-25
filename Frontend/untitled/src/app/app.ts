import { CommonModule } from '@angular/common';
import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
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
  protected readonly title = signal('Aster Store');
  protected readonly currentYear = new Date().getFullYear();
  protected isMobileMenuOpen = false;

  currentUser: string | null = null;
  role: string | null = null;
  token: string | null = null;

  showLogin = false;
  showRegister = false;
  showForgot = false;
  showReset = false;
  registerSubmitted = false;
  forgotSubmitting = false;
  resetSubmitting = false;

  popup: { type: PopupType; title: string; message: string } | null = null;

  loginForm: any;
  registerForm: any;
  forgotForm: any;
  resetForm: any;

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {
    this.loginForm = this.fb.group({ username: ['', Validators.required], password: ['', Validators.required] });
    this.forgotForm = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
    this.resetForm = this.fb.group({ token: ['', Validators.required], newPassword: ['', [Validators.required, Validators.minLength(6)]] });
    this.registerForm = this.fb.group(
      {
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        phone: ['', [Validators.required, Validators.pattern(/^(0|\+84)[0-9]{9}$/)]],
        email: ['', [Validators.required, Validators.email]],
        address: ['', Validators.required],
        username: ['', [Validators.required, Validators.minLength(4), Validators.pattern(/^[a-zA-Z0-9_]+$/)]],
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
    const token = new URLSearchParams(window.location.search).get('token');
    if (token) {
      this.resetForm.patchValue({ token });
      this.showReset = true;
    }
    if (this.token) {
      this.accountService.getCurrentUser().subscribe({
        next: (res: any) => { this.currentUser = res.username; localStorage.setItem('username', res.username); this.cdr.detectChanges(); },
        error: () => this.logout(false),
      });
      this.accountService.getRole().subscribe({ next: (res) => { this.role = res.role; localStorage.setItem('role', res.role); this.cdr.detectChanges(); } });
    }
  }

  refreshAuthState() { this.token = localStorage.getItem('token'); this.role = localStorage.getItem('role'); this.currentUser = localStorage.getItem('username'); }
  passwordMatchValidator(group: AbstractControl): ValidationErrors | null { const pw = group.get('password')?.value; const cpw = group.get('confirmPassword')?.value; return pw && cpw && pw !== cpw ? { passwordMismatch: true } : null; }
  getRegField(name: string) { return this.registerForm.get(name); }
  regFieldInvalid(name: string): boolean { const ctrl = this.getRegField(name); return !!(ctrl && (ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.invalid); }
  regFieldError(name: string): string { const ctrl = this.getRegField(name); if (!ctrl || !ctrl.errors) return ''; if (ctrl.errors['required']) return 'This field is required.'; if (ctrl.errors['minlength']) return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`; if (ctrl.errors['email']) return 'Enter a valid email address.'; if (ctrl.errors['pattern']) return name === 'phone' ? 'Phone must use Vietnam format.' : 'Only letters, numbers and underscores.'; return 'Invalid value.'; }
  get confirmPasswordError(): string { const ctrl = this.getRegField('confirmPassword'); if (!ctrl) return ''; if ((ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.errors?.['required']) return 'Please confirm your password.'; if ((ctrl.dirty || ctrl.touched || this.registerSubmitted) && this.registerForm.errors?.['passwordMismatch']) return 'Passwords do not match.'; return ''; }

  openLogin() { this.closeMobileMenu(); this.showRegister = false; this.showForgot = false; this.showReset = false; this.showLogin = true; }
  openRegister() { this.closeMobileMenu(); this.showLogin = false; this.showForgot = false; this.showReset = false; this.registerSubmitted = false; this.registerForm.reset({ gender: 'MALE' }); this.showRegister = true; }
  openForgot() { this.showLogin = false; this.showRegister = false; this.showReset = false; this.showForgot = true; }
  closeAuthPopup() { this.showLogin = false; this.showRegister = false; this.showForgot = false; this.showReset = false; }

  submitLogin() {
    if (this.loginForm.invalid) return;
    this.accountService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        this.token = res.token; localStorage.setItem('token', res.token);
        this.currentUser = res.username || this.loginForm.value.username; localStorage.setItem('username', this.currentUser!);
        if (res.role) { this.role = res.role; localStorage.setItem('role', res.role); }
        this.closeAuthPopup(); this.showPopup('success', 'Welcome back', `Signed in as ${this.currentUser}.`); this.cdr.detectChanges();
      },
      error: () => this.showPopup('error', 'Login failed', 'Invalid username or password.'),
    });
  }

  submitRegister() {
    this.registerSubmitted = true;
    if (this.registerForm.invalid) { this.showPopup('error', 'Form incomplete', 'Please fix the highlighted fields.'); return; }
    const { confirmPassword, ...payload } = this.registerForm.value;
    this.accountService.register(payload).subscribe({
      next: () => { this.closeAuthPopup(); this.registerForm.reset({ gender: 'MALE' }); this.registerSubmitted = false; this.showPopup('success', 'Account created', 'Your account has been created. You can now sign in.'); },
      error: (err: any) => this.showPopup('error', 'Registration failed', typeof err.error === 'string' ? err.error : (err.error?.message || 'Please check your information.')),
    });
  }

  submitForgot() {
    if (this.forgotForm.invalid) return;
    this.forgotSubmitting = true;
    this.accountService.forgotPassword(this.forgotForm.value.email).subscribe({
      next: () => { this.forgotSubmitting = false; this.closeAuthPopup(); this.showPopup('success', 'Check your email', 'If the email exists, a reset link has been sent.'); },
      error: () => { this.forgotSubmitting = false; this.showPopup('error', 'Request failed', 'Could not send reset email. Check Gmail SMTP settings.'); },
    });
  }

  submitReset() {
    if (this.resetForm.invalid) return;
    this.resetSubmitting = true;
    this.accountService.resetPassword(this.resetForm.value.token, this.resetForm.value.newPassword).subscribe({
      next: () => { this.resetSubmitting = false; this.closeAuthPopup(); this.showPopup('success', 'Password updated', 'You can now sign in with your new password.'); this.router.navigate(['/home']); },
      error: (err) => { this.resetSubmitting = false; this.showPopup('error', 'Reset failed', err?.error?.message || 'The reset token is invalid or expired.'); },
    });
  }

  logout(showMessage = true) { this.currentUser = null; this.role = null; this.token = null; localStorage.removeItem('token'); localStorage.removeItem('username'); localStorage.removeItem('role'); this.router.navigate(['/home']); if (showMessage) this.showPopup('info', 'Signed out', 'You have been signed out successfully.'); }
  showPopup(type: PopupType, title: string, message: string) { this.popup = { type, title, message }; this.cdr.detectChanges(); }
  closePopup() { this.popup = null; this.cdr.detectChanges(); }
  isUser(): boolean { return this.role === 'ROLE_USER' || this.role === 'USER'; }
  isAdmin(): boolean { return this.role === 'ROLE_ADMIN' || this.role === 'ADMIN'; }
  isShopper(): boolean { return this.isLoggedIn(); }
  isLoggedIn(): boolean { return !!this.token; }
  get initials(): string { return (this.currentUser || 'G').slice(0, 2).toUpperCase(); }
  toggleMobileMenu() { this.isMobileMenuOpen = !this.isMobileMenuOpen; }
  closeMobileMenu() { this.isMobileMenuOpen = false; }
}
