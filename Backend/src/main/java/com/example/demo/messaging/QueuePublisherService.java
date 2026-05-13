package com.example.demo.messaging;

import com.example.demo.dto.queue.MailQueueMessageDTO;
import com.example.demo.dto.queue.NotificationQueueMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QueuePublisherService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:sale.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.mail-routing-key:mail.send}")
    private String mailRoutingKey;

    @Value("${app.rabbitmq.notification-routing-key:notification.create}")
    private String notificationRoutingKey;

    public QueuePublisherService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishMail(MailQueueMessageDTO message) {
        rabbitTemplate.convertAndSend(exchangeName, mailRoutingKey, message);
    }

    public void publishNotification(NotificationQueueMessageDTO message) {
        rabbitTemplate.convertAndSend(exchangeName, notificationRoutingKey, message);
    }
}
