package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Person;
import com.example.demo.model.User;

public interface AccountDAO {
    void register(Account account);
    void createUser(Account account);
    Account getAccountByUsername(String username);
    Account getAccountByEmail(String email);
    Account getAccountByPhone(String phone);
    Account getAccountById(String id);
    String getCurrentAccountUsername();
    String getCurrentAccountRole(String username);
    Account getProfileByUsername(String username);
    void updateProfile(String username, Person updatedPerson);
    Person getCurrentUser();
    void updatePassword(String accountId, String encodedPassword);
}
