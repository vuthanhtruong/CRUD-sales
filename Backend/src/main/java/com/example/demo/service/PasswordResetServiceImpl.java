package com.example.demo.service;

import com.example.demo.dto.ForgotPasswordRequestDTO;
import com.example.demo.dto.ResetPasswordRequestDTO;
import com.example.demo.model.Account;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.repository.AccountDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {
    private final AccountDAO accountDAO;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.frontend.reset-password-url:http://localhost:4200/reset-password}")
    private String resetPasswordUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public PasswordResetServiceImpl(AccountDAO accountDAO, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.accountDAO = accountDAO;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public void requestReset(ForgotPasswordRequestDTO request) {
        Account account = accountDAO.getAccountByEmail(request.getEmail());
        // Always return success to avoid leaking registered emails.
        if (account == null) return;

        invalidateOpenTokens(account.getId());

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setAccount(account);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        entityManager.persist(resetToken);

        sendResetEmail(account, token);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = entityManager.createQuery(
                        "SELECT t FROM PasswordResetToken t JOIN FETCH t.account a WHERE t.token = :token",
                        PasswordResetToken.class)
                .setParameter("token", request.getToken())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.isUsed()) throw new RuntimeException("Reset token has already been used");
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) throw new RuntimeException("Reset token has expired");

        accountDAO.updatePassword(resetToken.getAccount().getId(), passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);
        entityManager.merge(resetToken);
    }

    private void invalidateOpenTokens(String accountId) {
        entityManager.createQuery("UPDATE PasswordResetToken t SET t.used = true WHERE t.account.id = :accountId AND t.used = false")
                .setParameter("accountId", accountId)
                .executeUpdate();
    }

    private void sendResetEmail(Account account, String token) {
        String email = account.getUser() == null ? null : account.getUser().getEmail();
        if (email == null || email.isBlank()) return;
        String link = resetPasswordUrl + "?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Reset your Nova Commerce password");
        message.setText("Hello " + account.getUsername() + ",\n\n" +
                "Use this secure link to reset your password. The link expires in 30 minutes:\n" +
                link + "\n\n" +
                "If you did not request this, you can ignore this email.\n\n" +
                "Nova Commerce Team");
        try {
            mailSender.send(message);
        } catch (Exception ex) {
            // Development fallback: keep the app usable while Gmail SMTP is not configured yet.
            System.out.println("Password reset mail could not be sent. Reset link for development: " + link);
        }
    }
}
