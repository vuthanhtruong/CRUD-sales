import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProductUserService, SizeDTO, ColorDTO } from '../services/product-user.service';
import { ProductImageDTO } from '../services/product-image.service';
import { ProductDTO } from '../services/product.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {

  product: ProductDTO | null = null;
  images: ProductImageDTO[] = [];
  sizes: SizeDTO[] = [];
  colors: ColorDTO[] = [];

  selectedImage: ProductImageDTO | null = null;
  selectedSize: SizeDTO | null = null;
  selectedColor: ColorDTO | null = null;

  loading = true;

  constructor(
    private route: ActivatedRoute,
    private productUserService: ProductUserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const productId = this.route.snapshot.paramMap.get('id')!;
    this.loadAll(productId);
  }

  loadAll(productId: string) {
    this.loading = true;
    forkJoin({
      product: this.productUserService.getProductById(productId),
      images: this.productUserService.getImagesByProduct(productId),
      sizes: this.productUserService.getSizesByProduct(productId),
      colors: this.productUserService.getColorsByProduct(productId)
    }).subscribe({
      next: res => {
        this.product = res.product;
        this.images = res.images;
        this.sizes = res.sizes;
        this.colors = res.colors;

        // Mặc định chọn ảnh primary, nếu không có thì chọn ảnh đầu
        this.selectedImage = this.images.find(img => img.isPrimary) ?? this.images[0] ?? null;

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  selectImage(img: ProductImageDTO) {
    this.selectedImage = img;
    this.cdr.detectChanges();
  }

  selectSize(size: SizeDTO) {
    this.selectedSize = size;
    this.cdr.detectChanges();
  }

  selectColor(color: ColorDTO) {
    this.selectedColor = color;
    this.cdr.detectChanges();
  }

  buildImageSrc(img: ProductImageDTO): string {
    if (!img.imageData || !img.contentType) return '';
    if (img.imageData.startsWith('data:')) return img.imageData;
    return `data:${img.contentType};base64,${img.imageData}`;
  }
}
