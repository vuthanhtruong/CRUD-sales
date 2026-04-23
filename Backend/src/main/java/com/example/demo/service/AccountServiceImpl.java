package com.example.demo.service;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Person;
import com.example.demo.model.User;
import com.example.demo.repository.AccountDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    @Override
    public void updateProfile(String username, ProfileDTO profile) {

        Account account = accountDAO.getAccountByUsername(username);

        if (account == null || account.getUser() == null) {
            throw new RuntimeException("Account or profile not found");
        }

        Person person = account.getUser();
        person.setId(account.getUser().getId());
        person.setFirstName(profile.getFirstName());
        person.setLastName(profile.getLastName());
        person.setPhone(profile.getPhone());
        person.setAddress(profile.getAddress());
        person.setGender(profile.getGender());
        person.setBirthday(profile.getBirthday());

        accountDAO.updateProfile(username,person);
    }

    @Override
    public ProfileDTO getProfileByUsername(String username) {

        Account account = accountDAO.getProfileByUsername(username);

        if (account == null || account.getUser() == null) {
            throw new RuntimeException("Profile not found");
        }

        Person user = account.getUser();

        ProfileDTO dto = new ProfileDTO();
        dto.setUsername(account.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setGender(user.getGender());
        dto.setBirthday(user.getBirthday());

        return dto;
    }

    @Override
    public String getCurrentAccountRole(String username) {

        return accountDAO.getCurrentAccountRole(username);
    }

    @Override
    public String getCurrentAccountUsername() {

        return accountDAO.getCurrentAccountUsername();
    }



    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(AccountDTO dto) {
        Account account = mapToEntity(dto);
        accountDAO.register(account);
    }

    @Override
    public void createUser(AccountDTO dto) {
        Account account = mapToEntity(dto);
        accountDAO.createUser(account);
    }

    @Override
    public AccountDTO getAccountByUsername(String username) {
        return mapToDTO(accountDAO.getAccountByUsername(username));
    }

    @Override
    public AccountDTO getAccountByEmail(String email) {
        return mapToDTO(accountDAO.getAccountByEmail(email));
    }

    @Override
    public AccountDTO getAccountByPhone(String phone) {
        return mapToDTO(accountDAO.getAccountByPhone(phone));
    }

    @Override
    public AccountDTO getAccountById(String id) {
        return mapToDTO(accountDAO.getAccountById(id));
    }

    private Account mapToEntity(AccountDTO dto) {

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());

        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setUser(user);

        return account;
    }

    private AccountDTO mapToDTO(Account account) {
        if (account == null) return null;

        Person user = account.getUser();

        return new AccountDTO(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                account.getPassword(),
                account.getUsername(),
                user.getGender(),
                user.getBirthday()
        );
    }
}