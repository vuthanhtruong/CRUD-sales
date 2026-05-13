package com.example.demo.dto.queue;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueueMessageDTO {
    @NotBlank
    private String userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String type;
    private String targetUrl;
}
