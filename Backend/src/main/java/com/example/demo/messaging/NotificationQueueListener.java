package com.example.demo.messaging;

import com.example.demo.dto.queue.NotificationQueueMessageDTO;
import com.example.demo.model.Notification;
import com.example.demo.model.Person;
import com.example.demo.repository.NotificationDAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationQueueListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationQueueListener.class);

    private final NotificationDAO notificationDAO;

    @PersistenceContext
    private EntityManager entityManager;

    public NotificationQueueListener(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @RabbitListener(queues = "${app.rabbitmq.notification-queue:notification.queue}")
    @Transactional
    public void handle(NotificationQueueMessageDTO message) {
        if (message == null || isBlank(message.getUserId()) || isBlank(message.getTitle()) || isBlank(message.getMessage())) {
            log.warn("Skipped invalid notification queue message: {}", message);
            return;
        }

        Person user = entityManager.find(Person.class, message.getUserId());
        if (user == null) {
            log.warn("Skipped notification queue message because user {} was not found", message.getUserId());
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(message.getTitle());
        notification.setMessage(message.getMessage());
        notification.setType(message.getType());
        notification.setTargetUrl(message.getTargetUrl());
        notification.setRead(false);
        notificationDAO.save(notification);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
