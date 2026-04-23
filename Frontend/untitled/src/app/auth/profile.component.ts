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
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  profileForm: any;
  loading = true;
  submitting = false;
  isEdit = false;
  successMessage = '';
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private detailAccountService: DetailAccountService,
    private cdr: ChangeDetectorRef
  ) {
    this.profileForm = this.fb.group({
      username:  [{ value: '', disabled: true }],
      firstName: ['', [Validators.required, Validators.maxLength(50)]],
      lastName:  ['', [Validators.required, Validators.maxLength(50)]],
      phone:     ['', [Validators.required, Validators.pattern('^(0|\\+84)[0-9]{9}$')]],
      address:   ['', [Validators.required, Validators.maxLength(255)]],
      gender:    ['MALE', Validators.required],
      birthday:  ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.detailAccountService.getMe().subscribe({
      next: (res: ProfileDTO) => {
        this.profileForm.patchValue({
          username:  res.username,
          firstName: res.firstName,
          lastName:  res.lastName,
          phone:     res.phone,
          address:   res.address,
          gender:    res.gender,
          birthday:  res.birthday
        });
        this.loading = false;
        this.setFormDisabled();
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  setFormDisabled() {
    ['firstName', 'lastName', 'phone', 'address', 'gender', 'birthday'].forEach(f =>
      this.profileForm.get(f).disable()
    );
  }

  setFormEnabled() {
    ['firstName', 'lastName', 'phone', 'address', 'gender', 'birthday'].forEach(f =>
      this.profileForm.get(f).enable()
    );
  }

  enableEdit() {
    this.isEdit = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.setFormEnabled();
    this.cdr.detectChanges();
  }

  cancelEdit() {
    this.isEdit = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.setFormDisabled();
    this.loadProfile();
  }

  submit() {
    if (this.profileForm.invalid) return;

    this.submitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const raw = this.profileForm.getRawValue();
    const dto: ProfileDTO = {
      username:  raw.username,
      firstName: raw.firstName,
      lastName:  raw.lastName,
      phone:     raw.phone,
      address:   raw.address,
      gender:    raw.gender,
      birthday:  raw.birthday
    };

    this.detailAccountService.updateAccount(dto).subscribe({
      next: () => {
        this.submitting = false;
        this.isEdit = false;
        this.successMessage = 'Profile updated successfully!';
        this.setFormDisabled();
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.submitting = false;
        this.errorMessage = 'Update failed. Please try again.';
        this.cdr.detectChanges();
      }
    });
  }
}
