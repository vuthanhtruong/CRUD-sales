package com.example.demo.service;

import com.example.demo.dto.TopUpRequestDTO;
import com.example.demo.dto.WalletDTO;
import com.example.demo.dto.WalletTransactionDTO;
import com.example.demo.model.*;
import com.example.demo.repository.AccountDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {
    private final AccountDAO accountDAO;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    public WalletServiceImpl(AccountDAO accountDAO, NotificationService notificationService) {
        this.accountDAO = accountDAO;
        this.notificationService = notificationService;
    }

    @Override
    public WalletDTO mine() {
        return toWalletDTO(findOrCreate(accountDAO.getCurrentUser()));
    }

    @Override
    public WalletTransactionDTO requestTopUp(TopUpRequestDTO request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
        Wallet wallet = findOrCreate(accountDAO.getCurrentUser());
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(WalletTransactionType.TOP_UP);
        tx.setTopUpStatus(WalletTopUpStatus.PENDING);
        tx.setAmount(request.getAmount());
        tx.setBalanceAfter(wallet.getBalance());
        tx.setDescription(request.getNote() == null || request.getNote().isBlank()
                ? "Customer top-up request"
                : request.getNote());
        entityManager.persist(tx);
        notificationService.notifyUser(wallet.getUser(), "Top-up request created", "Your wallet top-up request is waiting for admin approval.", "WALLET", "/wallet");
        return toTransactionDTO(tx);
    }

    @Override
    public List<WalletTransactionDTO> adminTopUps(WalletTopUpStatus status) {
        WalletTopUpStatus resolved = status == null ? WalletTopUpStatus.PENDING : status;
        return entityManager.createQuery(
                        "SELECT t FROM WalletTransaction t JOIN FETCH t.wallet w JOIN FETCH w.user u WHERE t.type = :type AND t.topUpStatus = :status ORDER BY t.createdAt DESC",
                        WalletTransaction.class)
                .setParameter("type", WalletTransactionType.TOP_UP)
                .setParameter("status", resolved)
                .getResultList()
                .stream().map(this::toTransactionDTO).toList();
    }

    @Override
    public WalletTransactionDTO approveTopUp(String transactionId) {
        WalletTransaction tx = getTransaction(transactionId);
        if (tx.getType() != WalletTransactionType.TOP_UP || tx.getTopUpStatus() != WalletTopUpStatus.PENDING) {
            throw new RuntimeException("Only pending top-up requests can be approved");
        }
        Wallet wallet = tx.getWallet();
        wallet.setBalance(wallet.getBalance().add(tx.getAmount()));
        tx.setTopUpStatus(WalletTopUpStatus.APPROVED);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setApprovedAt(LocalDateTime.now());
        Person adminPerson = accountDAO.getCurrentUser();
        if (adminPerson instanceof Admin admin) tx.setApprovedBy(admin);
        entityManager.merge(wallet);
        entityManager.merge(tx);
        notificationService.notifyUser(wallet.getUser(), "Wallet topped up", "Your top-up request has been approved.", "WALLET", "/wallet");
        return toTransactionDTO(tx);
    }

    @Override
    public WalletTransactionDTO rejectTopUp(String transactionId) {
        WalletTransaction tx = getTransaction(transactionId);
        if (tx.getType() != WalletTransactionType.TOP_UP || tx.getTopUpStatus() != WalletTopUpStatus.PENDING) {
            throw new RuntimeException("Only pending top-up requests can be rejected");
        }
        tx.setTopUpStatus(WalletTopUpStatus.REJECTED);
        tx.setApprovedAt(LocalDateTime.now());
        entityManager.merge(tx);
        notificationService.notifyUser(tx.getWallet().getUser(), "Top-up rejected", "Your wallet top-up request was rejected.", "WALLET", "/wallet");
        return toTransactionDTO(tx);
    }

    @Override
    public WalletTransaction pay(Person user, BigDecimal amount, String referenceId, String description) {
        Wallet wallet = findOrCreate(user);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient wallet balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return record(wallet, WalletTransactionType.PAYMENT, WalletTopUpStatus.NONE, amount.negate(), wallet.getBalance(), referenceId, description);
    }

    @Override
    public WalletTransaction refund(Person user, BigDecimal amount, String referenceId, String description) {
        Wallet wallet = findOrCreate(user);
        wallet.setBalance(wallet.getBalance().add(amount));
        return record(wallet, WalletTransactionType.REFUND, WalletTopUpStatus.NONE, amount, wallet.getBalance(), referenceId, description);
    }

    private WalletTransaction record(Wallet wallet, WalletTransactionType type, WalletTopUpStatus status, BigDecimal amount, BigDecimal balanceAfter, String referenceId, String description) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(type);
        tx.setTopUpStatus(status);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setReferenceId(referenceId);
        tx.setDescription(description);
        entityManager.merge(wallet);
        entityManager.persist(tx);
        return tx;
    }

    private WalletTransaction getTransaction(String id) {
        return entityManager.createQuery(
                        "SELECT t FROM WalletTransaction t JOIN FETCH t.wallet w JOIN FETCH w.user u WHERE t.id = :id",
                        WalletTransaction.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Wallet transaction not found"));
    }

    private Wallet findOrCreate(Person user) {
        if (user == null) throw new RuntimeException("User profile not found");
        Wallet wallet = entityManager.createQuery("SELECT w FROM Wallet w WHERE w.user.id = :userId", Wallet.class)
                .setParameter("userId", user.getId())
                .getResultStream()
                .findFirst()
                .orElse(null);
        if (wallet != null) return wallet;
        wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        entityManager.persist(wallet);
        return wallet;
    }

    private WalletDTO toWalletDTO(Wallet wallet) {
        List<WalletTransactionDTO> txs = entityManager.createQuery(
                        "SELECT t FROM WalletTransaction t WHERE t.wallet.id = :walletId ORDER BY t.createdAt DESC",
                        WalletTransaction.class)
                .setParameter("walletId", wallet.getId())
                .setMaxResults(50)
                .getResultList()
                .stream().map(this::toTransactionDTO).toList();
        return new WalletDTO(wallet.getId(), wallet.getBalance(), wallet.getUpdatedAt(), txs);
    }

    private WalletTransactionDTO toTransactionDTO(WalletTransaction tx) {
        Person user = tx.getWallet() == null ? null : tx.getWallet().getUser();
        String username = null;
        if (user != null) {
            try { username = accountDAO.getAccountById(user.getId()).getUsername(); } catch (Exception ignored) {}
        }
        String customerName = user == null ? null : ((safe(user.getFirstName()) + " " + safe(user.getLastName())).trim());
        return new WalletTransactionDTO(tx.getId(), tx.getType(), tx.getTopUpStatus(), tx.getAmount(), tx.getBalanceAfter(), tx.getReferenceId(), tx.getDescription(), tx.getCreatedAt(), tx.getApprovedAt(), username, customerName);
    }

    private String safe(String s) { return s == null ? "" : s; }
}
