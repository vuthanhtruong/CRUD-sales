package com.example.demo.controller;

import com.example.demo.dto.AddressBookDTO;
import com.example.demo.service.AddressBookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "http://localhost:4200")
public class AddressBookController {
    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @GetMapping
    public ResponseEntity<List<AddressBookDTO>> findMine() { return ResponseEntity.ok(addressBookService.findMine()); }

    @PostMapping
    public ResponseEntity<AddressBookDTO> create(@RequestBody @Valid AddressBookDTO dto) { return ResponseEntity.ok(addressBookService.create(dto)); }

    @PutMapping("/{id}")
    public ResponseEntity<AddressBookDTO> update(@PathVariable String id, @RequestBody @Valid AddressBookDTO dto) { return ResponseEntity.ok(addressBookService.update(id, dto)); }

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressBookDTO> setDefault(@PathVariable String id) { return ResponseEntity.ok(addressBookService.setDefault(id)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) { addressBookService.delete(id); return ResponseEntity.noContent().build(); }
}
