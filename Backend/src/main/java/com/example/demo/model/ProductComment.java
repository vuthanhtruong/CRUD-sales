package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "product_comment",
        indexes = {
                @Index(name = "idx_comment_product_status_created", columnList = "product_id, status, created_at"),
                @Index(name = "idx_comment_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_comment_parent", columnList = "parent_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Person user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ProductComment parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("createdAt ASC")
    private List<ProductComment> replies = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PUBLISHED;

    @Column(nullable = false)
    private Integer helpfulCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
