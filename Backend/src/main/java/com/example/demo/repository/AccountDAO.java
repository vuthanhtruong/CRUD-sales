package com.example.demo.repository;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Person;
import com.example.demo.model.User;

public interface AccountDAO {
    void register(Account account);
    void createUser(Account account);
    Account getAccountByUsername(String username);
    AccountDTO getAccountDTOByUsername(String username);
    Account getAccountByEmail(String email);
    AccountDTO getAccountDTOByEmail(String email);
    Account getAccountByPhone(String phone);
    AccountDTO getAccountDTOByPhone(String phone);
    Account getAccountById(String id);
    AccountDTO getAccountDTOById(String id);
    String getCurrentAccountUsername();
    String getCurrentAccountRole(String username);
    Account getProfileByUsername(String username);
    ProfileDTO getProfileDTOByUsername(String username);
    void updateProfile(String username, Person updatedPerson);
    Person getCurrentUser();
    void updatePassword(String accountId, String encodedPassword);
}
