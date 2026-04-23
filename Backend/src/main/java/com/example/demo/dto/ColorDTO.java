package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorDTO {

    @NotBlank(message = "Color ID is required")
    @Size(min = 1, max = 20, message = "Color ID must be 1–20 characters")
    private String id;

    @NotBlank(message = "Color name is required")
    @Size(min = 1, max = 50, message = "Color name must be 1–50 characters")
    private String name;
}