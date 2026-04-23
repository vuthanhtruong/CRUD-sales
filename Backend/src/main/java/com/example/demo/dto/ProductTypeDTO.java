package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductTypeDTO {

    @NotBlank(message = "Product type ID is required")
    @Size(min = 2, max = 20, message = "Product type ID must be 2–20 characters")
    private String id;

    @NotBlank(message = "Product type name is required")
    @Size(min = 2, max = 100, message = "Product type name must be 2–100 characters")
    private String typeName;
}