package com.example.demo.dto;

import com.example.demo.model.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewDTO {
    private String id;

    @NotBlank(message = "Product is required")
    private String productId;
    private String productName;
    private String username;
    private String customerName;
    private String orderId;

    @NotNull(message = "Rating is required")
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 120)
    private String title;

    @NotBlank(message = "Comment is required")
    @Size(max = 2000)
    private String comment;

    private ReviewStatus status;
    private String adminReply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
