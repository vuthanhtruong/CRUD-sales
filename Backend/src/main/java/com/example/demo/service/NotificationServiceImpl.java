package com.example.demo.service;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;
import com.example.demo.model.Person;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.NotificationDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationDAO notificationDAO;
    private final AccountDAO accountDAO;

    public NotificationServiceImpl(NotificationDAO notificationDAO, AccountDAO accountDAO) {
        this.notificationDAO = notificationDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public NotificationDTO notifyUser(Person user, String title, String message, String type, String targetUrl) {
        if (user == null) return null;
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setTargetUrl(targetUrl);
        n.setRead(false);
        return toDTO(notificationDAO.save(n));
    }

    @Override
    public List<NotificationDTO> mine() { return notificationDAO.findByUserId(currentUser().getId()).stream().map(this::toDTO).toList(); }

    @Override
    public long unreadCount() { return notificationDAO.unreadCount(currentUser().getId()); }

    @Override
    public NotificationDTO markRead(String id) {
        Person user = currentUser();
        Notification n = notificationDAO.findById(id).orElseThrow(() -> new RuntimeException("Notification not found"));
        if (n.getUser() == null || !user.getId().equals(n.getUser().getId())) throw new RuntimeException("Bạn không có quyền đọc thông báo này");
        n.setRead(true);
        return toDTO(notificationDAO.save(n));
    }

    @Override
    public void markAllRead() { notificationDAO.markAllRead(currentUser().getId()); }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private NotificationDTO toDTO(Notification n) { return new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.getTargetUrl(), n.isRead(), n.getCreatedAt()); }
}
