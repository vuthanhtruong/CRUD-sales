import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductTypeService, ProductType } from '../services/product-type.service';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-product-type',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-type.component.html',
  styleUrls: ['./product-type.component.css']
})
export class ProductTypeComponent implements OnInit {

  productTypes: ProductType[] = [];

  form: ProductType = {
    id: '',
    typeName: ''
  };

  errors: any = {};
  idExists = false;
  isEdit = false;
  loading = false;
  errorMsg = '';

  constructor(
    private service: ProductTypeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }


  onIdChange() {
    this.errors.id = '';
    this.idExists = false;

    const id = this.form.id?.trim();

    if (!id) {
      this.errors.id = 'ID is required';
      return;
    }

    if (id.length < 2 || id.length > 20) {
      this.errors.id = 'ID must be 2–20 characters';
      return;
    }

    if (this.isEdit) return;

    this.service.getById(id).pipe(
      catchError(err => of(null))
    ).subscribe(res => {
      if (res) {
        this.idExists = true;
      }
      this.cdr.detectChanges();
    });
  }


  onNameChange() {
    this.errors.typeName = '';

    const name = this.form.typeName?.trim();

    if (!name) {
      this.errors.typeName = 'Type name is required';
      return;
    }

    if (name.length < 2 || name.length > 100) {
      this.errors.typeName = 'Type name must be 2–100 characters';
    }
  }

  submit() {

    this.onIdChange();
    this.onNameChange();

    if (
      this.errors.id ||
      this.errors.typeName ||
      this.idExists
    ) {
      return;
    }

    const req = this.isEdit
      ? this.service.update(this.form.id, this.form)
      : this.service.create(this.form);

    req.subscribe({
      next: () => {
        this.resetForm();
        this.loadData();
      },
      error: () => {
        this.errorMsg = 'Save failed';
        this.cdr.detectChanges();
      }
    });
  }


  loadData() {
    this.loading = true;
    this.errorMsg = '';

    this.service.getAll().subscribe({
      next: (res) => {
        this.productTypes = res || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMsg = 'Cannot load data';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  edit(item: ProductType) {
    this.form = { ...item };
    this.isEdit = true;
    this.idExists = false;
    this.errors = {};
  }

  delete(id: string) {
    if (confirm('Delete this item?')) {
      this.service.delete(id).subscribe(() => {
        this.loadData();
      });
    }
  }

  resetForm() {
    this.form = { id: '', typeName: '' };
    this.errors = {};
    this.idExists = false;
    this.isEdit = false;
    this.cdr.detectChanges();
  }

  
}
