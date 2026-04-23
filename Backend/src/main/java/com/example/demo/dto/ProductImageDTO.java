package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDTO {

    private String id;

    // Base64 string (frontend gửi lên)
    private String imageData;

    private String contentType;

    private boolean isPrimary;

    private String productId;
}