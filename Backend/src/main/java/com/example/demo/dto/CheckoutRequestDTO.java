package com.example.demo.dto;

import com.example.demo.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequestDTO {

    @NotEmpty(message = "Please select at least one cart item")
    private List<String> cartItemIds;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 100, message = "Receiver name is too long")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Phone number is invalid")
    private String receiverPhone;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 255, message = "Shipping address is too long")
    private String shippingAddress;

    @Size(max = 1000, message = "Note is too long")
    private String note;

    @Size(max = 40, message = "Coupon code is too long")
    private String couponCode;

    private PaymentMethod paymentMethod = PaymentMethod.COD;
}
