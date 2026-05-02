package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.demo.model.ProductStatus;
import java.math.BigDecimal;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    @NotBlank(message = "Product ID cannot be empty")
    @Size(min = 2, max = 20, message = "Product ID must be 2-20 characters")
    private String productId;

    @NotBlank(message = "Product name cannot be empty")
    @Size(min = 2, max = 100, message = "Product name must be 2-100 characters")
    private String productName;

    @NotBlank(message = "Status cannot be empty")
    private String status;

    @NotNull(message = "Product type is required")
    private String productTypeId;

    private String createdBy;

    @NotNull(message = "Price is required")
    @jakarta.validation.constraints.DecimalMin(value = "0.0", inclusive = false)
    private java.math.BigDecimal price;

    @Size(max = 2000, message = "Description too long")
    private String description;

    private String image; // 🔥 đổi tên đúng bản chất

    private List<ProductImageDTO> images;

    public ProductDTO(String productId, String productName, ProductStatus status, String productTypeId, String createdBy,
                      BigDecimal price, String description, byte[] imageData) {
        this.productId = productId;
        this.productName = productName;
        this.status = status == null ? null : status.name();
        this.productTypeId = productTypeId;
        this.createdBy = createdBy;
        this.price = price;
        this.description = description;
        this.image = imageData == null ? null : Base64.getEncoder().encodeToString(imageData);
        this.images = null;
    }

}