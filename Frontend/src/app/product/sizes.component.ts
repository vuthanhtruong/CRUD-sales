import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { SizeService, Size } from '../services/size.service';
import { ProductService } from '../services/product.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-sizes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sizes.component.html',
  styleUrls: ['./sizes.component.css'],
})
export class SizesComponent implements OnInit {
  sizes: Size[] = [];

  form: Size = { id: '', name: '' };

  errors: any = {};
  idExists = false;
  isEditMode = false;

  // ================= DELETE CONFIRM =================
  showDeleteConfirm = false;
  deleteTarget: Size | null = null;
  deleteBlocked = false;
  deleteCheckLoading = false;

  constructor(
    private sizeService: SizeService,
    private productService: ProductService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadSizes();
  }

  onIdChange() {
    this.errors.id = '';
    this.idExists = false;

    const id = this.form.id?.trim();
    if (!id) {
      this.errors.id = 'Size ID is required';
      return;
    }
    if (id.length < 1 || id.length > 20) {
      this.errors.id = 'Size ID must be 1–20 characters';
      return;
    }
    if (this.isEditMode) return;

    this.sizeService
      .getById(id)
      .pipe(catchError(() => of(null)))
      .subscribe((res) => {
        if (res) this.idExists = true;
        this.cdr.detectChanges();
      });
  }

  onNameChange() {
    this.errors.name = '';
    const name = this.form.name?.trim();
    if (!name) {
      this.errors.name = 'Size name is required';
      return;
    }
    if (name.length < 1 || name.length > 50) {
      this.errors.name = 'Size name must be 1–50 characters';
    }
  }

  submit() {
    this.onIdChange();
    this.onNameChange();
    if (this.errors.id || this.errors.name || this.idExists) return;

    const req = this.isEditMode
      ? this.sizeService.update(this.form.id, this.form)
      : this.sizeService.create(this.form);

    req.subscribe({
      next: () => {
        this.resetForm();
        this.loadSizes();
      },
      error: (err) => console.error(err),
    });
  }

  loadSizes() {
    this.sizeService.getAll().subscribe({
      next: (data) => {
        this.sizes = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error(err),
    });
  }

  edit(size: Size) {
    this.form = { ...size };
    this.isEditMode = true;
    this.idExists = false;
    this.errors = {};
    this.closeDeleteConfirm();
  }

  // ================= DELETE FLOW =================
  requestDelete(item: Size) {
    this.deleteTarget = item;
    this.deleteBlocked = false;
    this.deleteCheckLoading = true;
    this.showDeleteConfirm = true;
    this.cdr.detectChanges();

    this.productService
      .existsBySize(item.id)
      .pipe(catchError(() => of(false)))
      .subscribe((exists) => {
        this.deleteBlocked = exists;
        this.deleteCheckLoading = false;
        this.cdr.detectChanges();
      });
  }

  confirmDelete() {
    if (!this.deleteTarget || this.deleteBlocked) return;
    this.sizeService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.closeDeleteConfirm();
        this.loadSizes();
      },
      error: () => {
        this.closeDeleteConfirm();
      },
    });
  }

  closeDeleteConfirm() {
    this.showDeleteConfirm = false;
    this.deleteTarget = null;
    this.deleteBlocked = false;
    this.deleteCheckLoading = false;
    this.cdr.detectChanges();
  }

  resetForm() {
    this.form = { id: '', name: '' };
    this.errors = {};
    this.idExists = false;
    this.isEditMode = false;
    this.cdr.detectChanges();
  }
}
