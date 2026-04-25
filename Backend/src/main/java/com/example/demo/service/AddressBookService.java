package com.example.demo.service;

import com.example.demo.dto.AddressBookDTO;

import java.util.List;

public interface AddressBookService {
    List<AddressBookDTO> findMine();
    AddressBookDTO create(AddressBookDTO dto);
    AddressBookDTO update(String id, AddressBookDTO dto);
    void delete(String id);
    AddressBookDTO setDefault(String id);
}
