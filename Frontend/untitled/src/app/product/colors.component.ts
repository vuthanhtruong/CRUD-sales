import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ColorService, ColorDTO } from '../services/color.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-colors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './colors.component.html',
  styleUrls: ['./colors.component.css']
})
export class ColorsComponent implements OnInit {

  colors: ColorDTO[] = [];

  form: ColorDTO = {
    id: '',
    name: ''
  };

  isEdit = false;
  idExists = false;

  errors: any = {};

  constructor(
    private colorService: ColorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadColors();
  }
  
  onIdChange() {
    this.errors.id = '';
    this.idExists = false;

    const id = this.form.id?.trim();

    if (!id) {
      this.errors.id = 'Color ID is required';
      return;
    }

    if (id.length < 1 || id.length > 20) {
      this.errors.id = 'Color ID must be 1–20 characters';
      return;
    }

    if (this.isEdit) return;

    this.colorService.getById(id).pipe(
      catchError(() => of(null))
    ).subscribe(res => {
      if (res) this.idExists = true;
      this.cdr.detectChanges();
    });
  }

  onNameChange() {
    this.errors.name = '';

    const name = this.form.name?.trim();

    if (!name) {
      this.errors.name = 'Color name is required';
      return;
    }

    if (name.length < 1 || name.length > 50) {
      this.errors.name = 'Color name must be 1–50 characters';
    }
  }

  save() {

    this.onIdChange();
    this.onNameChange();

    if (this.errors.id || this.errors.name || this.idExists) return;

    const req = this.isEdit
      ? this.colorService.update(this.form.id, this.form)
      : this.colorService.create(this.form);

    req.subscribe({
      next: () => {
        this.reset();
        this.loadColors();
      },
      error: (err) => console.error(err)
    });
  }

  loadColors() {
    this.colorService.getAll().subscribe(data => {
      this.colors = data;
      this.cdr.detectChanges();
    });
  }

  edit(c: ColorDTO) {
    this.form = { ...c };
    this.isEdit = true;
    this.errors = {};
    this.idExists = false;
  }

  delete(id: string) {
    this.colorService.delete(id).subscribe(() => {
      this.loadColors();
    });
  }

  reset() {
    this.form = { id: '', name: '' };
    this.isEdit = false;
    this.errors = {};
    this.idExists = false;
    this.cdr.detectChanges();
  }
}
