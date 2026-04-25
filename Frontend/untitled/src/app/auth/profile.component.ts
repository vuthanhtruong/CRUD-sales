import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DetailAccountService, ProfileDTO } from '../services/detail-account.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
})
export class ProfileComponent implements OnInit {
  profileForm: any;
  loading = true;
  submitting = false;
  isEdit = false;
  submitted = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private detailAccountService: DetailAccountService,
    private cdr: ChangeDetectorRef,
  ) {
    this.profileForm = this.fb.group({
      username: [{ value: '', disabled: true }],
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      phone: ['', [Validators.required, Validators.pattern('^(0|\\+84)[0-9]{9}$')]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      address: ['', [Validators.required, Validators.maxLength(255)]],
      gender: ['MALE', Validators.required],
      birthday: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  // ─── Field helpers (giống register) ──────────────────────────────────────
  f(name: string) {
    return this.profileForm.get(name);
  }

  isInvalid(name: string): boolean {
    const ctrl = this.f(name);
    return ctrl && (ctrl.dirty || ctrl.touched || this.submitted) && ctrl.invalid;
  }

  fieldError(name: string): string {
    const ctrl = this.f(name);
    if (!ctrl || !ctrl.errors) return '';
    if (ctrl.errors['required']) return 'This field is required.';
    if (ctrl.errors['minlength'])
      return `Minimum ${ctrl.errors['minlength'].requiredLength} characters.`;
    if (ctrl.errors['maxlength'])
      return `Maximum ${ctrl.errors['maxlength'].requiredLength} characters.`;
    if (ctrl.errors['pattern']) {
      if (name === 'phone') return 'Phone must be Vietnam format (0xxxxxxxxx or +84xxxxxxxxx).';
    }
    return 'Invalid value.';
  }

  // ─── Load ─────────────────────────────────────────────────────────────────
  loadProfile() {
    this.loading = true;
    this.detailAccountService.getMe().subscribe({
      next: (res: ProfileDTO) => {
        this.profileForm.patchValue({
          username: res.username,
          firstName: res.firstName,
          lastName: res.lastName,
          phone: res.phone,
          email: res.email,
          address: res.address,
          gender: res.gender,
          birthday: res.birthday,
        });
        this.loading = false;
        this.setFormDisabled();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  setFormDisabled() {
    ['firstName', 'lastName', 'phone', 'email', 'address', 'gender', 'birthday'].forEach((f) =>
      this.profileForm.get(f)?.disable(),
    );
  }

  setFormEnabled() {
    ['firstName', 'lastName', 'phone', 'email', 'address', 'gender', 'birthday'].forEach((f) =>
      this.profileForm.get(f)?.enable(),
    );
  }

  enableEdit() {
    this.isEdit = true;
    this.submitted = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.setFormEnabled();
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.isEdit = false;
    this.submitted = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.profileForm.markAsPristine();
    this.profileForm.markAsUntouched();
    this.setFormDisabled();
    this.loadProfile();
  }

  submit() {
    this.submitted = true;
    this.cdr.detectChanges();

    if (this.profileForm.invalid) {
      // Touch all fields để hiện hết lỗi
      Object.keys(this.profileForm.controls).forEach((key) => {
        this.profileForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const raw = this.profileForm.getRawValue();
    const dto: ProfileDTO = {
      username: raw.username,
      firstName: raw.firstName,
      lastName: raw.lastName,
      phone: raw.phone,
      email: raw.email,
      address: raw.address,
      gender: raw.gender,
      birthday: raw.birthday,
    };

    this.detailAccountService.updateAccount(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.isEdit = false;
        this.submitted = false;
        this.successMessage = 'Profile updated successfully!';
        this.profileForm.markAsPristine();
        this.profileForm.markAsUntouched();
        this.setFormDisabled();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.submitting = false;
        const msg =
          typeof err.error === 'string' && err.error
            ? err.error
            : 'Update failed. Please try again.';
        this.errorMessage = msg;
        this.cdr.detectChanges();
      },
    });
  }
}
