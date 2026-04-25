package com.example.demo.repository;

import com.example.demo.model.AddressBook;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class AddressBookDAOImpl implements AddressBookDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AddressBook save(AddressBook address) {
        if (address.getId() == null) {
            entityManager.persist(address);
            return address;
        }
        return entityManager.merge(address);
    }

    @Override
    public Optional<AddressBook> findById(String id) {
        return Optional.ofNullable(entityManager.find(AddressBook.class, id));
    }

    @Override
    public List<AddressBook> findByUserId(String userId) {
        return entityManager.createQuery(
                        "SELECT a FROM AddressBook a WHERE a.user.id = :userId ORDER BY a.defaultAddress DESC, a.updatedAt DESC",
                        AddressBook.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void clearDefault(String userId) {
        entityManager.createQuery("UPDATE AddressBook a SET a.defaultAddress = false WHERE a.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void delete(String id) {
        AddressBook address = entityManager.find(AddressBook.class, id);
        if (address != null) entityManager.remove(address);
    }
}
