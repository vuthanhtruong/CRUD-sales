package com.example.demo.dto;

import com.example.demo.model.CommentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCommentDTO {
    private String id;
    private String productId;
    private String productName;
    private String parentId;
    private String username;
    private String customerName;

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment must be at most 2000 characters")
    private String content;

    private CommentStatus status;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductCommentDTO> replies = new ArrayList<>();
    public ProductCommentDTO(String id, String productId, String productName, String parentId, String username, String customerName,
                             String content, CommentStatus status, Integer helpfulCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.parentId = parentId;
        this.username = username;
        this.customerName = customerName;
        this.content = content;
        this.status = status;
        this.helpfulCount = helpfulCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.replies = new ArrayList<>();
    }

}
