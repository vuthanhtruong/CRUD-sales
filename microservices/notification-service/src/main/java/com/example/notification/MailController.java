package com.example.notification;

import com.example.demo.dto.queue.MailQueueMessageDTO;
import com.example.notification.service.MailSenderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
public class MailController {
    private final MailSenderService mailSenderService;

    public MailController(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody @Valid MailQueueMessageDTO request) {
        mailSenderService.send(request);
        return ResponseEntity.ok(Map.of("message", "Email sent"));
    }
}
