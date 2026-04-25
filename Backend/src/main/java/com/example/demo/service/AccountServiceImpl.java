package com.example.demo.service;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.Account;
import com.example.demo.model.Person;
import com.example.demo.model.User;
import com.example.demo.repository.AccountDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountDAO accountDAO;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(AccountDAO accountDAO, PasswordEncoder passwordEncoder) {
        this.accountDAO = accountDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(AccountDTO dto) {
        validateUnique(dto, null);
        accountDAO.register(mapToEntity(dto));
    }

    @Override
    public void createUser(AccountDTO dto) {
        validateUnique(dto, null);
        accountDAO.createUser(mapToEntity(dto));
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

    @Override
    public String getCurrentAccountUsername() {
        return accountDAO.getCurrentAccountUsername();
    }

    @Override
    public String getCurrentAccountRole(String username) {
        return accountDAO.getCurrentAccountRole(username);
    }

    @Override
    public ProfileDTO getProfileByUsername(String username) {
        Account account = accountDAO.getProfileByUsername(username);
        if (account == null || account.getUser() == null) throw new RuntimeException("Profile not found");
        Person user = account.getUser();
        return new ProfileDTO(
                account.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getAddress(),
                user.getGender(),
                user.getBirthday()
        );
    }

    @Override
    public void updateProfile(String username, ProfileDTO profile) {
        Account account = accountDAO.getAccountByUsername(username);
        if (account == null || account.getUser() == null) throw new RuntimeException("Account or profile not found");

        validateProfileUnique(profile, account.getId());

        Person person = account.getUser();
        person.setFirstName(profile.getFirstName());
        person.setLastName(profile.getLastName());
        person.setPhone(profile.getPhone());
        person.setEmail(profile.getEmail());
        person.setAddress(profile.getAddress());
        person.setGender(profile.getGender());
        person.setBirthday(profile.getBirthday());
        accountDAO.updateProfile(username, person);
    }

    private Account mapToEntity(AccountDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
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
        if (account == null || account.getUser() == null) return null;
        Person user = account.getUser();
        return new AccountDTO(
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getAddress(),
                account.getUsername(),
                null,
                user.getGender(),
                user.getBirthday()
        );
    }

    private void validateUnique(AccountDTO dto, String currentId) {
        if (accountDAO.getAccountByUsername(dto.getUsername()) != null) throw new RuntimeException("Username already exists");
        validateProfileUnique(new ProfileDTO(dto.getUsername(), dto.getFirstName(), dto.getLastName(), dto.getPhone(), dto.getEmail(), dto.getAddress(), dto.getGender(), dto.getBirthday()), currentId);
    }

    private void validateProfileUnique(ProfileDTO dto, String currentId) {
        Account emailOwner = accountDAO.getAccountByEmail(dto.getEmail());
        if (emailOwner != null && !emailOwner.getId().equals(currentId)) throw new RuntimeException("Email already exists");
        Account phoneOwner = accountDAO.getAccountByPhone(dto.getPhone());
        if (phoneOwner != null && !phoneOwner.getId().equals(currentId)) throw new RuntimeException("Phone already exists");
    }
}
