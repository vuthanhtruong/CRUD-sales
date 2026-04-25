package com.example.demo.service;

import com.example.demo.dto.AddressBookDTO;
import com.example.demo.model.AddressBook;
import com.example.demo.model.Person;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.AddressBookDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class AddressBookServiceImpl implements AddressBookService {
    private final AddressBookDAO addressBookDAO;
    private final AccountDAO accountDAO;

    public AddressBookServiceImpl(AddressBookDAO addressBookDAO, AccountDAO accountDAO) {
        this.addressBookDAO = addressBookDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public List<AddressBookDTO> findMine() {
        return addressBookDAO.findByUserId(currentUser().getId()).stream().map(this::toDTO).toList();
    }

    @Override
    public AddressBookDTO create(AddressBookDTO dto) {
        Person user = currentUser();
        AddressBook address = new AddressBook();
        copy(dto, address);
        address.setUser(user);
        if (dto.isDefaultAddress()) addressBookDAO.clearDefault(user.getId());
        AddressBook saved = addressBookDAO.save(address);
        return toDTO(saved);
    }

    @Override
    public AddressBookDTO update(String id, AddressBookDTO dto) {
        Person user = currentUser();
        AddressBook address = getOwned(id, user.getId());
        copy(dto, address);
        if (dto.isDefaultAddress()) addressBookDAO.clearDefault(user.getId());
        AddressBook saved = addressBookDAO.save(address);
        return toDTO(saved);
    }

    @Override
    public void delete(String id) {
        getOwned(id, currentUser().getId());
        addressBookDAO.delete(id);
    }

    @Override
    public AddressBookDTO setDefault(String id) {
        Person user = currentUser();
        AddressBook address = getOwned(id, user.getId());
        addressBookDAO.clearDefault(user.getId());
        address.setDefaultAddress(true);
        return toDTO(addressBookDAO.save(address));
    }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private AddressBook getOwned(String id, String userId) {
        AddressBook address = addressBookDAO.findById(id).orElseThrow(() -> new RuntimeException("Address not found"));
        if (address.getUser() == null || !userId.equals(address.getUser().getId())) throw new RuntimeException("Bạn không có quyền thao tác địa chỉ này");
        return address;
    }

    private void copy(AddressBookDTO dto, AddressBook address) {
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setFullAddress(dto.getFullAddress());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setWard(dto.getWard());
        address.setLabel(dto.getLabel());
        address.setDefaultAddress(dto.isDefaultAddress());
    }

    private AddressBookDTO toDTO(AddressBook a) {
        return new AddressBookDTO(a.getId(), a.getReceiverName(), a.getReceiverPhone(), a.getFullAddress(), a.getCity(), a.getDistrict(), a.getWard(), a.getLabel(), a.isDefaultAddress(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
