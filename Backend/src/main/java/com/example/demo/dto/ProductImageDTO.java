package com.example.demo.dto;

import lombok.*;
import java.util.Base64;

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
    public ProductImageDTO(String id, byte[] imageData, String contentType, boolean isPrimary, String productId) {
        this.id = id;
        this.imageData = imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
        this.contentType = contentType;
        this.isPrimary = isPrimary;
        this.productId = productId;
    }

}