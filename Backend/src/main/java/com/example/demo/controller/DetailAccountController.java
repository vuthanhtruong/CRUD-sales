package com.example.demo.controller;

import com.example.demo.dto.ProfileDTO;
import com.example.demo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detail-account")
@CrossOrigin(origins = "http://localhost:4200")
public class DetailAccountController {

    private final AccountService accountService;

    public DetailAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile() {
        return ResponseEntity.ok(accountService.getProfileByUsername(currentUsername()));
    }

    @GetMapping("/role")
    public ResponseEntity<String> getMyRole() {
        String username = currentUsername();
        return ResponseEntity.ok(accountService.getCurrentAccountRole(username));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateMyProfile(@RequestBody @Valid ProfileDTO dto) {
        accountService.updateProfile(currentUsername(), dto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<Void> updateProfileLegacy(@PathVariable String username, @RequestBody @Valid ProfileDTO dto) {
        String current = currentUsername();
        if (!current.equals(username)) {
            throw new RuntimeException("You can only update your own profile");
        }
        accountService.updateProfile(current, dto);
        return ResponseEntity.ok().build();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) throw new RuntimeException("User not logged in");
        return authentication.getName();
    }
}
