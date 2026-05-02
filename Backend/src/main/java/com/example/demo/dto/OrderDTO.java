package com.example.demo.dto;

import com.example.demo.model.OrderStatus;
import com.example.demo.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String username;
    private String customerName;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String couponCode;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;
    private List<OrderTimelineDTO> timeline;
    public OrderDTO(String id, String username, String customerName, OrderStatus status, PaymentMethod paymentMethod,
                    BigDecimal subtotalAmount, BigDecimal discountAmount, BigDecimal totalAmount, String couponCode,
                    String receiverName, String receiverPhone, String shippingAddress, String note,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.customerName = customerName;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.subtotalAmount = subtotalAmount;
        this.discountAmount = discountAmount;
        this.totalAmount = totalAmount;
        this.couponCode = couponCode;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
