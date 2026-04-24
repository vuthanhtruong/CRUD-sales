import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ProductService, ProductDTO, ProductSearchParams } from '../services/product.service';
import { ProductImageService, ProductImageDTO } from '../services/product-image.service';
import { ProductVariantService, ProductVariantDTO } from '../services/product-variant.service';
import { ProductTypeService, ProductType } from '../services/product-type.service';
import { SizeService, Size } from '../services/size.service';
import { ColorService, ColorDTO } from '../services/color.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExportService } from '../services/export.service';

interface ImageRow {
  preview: string;
  imageData?: string;
  contentType?: string;
  isPrimary: boolean;
}

@Component({
  selector: 'app-product',
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css'],
})
export class ProductsComponent implements OnInit {
  protected readonly currentYear = new Date().getFullYear();
  cartCount = 0;
  showProductModal = false;

  // =========================
  // PRODUCT
  // =========================
  products: ProductDTO[] = [];
  statuses: string[] = [];
  productTypes: ProductType[] = [];

  form: ProductDTO = {
    productId: '',
    productName: '',
    status: '',
    productTypeId: '',
    price: undefined,
    description: '',
    images: [],
  };

  isEdit = false;
  idExists = false;
  errors: any = {};

  // =========================
  // SEARCH
  // =========================
  searchParams: ProductSearchParams = {
    keyword: '',
    minPrice: undefined,
    maxPrice: undefined,
    productTypeId: '',
    status: '',
  };
  isSearchMode = false;
  searchError = '';

  // =========================
  // PAGINATION
  // =========================
  currentPage = 1;
  totalPages = 1;
  pageSize = 10;

  // =========================
  // IMAGE ROWS
  // =========================
  imageRows: ImageRow[] = [];
  imageErrors: string[] = [];

  // =========================
  // IMAGE MODAL
  // =========================
  showImageModal = false;
  imageModalProductId = '';
  productImages: ProductImageDTO[] = [];
  selectedImageIds: Set<string> = new Set();
  imageModalLoading = false;
  modalImageRows: ImageRow[] = [];
  modalImageErrors: string[] = [];
  modalUploadSubmitting = false;

  // =========================
  // VARIANT MODAL
  // =========================
  showVariantModal = false;
  selectedProductId = '';
  existingVariants: ProductVariantDTO[] = [];
  newVariantRows: ProductVariantDTO[] = [];
  sizes: Size[] = [];
  colors: ColorDTO[] = [];

