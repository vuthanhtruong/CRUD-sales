package com.example.demo.service;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Account;

public interface AccountService {
    void register(AccountDTO account);
    void createUser(AccountDTO account);
    AccountDTO getAccountByUsername(String username);
    AccountDTO getAccountByEmail(String email);
    AccountDTO getAccountByPhone(String phone);
    AccountDTO getAccountById(String id);
    String getCurrentAccountUsername();
    String getCurrentAccountRole(String username);
    ProfileDTO getProfileByUsername(String username);
    void updateProfile(String username, ProfileDTO profile);
}
