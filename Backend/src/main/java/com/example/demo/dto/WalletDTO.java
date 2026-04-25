package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private String walletId;
    private BigDecimal balance;
    private LocalDateTime updatedAt;
    private List<WalletTransactionDTO> transactions;
}
