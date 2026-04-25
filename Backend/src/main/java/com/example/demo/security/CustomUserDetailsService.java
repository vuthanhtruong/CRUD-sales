package com.example.demo.security;

import com.example.demo.model.Account;
import com.example.demo.model.Admin;
import com.example.demo.model.User;
import com.example.demo.repository.AccountDAO;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (acc == null) {
            throw new UsernameNotFoundException("User not found");
        }

        String role = "ROLE_USER";

        if (acc.getUser() instanceof Admin) {
            role = "ROLE_ADMIN";
        } else if (acc.getUser() instanceof User) {
            role = "ROLE_USER";
        }

        return new org.springframework.security.core.userdetails.User(
                acc.getUsername(),
                acc.getPassword(),
                List.of(new SimpleGrantedAuthority(role))
        );
    }

    public Account getAccount(String username) {
        return accountDAO.getAccountByUsername(username);
    }
}