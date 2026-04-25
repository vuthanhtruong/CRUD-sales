package com.example.demo.dto;

import com.example.demo.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimelineDTO {
    private String id;
    private OrderStatus status;
    private String note;
    private LocalDateTime createdAt;
}
