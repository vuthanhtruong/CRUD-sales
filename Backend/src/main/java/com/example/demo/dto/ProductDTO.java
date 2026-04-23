package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private List<ProductImageDTO> images;

}