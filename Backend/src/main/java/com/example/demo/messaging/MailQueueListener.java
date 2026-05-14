package com.example.demo.messaging;

import com.example.demo.dto.queue.MailQueueMessageDTO;
import com.example.demo.service.MailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MailQueueListener {
    private static final Logger log = LoggerFactory.getLogger(MailQueueListener.class);

    private final MailSenderService mailSenderService;

    public MailQueueListener(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.mail-queue:mail.queue}")
    public void handle(MailQueueMessageDTO message) {
        if (message == null || isBlank(message.getTo()) || isBlank(message.getSubject()) || isBlank(message.getBody())) {
            log.warn("Skipped invalid mail queue message: {}", message);
            return;
        }

        try {
            mailSenderService.send(message);
            log.info("Queued email sent to {} with subject {}", message.getTo(), message.getSubject());
        } catch (Exception ex) {
            log.error("Could not send queued email to {}. Message will be dead-lettered.", message.getTo(), ex);
            throw new AmqpRejectAndDontRequeueException("Could not send queued email", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
