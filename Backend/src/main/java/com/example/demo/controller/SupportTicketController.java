package com.example.demo.controller;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.dto.SupportTicketDTO;
import com.example.demo.model.TicketStatus;
import com.example.demo.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "http://localhost:4200")
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<SupportTicketDTO>> mine() {
        return ResponseEntity.ok(supportTicketService.mine());
    }

    @PostMapping
    public ResponseEntity<SupportTicketDTO> create(@RequestBody @Valid SupportTicketDTO dto) {
        return ResponseEntity.ok(supportTicketService.create(dto));
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<SupportTicketDTO> addMessage(@PathVariable String ticketId, @RequestBody @Valid SupportMessageDTO dto) {
        return ResponseEntity.ok(supportTicketService.addMessage(ticketId, dto));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<SupportTicketDTO>> adminFindAll(@RequestParam(required = false) TicketStatus status) {
        return ResponseEntity.ok(supportTicketService.adminFindAll(status));
    }

    @PutMapping("/admin/{ticketId}/status")
    public ResponseEntity<SupportTicketDTO> updateStatus(@PathVariable String ticketId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(supportTicketService.updateStatus(ticketId, TicketStatus.valueOf(body.get("status"))));
    }
}
