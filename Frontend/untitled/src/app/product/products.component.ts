import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ProductService, ProductDTO, ProductSearchParams } from '../services/product.service';
import { ProductImageService, ProductImageDTO } from '../services/product-image.service';
import { ProductVariantService, ProductVariantDTO } from '../services/product-variant.service';
import { ProductTypeService, ProductType } from '../services/product-type.service';
import { SizeService, Size } from '../services/size.service';
import { ColorService, ColorDTO } from '../services/color.service';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface ImageRow {
  preview: string;
  imageData?: string;
  contentType?: string;
  isPrimary: boolean;
}

// Placeholder image list from Picsum Photos (free, no API key needed)
const PLACEHOLDER_IMAGES = [
  'https://picsum.photos/seed/product1/400/300',
  'https://picsum.photos/seed/product2/400/300',
  'https://picsum.photos/seed/product3/400/300',
  'https://picsum.photos/seed/product4/400/300',
  'https://picsum.photos/seed/product5/400/300',
  'https://picsum.photos/seed/product6/400/300',
  'https://picsum.photos/seed/product7/400/300',
  'https://picsum.photos/seed/product8/400/300',
];

const MAX_IMAGE_SIZE_MB = 1;
const MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024;

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
  totalItems = 0;

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

  selectedPages: Set<number> = new Set();
  exportScopeType: 'current' | 'selected' | 'all' = 'current';

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  // =========================
  // DELETE PRODUCT POPUP
  // =========================
  showDeletePopup = false;
  deleteProductId = '';
  deleteLoading = false;
  deleteError = '';

  // =========================
  // DELETE IMAGES POPUP
  // =========================
  showDeleteImagesPopup = false;
  deleteImagesLoading = false;
  deleteImagesError = '';

  // =========================
  // DELETE VARIANT POPUP
  // =========================
  showDeleteVariantPopup = false;
  variantToDelete: ProductVariantDTO | null = null;
  deleteVariantLoading = false;
  deleteVariantError = '';

  // =========================
  // GENERIC ALERT POPUP
  // =========================
  showAlertPopup = false;
  alertPopupTitle = '';
  alertPopupMessage = '';
  alertPopupIsError = false;

  constructor(
    private productService: ProductService,
    private productImageService: ProductImageService,
    private variantService: ProductVariantService,
    private productTypeService: ProductTypeService,
    private sizeService: SizeService,
    private colorService: ColorService,
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

  // =========================
  // GENERIC ALERT POPUP
  // =========================
  openAlertPopup(title: string, message: string, isError = false) {
    this.alertPopupTitle = title;
    this.alertPopupMessage = message;
    this.alertPopupIsError = isError;
    this.showAlertPopup = true;
    this.cdr.detectChanges();
  }

  closeAlertPopup() {
    this.showAlertPopup = false;
    this.cdr.detectChanges();
  }

  buildProductImageSrc(image: string | undefined): string {
    if (!image) return '';
    if (image.startsWith('data:') || image.startsWith('http')) return image;
    return `data:image/jpeg;base64,${image}`;
  }

  /**
   * Return a deterministic placeholder image URL from the product ID.
   * Each product always receives the same placeholder image.
   */
  getPlaceholderImageUrl(productId: string | undefined): string {
    const id = productId || '';
    const index = Math.abs(this.simpleHash(id)) % PLACEHOLDER_IMAGES.length;
    return PLACEHOLDER_IMAGES[index];
  }

  private simpleHash(str: string): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash + str.charCodeAt(i)) | 0;
    }
    return hash;
  }

  openCart() {
    this.openAlertPopup('Cart', `Cart items: ${this.cartCount}`);
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
    this.productService
      .findProductsPage(this.searchParams, this.currentPage, this.pageSize)
      .subscribe({
        next: (res) => {
          this.products = res.content ?? [];
          this.totalPages = Math.max(1, res.totalPages || 1);
          this.totalItems = res.totalItems || 0;
          this.currentPage = Math.min(Math.max(1, res.currentPage || 1), this.totalPages);
          this.cdr.detectChanges();
        },
        error: (err) => console.error(err),
      });
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.loadProductsPaged();
  }

  changePageSize(size: number) {
    this.pageSize = size;
    this.currentPage = 1;
    this.loadProductsPaged();
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

    this.isSearchMode = hasAnyFilter;
    this.currentPage = 1;
    this.loadProductsPaged();
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

  doExport() {
    this.exportError = '';

    const selected = Array.from(this.selectedPages).sort((a, b) => a - b);
    if (this.exportScopeType === 'selected' && selected.length === 0) {
      this.exportError = 'Please select at least one page.';
      return;
    }

    this.exportLoading = true;
    this.cdr.detectChanges();

    this.productService
      .exportProducts(
        this.exportFormat,
        this.exportScopeType,
        this.searchParams,
        this.currentPage,
        this.pageSize,
        selected,
      )
      .subscribe({
        next: (response) => {
          const blob = response.body;
          if (!blob) {
            this.exportError = 'Export failed. Please try again.';
            this.exportLoading = false;
            this.cdr.detectChanges();
            return;
          }

          this.downloadBlob(
            blob,
            this.getFileNameFromResponse(response.headers.get('Content-Disposition')),
          );
          this.exportLoading = false;
          this.closeExportModal();
        },
        error: (err) => {
          console.error(err);
          this.exportError = 'Export failed. Please try again.';
          this.exportLoading = false;
          this.cdr.detectChanges();
        },
      });
  }

  private downloadBlob(blob: Blob, fileName: string) {
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.URL.revokeObjectURL(url);
  }

  private getFileNameFromResponse(contentDisposition: string | null): string {
    const fallback = `products_${new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')}.${this.exportExtension()}`;
    if (!contentDisposition) return fallback;

    const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
    return match?.[1] ? decodeURIComponent(match[1]) : fallback;
  }

  private exportExtension(): string {
    if (this.exportFormat === 'word') return 'docx';
    if (this.exportFormat === 'pdf') return 'pdf';
    return 'xlsx';
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

  // --- Delete Images Popup ---
  openDeleteImagesPopup() {
    if (this.selectedImageIds.size === 0) {
      this.openAlertPopup('No images selected', 'Please select at least one image to delete.', true);
      return;
    }
    this.deleteImagesError = '';
    this.deleteImagesLoading = false;
    this.showDeleteImagesPopup = true;
    this.cdr.detectChanges();
  }

  closeDeleteImagesPopup() {
    if (this.deleteImagesLoading) return;
    this.showDeleteImagesPopup = false;
    this.deleteImagesError = '';
    this.cdr.detectChanges();
  }

  confirmDeleteImages() {
    this.deleteImagesLoading = true;
    this.deleteImagesError = '';
    this.cdr.detectChanges();

    this.productImageService.deleteBatch(Array.from(this.selectedImageIds)).subscribe({
      next: () => {
        this.deleteImagesLoading = false;
        this.showDeleteImagesPopup = false;
        this.deleteImagesError = '';
        this.openImageModal(this.imageModalProductId);
      },
      error: () => {
        this.deleteImagesLoading = false;
        this.deleteImagesError = 'Error deleting images. Please try again.';
        this.cdr.detectChanges();
      },
    });
  }

  makePrimaryImage(img: ProductImageDTO, event?: Event) {
    event?.stopPropagation();
    if (!img.id) return;
    this.productImageService.setPrimary(img.id).subscribe({
      next: () => this.openImageModal(this.imageModalProductId),
      error: () => this.openAlertPopup('Error', 'Could not update primary image.', true),
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
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      this.modalImageErrors[index] = `Image must be under ${MAX_IMAGE_SIZE_MB}MB`;
      (event.target as HTMLInputElement).value = '';
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
      if (this.modalImageRows.length === 0) {
        this.openAlertPopup('No images', 'Please add at least one image.', true);
      }
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
          this.modalUploadSubmitting = false;
          this.openAlertPopup('Upload error', `Error uploading image ${i + 1}. Please try again.`, true);
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
    if (file.size > MAX_IMAGE_SIZE_BYTES) {
      this.imageErrors[index] = `Image must be under ${MAX_IMAGE_SIZE_MB}MB`;
      (event.target as HTMLInputElement).value = '';
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

  // =========================
  // DELETE PRODUCT POPUP
  // =========================
  openDeletePopup(productId: string) {
    this.deleteProductId = productId;
    this.deleteError = '';
    this.deleteLoading = false;
    this.showDeletePopup = true;
    this.cdr.detectChanges();
  }

  closeDeletePopup() {
    if (this.deleteLoading) return;
    this.showDeletePopup = false;
    this.deleteProductId = '';
    this.deleteError = '';
    this.cdr.detectChanges();
  }

  confirmDeleteProduct() {
    if (!this.deleteProductId) return;
    this.deleteLoading = true;
    this.deleteError = '';
    this.cdr.detectChanges();

    this.productService.delete(this.deleteProductId).subscribe({
      next: () => {
        this.deleteLoading = false;
        this.showDeletePopup = false;
        this.deleteProductId = '';
        this.deleteError = '';
        this.loadProductsPaged();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.deleteLoading = false;
        this.deleteError = 'Could not delete product. Please try again.';
        this.cdr.detectChanges();
      },
    });
  }

  delete(productId: string) {
    this.openDeletePopup(productId);
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
      this.openAlertPopup('Validation error', 'Please fill in all fields for each variant row.', true);
      return;
    }
    if (this.newVariantRows.length === 0) {
      this.openAlertPopup('No variants', 'No new variants to save.', true);
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
          this.newVariantRows = [];
          this.loadVariants(this.selectedProductId);
          this.openAlertPopup('Success', 'Variants saved successfully.');
        },
        error: () => this.openAlertPopup('Error', 'Error saving variants. Please try again.', true),
      });
  }

  updateVariant(v: ProductVariantDTO) {
    this.variantService.update(v).subscribe({
      next: () => this.openAlertPopup('Success', 'Variant updated successfully.'),
      error: (err) => console.error(err),
    });
  }

  // --- Delete Variant Popup ---
  openDeleteVariantPopup(v: ProductVariantDTO) {
    this.variantToDelete = v;
    this.deleteVariantError = '';
    this.deleteVariantLoading = false;
    this.showDeleteVariantPopup = true;
    this.cdr.detectChanges();
  }

  closeDeleteVariantPopup() {
    if (this.deleteVariantLoading) return;
    this.showDeleteVariantPopup = false;
    this.variantToDelete = null;
    this.deleteVariantError = '';
    this.cdr.detectChanges();
  }

  confirmDeleteVariant() {
    if (!this.variantToDelete) return;
    const v = this.variantToDelete;
    this.deleteVariantLoading = true;
    this.deleteVariantError = '';
    this.cdr.detectChanges();

    this.variantService.delete(v.productId, v.sizeId, v.colorId).subscribe({
      next: () => {
        this.deleteVariantLoading = false;
        this.showDeleteVariantPopup = false;
        this.variantToDelete = null;
        this.loadVariants(this.selectedProductId);
        this.cdr.detectChanges();
      },
      error: () => {
        this.deleteVariantLoading = false;
        this.deleteVariantError = 'Could not delete variant. Please try again.';
        this.cdr.detectChanges();
      },
    });
  }

  deleteVariant(v: ProductVariantDTO) {
    this.openDeleteVariantPopup(v);
  }
}
