package com.example.demo.repository;

import com.example.demo.dto.AddressBookDTO;
import com.example.demo.model.AddressBook;

import java.util.List;
import java.util.Optional;

public interface AddressBookDAO {
    AddressBook save(AddressBook address);
    Optional<AddressBook> findById(String id);
    List<AddressBook> findByUserId(String userId);
    List<AddressBookDTO> findByUserIdDTO(String userId);
    void clearDefault(String userId);
    void delete(String id);
}
