package com.example.demo.controller;

import com.example.demo.dto.SizeDTO;
import com.example.demo.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@CrossOrigin(origins = "http://localhost:4200")
public class SizeController {

    @Autowired
    private SizeService sizeService;

    @PostMapping
    public ResponseEntity<SizeDTO> createSize(@RequestBody SizeDTO sizeDTO) {
        SizeDTO result = sizeService.CreateSize(sizeDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SizeDTO> getSize(@PathVariable String id) {
        SizeDTO result = sizeService.getSize(id);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SizeDTO> updateSize(
            @RequestBody SizeDTO sizeDTO,
            @PathVariable String id
    ) {
        SizeDTO result = sizeService.editSize(sizeDTO, id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteSize(@PathVariable String id) {
        boolean result = sizeService.deleteSize(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<SizeDTO>> getAllSizes() {
        List<SizeDTO> result = sizeService.getAllSizes();
        return ResponseEntity.ok(result);
    }
}