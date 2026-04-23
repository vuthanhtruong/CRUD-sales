package com.example.demo.controller;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.LoginRequest;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AccountService;
import jakarta.servlet.http.HttpSession;
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
    private final HttpSession httpSession;

    @Autowired
    public AccountController(AccountService accountService,
                             JwtUtil jwtUtil,
                             AuthenticationManager authenticationManager,
                             HttpSession httpSession) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.httpSession = httpSession;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid AccountDTO accountDTO) {
        accountService.register(accountDTO);
        return ResponseEntity.ok(Map.of(
                "message", "Register successfully"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtUtil.generateToken(request.getUsername());

            System.out.println("token: " + token);

            return ResponseEntity.ok(
                    Map.of(
                            "token", token,
                            "username", request.getUsername(),
                            "message", "Login success"
                    )
            );

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message", "Invalid username or password"
                    ));
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
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Not authenticated"));
        }

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName()
        ));
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of("message", "test"));
    }

    @GetMapping("/role")
    public ResponseEntity<?> getCurrentRole() {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Not authenticated"));
        }

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(Object::toString)
                .orElse(null);

        return ResponseEntity.ok(Map.of(
                "role", role
        ));
    }
}