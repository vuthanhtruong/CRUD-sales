package com.example.demo.repository;

import com.example.demo.dto.NotificationDTO;
import com.example.demo.model.Notification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class NotificationDAOImpl implements NotificationDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) entityManager.persist(notification);
        else notification = entityManager.merge(notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(String id) { return Optional.ofNullable(entityManager.find(Notification.class, id)); }

    @Override
    public List<Notification> findByUserId(String userId) {
        return entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC",
                        Notification.class)
                .setParameter("userId", userId)
                .setMaxResults(50)
                .getResultList();
    }

    @Override
    public long unreadCount(String userId) {
        return entityManager.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public void markAllRead(String userId) {
        entityManager.createQuery("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public List<NotificationDTO> findByUserIdDTO(String userId) {
        return entityManager.createQuery(
                        "SELECT new com.example.demo.dto.NotificationDTO(n.id, n.title, n.message, n.type, n.targetUrl, n.read, n.createdAt) " +
                                "FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC",
                        NotificationDTO.class)
                .setParameter("userId", userId)
                .getResultList();
    }

}
