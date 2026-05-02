package com.example.demo.repository;

import com.example.demo.dto.AccountDTO;
import com.example.demo.dto.ProfileDTO;
import com.example.demo.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class AccountDAOImpl implements AccountDAO {
    private static final String ACCOUNT_DTO_SELECT =
            "SELECT new com.example.demo.dto.AccountDTO(u.firstName, u.lastName, u.phone, u.email, " +
                    "u.avatarUrl, u.address, a.username, '', u.gender, u.birthday) FROM Account a JOIN a.user u ";

    private static final String PROFILE_DTO_SELECT =
            "SELECT new com.example.demo.dto.ProfileDTO(a.username, u.firstName, u.lastName, u.phone, u.email, " +
                    "u.avatarUrl, u.address, u.gender, u.birthday) FROM Account a JOIN a.user u ";

    @Override
    public Person getCurrentUser() {
        return getAccountByUsername(getCurrentAccountUsername()).getUser();
    }

    private final CartDAO cartDAO;

    public AccountDAOImpl(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Account getProfileByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a " +
                                "JOIN FETCH a.user u " +
                                "WHERE a.username = :username",
                        Account.class
                )
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public ProfileDTO getProfileDTOByUsername(String username) {
        return entityManager.createQuery(
                        PROFILE_DTO_SELECT + "WHERE a.username = :username",
                        ProfileDTO.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateProfile(String username, Person updatedPerson) {
        Account account = getAccountByUsername(username);

        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        Person existingPerson = account.getUser();

        if (existingPerson == null) {
            throw new RuntimeException("User profile not found");
        }
        existingPerson.setFirstName(updatedPerson.getFirstName());
        existingPerson.setLastName(updatedPerson.getLastName());
        existingPerson.setPhone(updatedPerson.getPhone());
        existingPerson.setEmail(updatedPerson.getEmail());
        existingPerson.setAvatarUrl(updatedPerson.getAvatarUrl());
        existingPerson.setAddress(updatedPerson.getAddress());
        existingPerson.setGender(updatedPerson.getGender());
        existingPerson.setBirthday(updatedPerson.getBirthday());

        entityManager.merge(existingPerson);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void createUser(Account account) {
        Person user = account.getUser();
        if (user == null) {
            throw new RuntimeException("User must not be null");
        }

        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }

        entityManager.persist(user);

        account.setId(user.getId());
        account.setCreatedTime(LocalDate.now());

        entityManager.persist(account);
    }

    @Override
    public void register(Account account) {

        if (account.getUser() == null) {
            throw new RuntimeException("User must not be null");
        }

        if (account.getUsername() == null || account.getPassword() == null) {
            throw new RuntimeException("Username and password are required");
        }

        if (getAccountByUsername(account.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }

        Person user = account.getUser();

        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }

        // 1. save user
        entityManager.persist(user);

        // 2. setup account
        account.setId(user.getId());
        account.setCreatedTime(LocalDate.now());

        // account.setPassword(passwordEncoder.encode(account.getPassword())); // nếu có encoder

        // 3. save account
        entityManager.persist(account);

        // 4. AUTO CREATE CART
        Cart cart = new Cart();
        cart.setUser(user); // cast vì Cart dùng User

        cartDAO.create(cart);
    }

    @Override
    public Account getAccountByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT a FROM Account a JOIN FETCH a.user u WHERE a.username = :username",
                        Account.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public AccountDTO getAccountDTOByUsername(String username) {
        return entityManager.createQuery(
                        ACCOUNT_DTO_SELECT + "WHERE a.username = :username",
                        AccountDTO.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Account getAccountByEmail(String email) {
        TypedQuery<Account> query = entityManager.createQuery(
                "SELECT a FROM Account a WHERE a.user.email = :email",
                Account.class);
        query.setParameter("email", email);
        List<Account> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public AccountDTO getAccountDTOByEmail(String email) {
        return entityManager.createQuery(
                        ACCOUNT_DTO_SELECT + "WHERE u.email = :email",
                        AccountDTO.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Account getAccountByPhone(String phone) {
        TypedQuery<Account> query = entityManager.createQuery(
                "SELECT a FROM Account a WHERE a.user.phone = :phone",
                Account.class);
        query.setParameter("phone", phone);
        List<Account> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public AccountDTO getAccountDTOByPhone(String phone) {
        return entityManager.createQuery(
                        ACCOUNT_DTO_SELECT + "WHERE u.phone = :phone",
                        AccountDTO.class)
                .setParameter("phone", phone)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public Account getAccountById(String id) {
        return entityManager.find(Account.class, id);
    }

    @Override
    public AccountDTO getAccountDTOById(String id) {
        return entityManager.createQuery(
                        ACCOUNT_DTO_SELECT + "WHERE a.id = :id",
                        AccountDTO.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getCurrentAccountUsername() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }

        return authentication.getName().toString();
    }

    @Override
    public String getCurrentAccountRole(String username) {

        Account account = getAccountByUsername(username);

        if (account.getUser() instanceof Admin) {
            return "ADMIN";
        }

        if (account.getUser() instanceof User) {
            return "USER";
        }

        return "UNKNOWN";
    }

    
    public void updatePassword(String accountId, String encodedPassword) {
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new RuntimeException("Account not found");
        }
        account.setPassword(encodedPassword);
        entityManager.merge(account);
    }

}