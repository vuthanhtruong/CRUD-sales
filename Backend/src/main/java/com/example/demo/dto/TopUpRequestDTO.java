package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequestDTO {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum top-up is 1,000")
    private BigDecimal amount;

    @Size(max = 500, message = "Note is too long")
    private String note;
}
