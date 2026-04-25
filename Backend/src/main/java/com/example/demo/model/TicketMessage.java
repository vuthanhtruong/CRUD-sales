package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_message", indexes = {@Index(name = "idx_ticket_message_ticket_created", columnList = "ticket_id, created_at")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id")
    private SupportTicket ticket;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private Person sender;

    @Column(nullable = false, length = 3000)
    private String message;

    @Column(nullable = false)
    private boolean internalNote = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
