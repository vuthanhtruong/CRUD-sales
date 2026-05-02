package com.example.demo.repository;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationDAO {
    Notification save(Notification notification);
    Optional<Notification> findById(String id);
    List<Notification> findByUserId(String userId);
    List<NotificationDTO> findByUserIdDTO(String userId);
    long unreadCount(String userId);
    void markAllRead(String userId);
}
