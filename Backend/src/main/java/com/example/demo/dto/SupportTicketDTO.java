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
    public SupportTicketDTO(String id, String username, String customerName, String subject, String category, String priority,
                            TicketStatus status, String initialMessage, String lastMessage, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.customerName = customerName;
        this.subject = subject;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.initialMessage = initialMessage;
        this.lastMessage = lastMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = new ArrayList<>();
    }

}
