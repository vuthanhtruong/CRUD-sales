import { CommonModule } from '@angular/common';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommentStatus, ProductCommentDTO, ProductCommentService } from '../services/product-comment.service';

@Component({
  selector: 'app-admin-comments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-comments.component.html',
  styleUrls: ['./admin-comments.component.css'],
})
export class AdminCommentsComponent implements OnInit {
  comments: ProductCommentDTO[] = [];
  statusFilter: CommentStatus | '' = '';
  loading = false;
  statuses: CommentStatus[] = ['PUBLISHED', 'PENDING', 'HIDDEN'];

  constructor(private commentService: ProductCommentService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void { this.loadComments(); }

  loadComments(): void {
    this.loading = true;
    this.commentService.adminFindAll(this.statusFilter).subscribe({
      next: (res) => { this.comments = res; this.loading = false; this.cdr.detectChanges(); },
      error: () => { this.loading = false; this.cdr.detectChanges(); },
    });
  }

  moderate(comment: ProductCommentDTO, status: CommentStatus): void {
    if (!comment.id) return;
    this.commentService.moderate(comment.id, status).subscribe({
      next: (updated) => { this.comments = this.comments.map(c => c.id === updated.id ? updated : c); this.cdr.detectChanges(); },
    });
  }

  remove(comment: ProductCommentDTO): void {
    if (!comment.id) return;
    this.commentService.delete(comment.id).subscribe({
      next: () => { this.comments = this.comments.filter(c => c.id !== comment.id); this.cdr.detectChanges(); },
    });
  }
}
