package com.example.demo.repository;

import com.example.demo.model.SupportTicket;
import com.example.demo.model.TicketMessage;
import com.example.demo.model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface SupportTicketDAO {
    SupportTicket save(SupportTicket ticket);
    TicketMessage saveMessage(TicketMessage message);
    Optional<SupportTicket> findById(String id);
    List<SupportTicket> findMine(String userId);
    List<SupportTicket> findAll(TicketStatus status);
}
