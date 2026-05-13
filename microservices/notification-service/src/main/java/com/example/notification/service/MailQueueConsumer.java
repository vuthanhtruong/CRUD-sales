package com.example.notification.service;

import com.example.demo.dto.queue.MailQueueMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MailQueueConsumer {
    private static final Logger log = LoggerFactory.getLogger(MailQueueConsumer.class);

    private final MailSenderService mailSenderService;

    public MailQueueConsumer(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.mail-queue:mail.queue}")
    public void consume(MailQueueMessageDTO message) {
        if (message == null || isBlank(message.getTo()) || isBlank(message.getSubject()) || isBlank(message.getBody())) {
            log.warn("Skipped invalid mail queue message: {}", message);
            return;
        }

        mailSenderService.send(message);
        log.info("Queued email sent to {} with subject {}", message.getTo(), message.getSubject());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
