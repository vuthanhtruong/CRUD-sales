package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SizeDTO {

    @NotBlank(message = "Size ID is required")
    @Size(min = 1, max = 20, message = "Size ID must be 1–20 characters")
    private String id;

    @NotBlank(message = "Size name is required")
    @Size(min = 1, max = 50, message = "Size name must be 1–50 characters")
    private String name;
}