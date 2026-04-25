package com.example.demo.controller;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ForgotPasswordRequestDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.ResetPasswordRequestDTO;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AccountService;
import com.example.demo.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:4200")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AccountController(AccountService accountService,
                             JwtUtil jwtUtil,
                             AuthenticationManager authenticationManager,
                             PasswordResetService passwordResetService) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AccountDTO accountDTO) {
        accountService.register(accountDTO);
        return ResponseEntity.ok(Map.of("message", "Registration completed successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequestDTO request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequestDTO request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtil.generateToken(request.getUsername());
            String role = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse(null);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", request.getUsername(),
                    "role", role,
                    "message", "Login success"
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid username or password"));
        }
    }

    @GetMapping("/exists/username")
    public boolean existsUsername(@RequestParam String username) {
        return accountService.getAccountByUsername(username) != null;
    }

    @GetMapping("/exists/email")
    public boolean existsEmail(@RequestParam String email) {
        return accountService.getAccountByEmail(email) != null;
    }

    @GetMapping("/exists/phone")
    public boolean existsPhone(@RequestParam String phone) {
        return accountService.getAccountByPhone(phone) != null;
    }

    @GetMapping("/{id}")
    public AccountDTO getById(@PathVariable String id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentAccount() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }

    @GetMapping("/role")
    public ResponseEntity<?> getCurrentRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }
        String role = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse(null);
        return ResponseEntity.ok(Map.of("role", role));
    }
}
