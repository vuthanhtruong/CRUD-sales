package com.example.demo.controller;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) { this.notificationService = notificationService; }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> mine() { return ResponseEntity.ok(notificationService.mine()); }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() { return ResponseEntity.ok(Map.of("count", notificationService.unreadCount())); }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markRead(@PathVariable String id) { return ResponseEntity.ok(notificationService.markRead(id)); }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() { notificationService.markAllRead(); return ResponseEntity.noContent().build(); }
}
