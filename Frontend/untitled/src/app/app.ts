import { CommonModule } from '@angular/common';
import { Component, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { forkJoin } from 'rxjs';
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

  private readonly usernamePattern = /^[a-zA-Z0-9_]+$/;
  private readonly passwordSpecialPattern = /^(?=.*[^A-Za-z0-9]).+$/;
  private readonly minAge = 13;
  private readonly maxAge = 120;
  protected readonly birthdayMinDate = this.toDateInputValue(this.addYears(new Date(), -this.maxAge));
  protected readonly birthdayMaxDate = this.toDateInputValue(this.addYears(new Date(), -this.minAge));

  currentUser: string | null = null;
  role: string | null = null;
  token: string | null = null;

  showLogin = false;
  showRegister = false;
  showForgot = false;
  showReset = false;
  showLoginPassword = false;
  showRegisterPassword = false;
  showRegisterConfirmPassword = false;
  loginSubmitted = false;
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
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(20), Validators.pattern(this.usernamePattern)]],
      password: ['', Validators.required],
    });
    this.forgotForm = this.fb.group({ email: ['', [Validators.required, Validators.email]] });
    this.resetForm = this.fb.group({
      token: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6), Validators.pattern(this.passwordSpecialPattern)]],
    });
    this.registerForm = this.fb.group(
      {
        firstName: ['', [Validators.required, Validators.minLength(2)]],
        lastName: ['', [Validators.required, Validators.minLength(2)]],
        phone: ['', [Validators.required, Validators.pattern(/^(0|\+84)[0-9]{9}$/)]],
        email: ['', [Validators.required, Validators.email]],
        address: ['', Validators.required],
        username: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(20), Validators.pattern(this.usernamePattern)]],
        password: ['', [Validators.required, Validators.minLength(6), Validators.pattern(this.passwordSpecialPattern)]],
        confirmPassword: ['', Validators.required],
        gender: ['MALE'],
        birthday: ['', [Validators.required, this.birthdayValidator()]],
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
      this.loadAuthenticatedContext(false);
    }
  }

  refreshAuthState() { this.token = localStorage.getItem('token'); this.role = localStorage.getItem('role'); this.currentUser = localStorage.getItem('username'); }
  passwordMatchValidator(group: AbstractControl): ValidationErrors | null { const pw = group.get('password')?.value; const cpw = group.get('confirmPassword')?.value; return pw && cpw && pw !== cpw ? { passwordMismatch: true } : null; }

  private addYears(date: Date, years: number): Date {
    const copy = new Date(date);
    copy.setFullYear(copy.getFullYear() + years);
    return copy;
  }

  private toDateInputValue(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private birthdayValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (!value) return null;

      const birthday = new Date(`${value}T00:00:00`);
      if (Number.isNaN(birthday.getTime())) return { invalidDate: true };

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const youngestAllowed = this.addYears(today, -this.minAge);
      const oldestAllowed = this.addYears(today, -this.maxAge);

      if (birthday > today) return { futureDate: true };
      if (birthday > youngestAllowed) return { ageTooYoung: { minAge: this.minAge } };
      if (birthday < oldestAllowed) return { ageTooOld: { maxAge: this.maxAge } };

      return null;
    };
  }

  getLoginField(name: string) { return this.loginForm.get(name); }
  loginFieldInvalid(name: string): boolean { const ctrl = this.getLoginField(name); return !!(ctrl && (ctrl.dirty || ctrl.touched || this.loginSubmitted) && ctrl.invalid); }
  loginFieldError(name: string): string {
    const ctrl = this.getLoginField(name);
    if (!ctrl || !ctrl.errors) return '';
    if (ctrl.errors['required']) return 'This field is required.';
    if (ctrl.errors['minlength']) return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    if (ctrl.errors['maxlength']) return `Maximum ${ctrl.errors['maxlength'].requiredLength} characters.`;
    if (ctrl.errors['pattern']) return 'Only letters, numbers and underscores.';
    return 'Invalid value.';
  }

  getRegField(name: string) { return this.registerForm.get(name); }
  regFieldInvalid(name: string): boolean { const ctrl = this.getRegField(name); return !!(ctrl && (ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.invalid); }
  regFieldError(name: string): string {
    const ctrl = this.getRegField(name);
    if (!ctrl || !ctrl.errors) return '';
    if (ctrl.errors['required']) return 'This field is required.';
    if (ctrl.errors['minlength']) return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    if (ctrl.errors['maxlength']) return `Maximum ${ctrl.errors['maxlength'].requiredLength} characters.`;
    if (ctrl.errors['email']) return 'Enter a valid email address.';
    if (ctrl.errors['invalidDate']) return 'Enter a valid birthday.';
    if (ctrl.errors['futureDate']) return 'Birthday cannot be in the future.';
    if (ctrl.errors['ageTooYoung']) return `You must be at least ${this.minAge} years old.`;
    if (ctrl.errors['ageTooOld']) return `Birthday cannot be more than ${this.maxAge} years ago.`;
    if (ctrl.errors['duplicate']) {
      if (name === 'username') return 'Username already exists.';
      if (name === 'email') return 'Email already exists.';
      if (name === 'phone') return 'Phone already exists.';
      return 'Value already exists.';
    }
    if (ctrl.errors['pattern']) {
      if (name === 'phone') return 'Phone must use Vietnam format.';
      if (name === 'password') return 'Password must include at least one special character.';
      return 'Only letters, numbers and underscores.';
    }
    return 'Invalid value.';
  }
  get confirmPasswordError(): string { const ctrl = this.getRegField('confirmPassword'); if (!ctrl) return ''; if ((ctrl.dirty || ctrl.touched || this.registerSubmitted) && ctrl.errors?.['required']) return 'Please confirm your password.'; if ((ctrl.dirty || ctrl.touched || this.registerSubmitted) && this.registerForm.errors?.['passwordMismatch']) return 'Passwords do not match.'; return ''; }

  openLogin() { this.closeMobileMenu(); this.showRegister = false; this.showForgot = false; this.showReset = false; this.loginSubmitted = false; this.showLoginPassword = false; this.showLogin = true; }
  openRegister() { this.closeMobileMenu(); this.showLogin = false; this.showForgot = false; this.showReset = false; this.registerSubmitted = false; this.showRegisterPassword = false; this.showRegisterConfirmPassword = false; this.registerForm.reset({ gender: 'MALE' }); this.showRegister = true; }
  openForgot() { this.showLogin = false; this.showRegister = false; this.showReset = false; this.showForgot = true; }
  closeAuthPopup() { this.showLogin = false; this.showRegister = false; this.showForgot = false; this.showReset = false; }

  submitLogin() {
    this.loginSubmitted = true;
    if (this.loginForm.invalid) { this.showPopup('error', 'Form incomplete', 'Please enter a valid username and password.'); return; }
    this.accountService.login(this.loginForm.value).subscribe({
      next: (res) => {
        if (!res?.token) { this.showPopup('error', 'Login failed', 'Server did not return an access token.'); return; }
        this.token = res.token;
        localStorage.setItem('token', res.token);
        this.loadAuthenticatedContext(true);
      },
      error: (err) => this.showPopup('error', 'Login failed', err?.error?.message || 'Invalid username or password.'),
    });
  }

  private loadAuthenticatedContext(showWelcome: boolean) {
    forkJoin({
      user: this.accountService.getCurrentUser(),
      role: this.accountService.getRole(),
    }).subscribe({
      next: ({ user, role }) => {
        this.currentUser = user.username;
        this.role = role.role;
        localStorage.setItem('username', this.currentUser || '');
        localStorage.setItem('role', this.role || '');
        if (showWelcome) {
          this.loginSubmitted = false;
          this.closeAuthPopup();
          this.showPopup('success', 'Welcome back', `Signed in as ${this.currentUser}.`);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.logout(false);
        if (showWelcome) this.showPopup('error', 'Login failed', 'Could not load your account context. Please sign in again.');
      },
    });
  }

  private extractApiError(err: any, fallback: string): string {
    if (typeof err?.error === 'string') return err.error;
    if (err?.error?.errors) {
      const firstError = Object.values(err.error.errors)[0];
      if (typeof firstError === 'string') return firstError;
    }
    return err?.error?.message || fallback;
  }

  submitRegister() {
    this.registerSubmitted = true;
    if (this.registerForm.invalid) { this.showPopup('error', 'Form incomplete', 'Please fix the highlighted fields.'); return; }
    const { confirmPassword, ...payload } = this.registerForm.value;

    forkJoin({
      usernameExists: this.accountService.checkUsername(payload.username),
      emailExists: this.accountService.checkEmail(payload.email),
      phoneExists: this.accountService.checkPhone(payload.phone),
    }).subscribe({
      next: ({ usernameExists, emailExists, phoneExists }) => {
        const duplicateMessages: string[] = [];
        if (usernameExists) { this.getRegField('username')?.setErrors({ duplicate: true }); duplicateMessages.push('username'); }
        if (emailExists) { this.getRegField('email')?.setErrors({ duplicate: true }); duplicateMessages.push('email'); }
        if (phoneExists) { this.getRegField('phone')?.setErrors({ duplicate: true }); duplicateMessages.push('phone'); }

        if (duplicateMessages.length > 0) {
          this.showPopup('error', 'Registration failed', `This ${duplicateMessages.join(', ')} is already in use.`);
          return;
        }

        this.accountService.register(payload).subscribe({
          next: () => { this.closeAuthPopup(); this.registerForm.reset({ gender: 'MALE' }); this.registerSubmitted = false; this.showPopup('success', 'Account created', 'Your account has been created. You can now sign in.'); },
          error: (err: any) => this.showPopup('error', 'Registration failed', this.extractApiError(err, 'Please check your information.')),
        });
      },
      error: () => this.showPopup('error', 'Registration failed', 'Could not validate your account information. Please try again.'),
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
    if (this.resetForm.invalid) { this.showPopup('error', 'Form incomplete', 'Password must be at least 6 characters and include one special character.'); return; }
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
