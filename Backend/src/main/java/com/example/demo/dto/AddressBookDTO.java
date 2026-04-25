package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookDTO {
    private String id;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 100, message = "Receiver name is too long")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Phone number is invalid")
    private String receiverPhone;

    @NotBlank(message = "Full address is required")
    @Size(max = 255, message = "Address is too long")
    private String fullAddress;

    private String city;
    private String district;
    private String ward;
    private String label;
    private boolean defaultAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
