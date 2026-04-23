package com.example.demo.repository;

import com.example.demo.model.Account;
import com.example.demo.model.Admin;
import com.example.demo.model.Person;
import com.example.demo.model.User;
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

        // Mã hóa password trước khi lưu
        account.setPassword(account.getPassword());

        createUser(account);
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
    public Account getAccountByEmail(String email) {
        TypedQuery<Account> query = entityManager.createQuery(
                "SELECT a FROM Account a WHERE a.user.email = :email",
                Account.class);
        query.setParameter("email", email);
        List<Account> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
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
    public Account getAccountById(String id) {
        return entityManager.find(Account.class, id);
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

}