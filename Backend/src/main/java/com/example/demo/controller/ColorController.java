package com.example.demo.controller;

import com.example.demo.dto.ColorDTO;
import com.example.demo.service.ColorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colors")
@CrossOrigin(origins = "http://localhost:4200")
public class ColorController {

    private final ColorService service;

    public ColorController(ColorService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ColorDTO dto) {
        service.createColor(dto);
        return ResponseEntity.ok(
                Map.of("message", "Created successfully")
        );
    }

    @GetMapping
    public ResponseEntity<List<ColorDTO>> getAll() {
        return ResponseEntity.ok(service.findAllColor());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        ColorDTO dto = service.getColorbyId(id);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable String id,
            @RequestBody ColorDTO dto
    ) {
        dto.setId(id);
        service.editColor(dto);
        return ResponseEntity.ok("Updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.deleteColor(id);
        return ResponseEntity.ok(
                Map.of("message", "Deleted successfully")
        );
    }
}