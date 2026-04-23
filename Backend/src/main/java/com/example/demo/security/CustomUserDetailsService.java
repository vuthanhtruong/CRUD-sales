package com.example.demo.security;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountDAO;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountDAO accountDAO;

    public CustomUserDetailsService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Account acc = accountDAO.getAccountByUsername(username);

        if (acc == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return toUserDetails(acc);
    }

    public UserDetails toUserDetails(Account acc) {
        return new org.springframework.security.core.userdetails.User(
                acc.getUsername(),
                acc.getPassword(),
                Collections.emptyList()
        );
    }

    public Account getAccount(String username) {
        return accountDAO.getAccountByUsername(username);
    }
}