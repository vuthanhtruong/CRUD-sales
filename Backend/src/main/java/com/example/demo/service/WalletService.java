package com.example.demo.service;

import com.example.demo.dto.TopUpRequestDTO;
import com.example.demo.dto.WalletDTO;
import com.example.demo.dto.WalletTransactionDTO;
import com.example.demo.model.Person;
import com.example.demo.model.WalletTransaction;
import com.example.demo.model.WalletTopUpStatus;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletDTO mine();
    WalletTransactionDTO requestTopUp(TopUpRequestDTO request);
    List<WalletTransactionDTO> adminTopUps(WalletTopUpStatus status);
    WalletTransactionDTO approveTopUp(String transactionId);
    WalletTransactionDTO rejectTopUp(String transactionId);
    WalletTransaction pay(Person user, BigDecimal amount, String referenceId, String description);
    WalletTransaction refund(Person user, BigDecimal amount, String referenceId, String description);
}
