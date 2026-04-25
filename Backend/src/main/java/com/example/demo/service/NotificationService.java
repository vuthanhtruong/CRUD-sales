package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Person;

import java.util.List;

public interface NotificationService {
    NotificationDTO notifyUser(Person user, String title, String message, String type, String targetUrl);
    List<NotificationDTO> mine();
    long unreadCount();
    NotificationDTO markRead(String id);
    void markAllRead();
}