  // =========================
  // EXPORT MODAL
  // =========================
  showExportModal = false;
  exportFormat: 'excel' | 'word' | 'pdf' = 'excel';
  exportScope: 'current' | 'all' | number = 'current';
  exportLoading = false;
  exportError = '';
  // danh sách trang để render checkbox
  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }
  selectedPages: Set<number> = new Set();
  exportScopeType: 'current' | 'selected' | 'all' = 'current';

  constructor(
    private productService: ProductService,
    private productImageService: ProductImageService,
    private variantService: ProductVariantService,
    private productTypeService: ProductTypeService,
    private sizeService: SizeService,
    private colorService: ColorService,
    private exportService: ExportService,
    private cdr: ChangeDetectorRef,
  ) {}

  // =========================
  // INIT
  // =========================
  ngOnInit(): void {
    this.loadProductsPaged();
    this.loadStatuses();
    this.loadProductTypes();
    this.loadSizes();
    this.loadColors();
  }

  buildProductImageSrc(image: string | undefined): string {
    if (!image) return '';
    if (image.startsWith('data:')) return image;
    return `data:image/jpeg;base64,${image}`;
  }

  openCart() {
    alert(`Cart items: ${this.cartCount}`);
  }

  trackByProductId(_: number, p: ProductDTO) {
    return p.productId;
  }

  seedFromId(id: string | undefined) {
    const s = (id || '').toString();
    let hash = 0;
    for (let i = 0; i < s.length; i++) hash = ((hash << 5) - hash + s.charCodeAt(i)) | 0;
    return Math.abs(hash % 360);
  }

  resolveTypeName(typeId: string) {
    return this.productTypes.find((t) => t.id === typeId)?.typeName || typeId;
  }

  openCreateModal() {
    this.reset();
    this.showProductModal = true;
  }
  openEditModal(p: ProductDTO) {
    this.edit(p);
    this.showProductModal = true;
  }
  closeProductModal() {
    this.showProductModal = false;
  }

  // =========================
  // LOAD DATA
  // =========================
  loadProductsPaged() {
    forkJoin({
      products: this.productService.findAllPaged(this.currentPage, this.pageSize),
      totalPages: this.productService.countTotalPages(this.pageSize),
    }).subscribe({
      next: (res) => {
        this.products = res.products;
        this.totalPages = res.totalPages;
        if (this.currentPage > this.totalPages) {
          this.currentPage = Math.max(1, this.totalPages);
        }
        this.cdr.detectChanges();
      },
      error: (err) => console.error(err),
    });
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    if (this.isSearchMode) this.doSearch();
    else this.loadProductsPaged();
  }

  changePageSize(size: number) {
    this.pageSize = size;
    this.currentPage = 1;
    if (this.isSearchMode) this.doSearch();
    else this.loadProductsPaged();
  }

  loadStatuses() {
    this.productService.getStatuses().subscribe((res) => {
      this.statuses = res;
      this.cdr.detectChanges();
    });
  }

  loadProductTypes() {
    this.productTypeService.getAll().subscribe((res) => {
      this.productTypes = res;
      this.cdr.detectChanges();
    });
  }

  loadSizes() {
    this.sizeService.getAll().subscribe((res) => {
      this.sizes = res;
      this.cdr.detectChanges();
    });
  }

  loadColors() {
    this.colorService.getAll().subscribe((res) => {
      this.colors = res;
      this.cdr.detectChanges();
    });
  }

  // =========================
  // SEARCH
  // =========================
  doSearch() {
    this.searchError = '';
    const min = this.searchParams.minPrice;
    const max = this.searchParams.maxPrice;
    if (min != null && max != null && min > max) {
      this.searchError = 'Min price cannot be greater than max price';
      return;
    }
    const hasAnyFilter =
      !!this.searchParams.keyword?.trim() ||
      min != null ||
      max != null ||
      !!this.searchParams.productTypeId?.trim() ||
      !!this.searchParams.status?.trim();

    if (!hasAnyFilter) {
      this.resetSearch();
      return;
    }

    this.isSearchMode = true;
    this.productService.searchProducts(this.searchParams).subscribe({
      next: (res) => {
        this.products = res;
        this.totalPages = 1;
        this.currentPage = 1;
        this.cdr.detectChanges();
      },
      error: () => {
        this.searchError = 'Search failed. Please try again.';
        this.cdr.detectChanges();
      },
    });
  }

  resetSearch() {
    this.searchParams = {
      keyword: '',
      minPrice: undefined,
      maxPrice: undefined,
      productTypeId: '',
      status: '',
    };
    this.isSearchMode = false;
    this.searchError = '';
    this.currentPage = 1;
    this.loadProductsPaged();
  }

  // =========================
  // EXPORT MODAL
  // =========================
  openExportModal() {
    this.showExportModal = true;
    this.exportFormat = 'excel';
    this.exportScopeType = 'current';
    this.selectedPages = new Set([this.currentPage]);
    this.exportError = '';
    this.exportLoading = false;
    this.cdr.detectChanges();
  }

  closeExportModal() {
    this.showExportModal = false;
    this.exportError = '';
    this.cdr.detectChanges();
  }

  togglePageSelection(page: number) {
    if (this.selectedPages.has(page)) this.selectedPages.delete(page);
    else this.selectedPages.add(page);
    this.cdr.detectChanges();
  }

  selectAllPages() {
    this.selectedPages = new Set(this.pageNumbers);
    this.cdr.detectChanges();
  }

  clearPageSelection() {
    this.selectedPages = new Set();
    this.cdr.detectChanges();
  }

  async doExport() {
    this.exportError = '';

    if (this.exportScopeType === 'selected' && this.selectedPages.size === 0) {
      this.exportError = 'Please select at least one page.';
      return;
    }

    this.exportLoading = true;
    this.cdr.detectChanges();

    try {
      let data: ProductDTO[] = [];

      if (this.exportScopeType === 'current') {
        // trang hiện tại đang hiển thị
        data = this.products;
      } else if (this.exportScopeType === 'selected') {
        // fetch từng trang được chọn song song
        const pages = Array.from(this.selectedPages).sort((a, b) => a - b);
        const results = await Promise.all(
          pages.map((p) =>
            this.productService
              .findAllPaged(p, this.pageSize)
              .toPromise()
              .then((r) => r ?? []),
          ),
        );
        data = results.flat();
      } else {
        // all
        const allData = await this.productService.findAll().toPromise();
        data = allData ?? [];
      }

      if (this.exportFormat === 'excel') {
        await this.exportService.exportExcel(data);
      } else if (this.exportFormat === 'word') {
        await this.exportService.exportWord(data);
      } else {
        await this.exportService.exportPdf(data);
      }

      this.exportLoading = false;
      this.closeExportModal();
    } catch (err) {
      console.error(err);
      this.exportError = 'Export failed. Please try again.';
      this.exportLoading = false;
      this.cdr.detectChanges();
    }
  }

  // =========================
  // IMAGE MODAL
  // =========================
  openImageModal(productId: string) {
    this.imageModalProductId = productId;
    this.showImageModal = true;
    this.productImages = [];
    this.selectedImageIds = new Set();
    this.imageModalLoading = true;
    this.modalImageRows = [];
    this.modalImageErrors = [];
    this.cdr.detectChanges();

    this.productImageService.findByProductId(productId).subscribe({
      next: (res) => {
        this.productImages = res;
        this.imageModalLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.imageModalLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  closeImageModal() {
    this.showImageModal = false;
    this.imageModalProductId = '';
    this.productImages = [];
    this.selectedImageIds = new Set();
    this.modalImageRows = [];
    this.modalImageErrors = [];
    this.cdr.detectChanges();
  }

  toggleSelectImage(id: string | undefined) {
    if (!id) return;
    if (this.selectedImageIds.has(id)) this.selectedImageIds.delete(id);
    else this.selectedImageIds.add(id);
    this.cdr.detectChanges();
  }

  selectAllImages() {
    this.productImages.forEach((img) => {
      if (img.id) this.selectedImageIds.add(img.id);
    });
    this.cdr.detectChanges();
  }

  clearSelection() {
    this.selectedImageIds = new Set();
    this.cdr.detectChanges();
  }

  deleteSelectedImages() {
    if (this.selectedImageIds.size === 0) {
      alert('Please select at least one image to delete');
      return;
    }
    if (!confirm(`Delete ${this.selectedImageIds.size} selected image(s)?`)) return;
    this.productImageService.deleteBatch(Array.from(this.selectedImageIds)).subscribe({
      next: () => this.openImageModal(this.imageModalProductId),
      error: () => alert('Error deleting images'),
    });
  }

  buildImageSrc(img: ProductImageDTO): string {
    if (!img.imageData || !img.contentType) return '';
    if (img.imageData.startsWith('data:')) return img.imageData;
    return `data:${img.contentType};base64,${img.imageData}`;
  }

  addModalImageRow() {
    this.modalImageRows.push({ preview: '', imageData: '', contentType: '', isPrimary: false });
    this.modalImageErrors.push('');
    if (this.modalImageRows.length === 1 && this.productImages.length === 0)
      this.modalImageRows[0].isPrimary = true;
    this.cdr.detectChanges();
  }

  removeModalImageRow(index: number) {
    const wasPrimary = this.modalImageRows[index].isPrimary;
    this.modalImageRows.splice(index, 1);
    this.modalImageErrors.splice(index, 1);
    if (wasPrimary && this.modalImageRows.length > 0) this.modalImageRows[0].isPrimary = true;
    this.cdr.detectChanges();
  }

  setModalPrimary(index: number) {
    this.modalImageRows.forEach((r, i) => (r.isPrimary = i === index));
    this.cdr.detectChanges();
  }

  onModalFileChange(event: Event, index: number) {
    const file = (event.target as HTMLInputElement).files?.[0];
    this.modalImageErrors[index] = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.modalImageErrors[index] = 'Only image files are allowed';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.modalImageErrors[index] = 'Image must be under 5MB';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      this.modalImageRows[index].preview = dataUrl;
      this.modalImageRows[index].imageData = dataUrl.split(',')[1];
      this.modalImageRows[index].contentType = file.type;
      this.cdr.detectChanges();
    };
    reader.readAsDataURL(file);
  }

  submitModalImages() {
    let valid = true;
    this.modalImageErrors = this.modalImageRows.map((row) => {
      if (!row.imageData) {
        valid = false;
        return 'Please select an image file';
      }
      return '';
    });
    if (!valid || this.modalImageRows.length === 0) {
      if (this.modalImageRows.length === 0) alert('Please add at least one image');
      this.cdr.detectChanges();
      return;
    }
    this.modalUploadSubmitting = true;
    this.cdr.detectChanges();
    const requests = this.modalImageRows.map((row) =>
      this.productImageService.create({
        productId: this.imageModalProductId,
        imageData: row.imageData,
        contentType: row.contentType,
        isPrimary: row.isPrimary,
      }),
    );
    let hasError = false;
    const sendNext = (i: number) => {
      if (i >= requests.length) {
        this.modalUploadSubmitting = false;
        if (!hasError) {
          this.modalImageRows = [];
          this.modalImageErrors = [];
          this.openImageModal(this.imageModalProductId);
        }
        this.cdr.detectChanges();
        return;
      }
      requests[i].subscribe({
        next: () => sendNext(i + 1),
        error: () => {
          hasError = true;
          alert(`Error uploading image ${i + 1}`);
          this.modalUploadSubmitting = false;
          this.cdr.detectChanges();
        },
      });
    };
    sendNext(0);
  }

  // =========================
  // IMAGE ROWS (form)
  // =========================
  addImageRow() {
    this.imageRows.push({ preview: '', imageData: '', contentType: '', isPrimary: false });
    this.imageErrors.push('');
    if (this.imageRows.length === 1) this.imageRows[0].isPrimary = true;
    this.cdr.detectChanges();
  }

  removeImageRow(index: number) {
    const wasPrimary = this.imageRows[index].isPrimary;
    this.imageRows.splice(index, 1);
    this.imageErrors.splice(index, 1);
    if (wasPrimary && this.imageRows.length > 0) this.imageRows[0].isPrimary = true;
    this.cdr.detectChanges();
  }

  setPrimary(index: number) {
    this.imageRows.forEach((r, i) => (r.isPrimary = i === index));
    this.cdr.detectChanges();
  }

  onFileChange(event: Event, index: number) {
    const file = (event.target as HTMLInputElement).files?.[0];
    this.imageErrors[index] = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.imageErrors[index] = 'Only image files are allowed';
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.imageErrors[index] = 'Image must be under 5MB';
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      this.imageRows[index].preview = dataUrl;
      this.imageRows[index].imageData = dataUrl.split(',')[1];
      this.imageRows[index].contentType = file.type;
      this.cdr.detectChanges();
    };
    reader.readAsDataURL(file);
  }

  validateImages(): boolean {
    let valid = true;
    this.imageErrors = this.imageRows.map((row) => {
      if (!row.imageData) {
        valid = false;
        return 'Please select an image file';
      }
      return '';
    });
    return valid;
  }

  // =========================
  // VALIDATE PRODUCT FORM
  // =========================
  onIdChange() {
    this.errors.productId = '';
    this.idExists = false;
    const id = this.form.productId?.trim();
    if (!id) {
      this.errors.productId = 'Product ID cannot be empty';
      return;
    }
    if (id.length < 2 || id.length > 20) {
      this.errors.productId = 'Product ID must be 2–20 characters';
      return;
    }
    if (this.isEdit) return;
    this.productService
      .findById(id)
      .pipe(catchError(() => of(null)))
      .subscribe((res) => {
        this.idExists = !!res;
        this.cdr.detectChanges();
      });
  }

  onNameChange() {
    this.errors.productName = '';
    const name = this.form.productName?.trim();
    if (!name) {
      this.errors.productName = 'Product name cannot be empty';
      return;
    }
    if (name.length < 2 || name.length > 100) {
      this.errors.productName = 'Product name must be 2–100 characters';
    }
  }

  onStatusChange() {
    this.errors.status = '';
    if (!this.form.status?.trim()) this.errors.status = 'Status cannot be empty';
  }

  onTypeChange() {
    this.errors.productTypeId = '';
    if (!this.form.productTypeId?.trim()) this.errors.productTypeId = 'Product type is required';
  }

  onPriceChange() {
    this.errors.price = '';
    const price = this.form.price;
    if (price === null || price === undefined || String(price).trim() === '') {
      this.errors.price = 'Price is required';
      return;
    }
    if (Number(price) <= 0) this.errors.price = 'Price must be greater than 0';
  }

  onDescriptionChange() {
    this.errors.description = '';
    if ((this.form.description ?? '').length > 2000)
      this.errors.description = 'Description must be under 2000 characters';
  }

  validate(): boolean {
    this.onIdChange();
    this.onNameChange();
    this.onStatusChange();
    this.onTypeChange();
    this.onPriceChange();
    this.onDescriptionChange();
    return (
      !this.errors.productId &&
      !this.errors.productName &&
      !this.errors.status &&
      !this.errors.productTypeId &&
      !this.errors.price &&
      !this.errors.description &&
      !this.idExists &&
      (this.imageRows.length === 0 || this.validateImages())
    );
  }

  // =========================
  // SUBMIT PRODUCT
  // =========================
  submit() {
    if (!this.validate()) return;
    this.form.images = this.imageRows.map((row) => ({
      imageData: row.imageData,
      contentType: row.contentType,
      isPrimary: row.isPrimary,
    }));
    const req$ = this.isEdit
      ? this.productService.edit(this.form.productId, this.form)
      : this.productService.create(this.form);
    req$.subscribe({
      next: () => {
        this.loadProductsPaged();
        this.reset();
        this.showProductModal = false;
      },
      error: (err) => console.error(err),
    });
  }

  edit(p: ProductDTO) {
    this.form = { ...p, images: [] };
    this.imageRows = [];
    this.imageErrors = [];
    this.isEdit = true;
    this.idExists = false;
    this.errors = {};
    this.cdr.detectChanges();
  }

  delete(productId: string) {
    if (!confirm(`Delete product "${productId}"?`)) return;
    this.productService.delete(productId).subscribe(() => this.loadProductsPaged());
  }

  reset() {
    this.form = {
      productId: '',
      productName: '',
      status: '',
      productTypeId: '',
      price: undefined,
      description: '',
      images: [],
    };
    this.imageRows = [];
    this.imageErrors = [];
    this.errors = {};
    this.isEdit = false;
    this.idExists = false;
    this.cdr.detectChanges();
  }

  // =========================
  // VARIANT MODAL
  // =========================
  openVariantModal(productId: string) {
    this.selectedProductId = productId;
    this.showVariantModal = true;
    this.existingVariants = [];
    this.newVariantRows = [];
    this.cdr.detectChanges();
    this.loadVariants(productId);
  }

  closeModal() {
    this.showVariantModal = false;
    this.selectedProductId = '';
    this.existingVariants = [];
    this.newVariantRows = [];
    this.cdr.detectChanges();
  }

  loadVariants(productId: string) {
    this.variantService.findByProduct(productId).subscribe((res) => {
      this.existingVariants = res;
      this.cdr.detectChanges();
    });
  }

  addRow() {
    this.newVariantRows.push({
      productId: this.selectedProductId,
      sizeId: '',
      colorId: '',
      quantity: 1,
    });
    this.cdr.detectChanges();
  }

  removeRow(index: number) {
    this.newVariantRows.splice(index, 1);
    this.cdr.detectChanges();
  }

  submitVariants() {
    const invalid = this.newVariantRows.some(
      (v) => !v.sizeId || !v.colorId || (v.quantity ?? 0) < 1,
    );
    if (invalid) {
      alert('Please fill in all fields for each variant row');
      return;
    }
    if (this.newVariantRows.length === 0) {
      alert('No new variants to save');
      return;
    }
    this.variantService
      .createBatch(
        this.newVariantRows.map((v) => ({
          productId: this.selectedProductId,
          sizeId: v.sizeId,
          colorId: v.colorId,
          quantity: v.quantity,
        })),
      )
      .subscribe({
        next: () => {
          alert('Variants saved successfully');
          this.newVariantRows = [];
          this.loadVariants(this.selectedProductId);
        },
        error: () => alert('Error saving variants'),
      });
  }

  updateVariant(v: ProductVariantDTO) {
    this.variantService.update(v).subscribe({
      next: () => alert('Variant updated'),
      error: (err) => console.error(err),
    });
  }

  deleteVariant(v: ProductVariantDTO) {
    if (!confirm('Delete this variant?')) return;
    this.variantService
      .delete(v.productId, v.sizeId, v.colorId)
      .subscribe(() => this.loadVariants(this.selectedProductId));
  }
}
