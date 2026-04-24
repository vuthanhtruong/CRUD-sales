package com.example.demo.controller;

import com.example.demo.dto.ProfileDTO;
import com.example.demo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detail-account")
@CrossOrigin(origins = "http://localhost:4200")
public class DetailAccountController {

    @Autowired
    private AccountService accountService;

    // 👤 GET PROFILE BY USERNAME (FRONTEND SEND)
    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }

        String username = authentication.getName();
        return ResponseEntity.ok(accountService.getProfileByUsername(username));
    }

    // 🧾 GET ROLE BY USERNAME
    @GetMapping("/role")
    public ResponseEntity<String> getMyRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }

        String username = authentication.getName();
        return ResponseEntity.ok(accountService.getCurrentAccountRole(username));
    }

    // ✏️ UPDATE PROFILE BY USERNAME
    @PutMapping("/update/{username}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable String username,
            @RequestBody @Valid ProfileDTO dto
    ) {
        accountService.updateProfile(username, dto);
        return ResponseEntity.ok().build();
    }
}