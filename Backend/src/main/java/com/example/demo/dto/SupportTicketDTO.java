package com.example.demo.dto;

import com.example.demo.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketDTO {
    private String id;
    private String username;
    private String customerName;

    @NotBlank(message = "Subject is required")
    @Size(max = 180, message = "Subject must be at most 180 characters")
    private String subject;

    private String category;
    private String priority;
    private TicketStatus status;
    private String initialMessage;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SupportMessageDTO> messages = new ArrayList<>();
}
