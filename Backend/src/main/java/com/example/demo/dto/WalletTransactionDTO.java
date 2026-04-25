package com.example.demo.dto;

import com.example.demo.model.WalletTopUpStatus;
import com.example.demo.model.WalletTransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionDTO {
    private String id;
    private WalletTransactionType type;
    private WalletTopUpStatus topUpStatus;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String username;
    private String customerName;
}
