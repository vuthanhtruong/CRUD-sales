package com.example.demo.repository;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.dto.SupportTicketDTO;
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
    List<SupportTicketDTO> findMineDTO(String userId);
    List<SupportTicket> findAll(TicketStatus status);
    List<SupportTicketDTO> findAllDTO(TicketStatus status);
    List<SupportMessageDTO> findMessagesByTicketIdDTO(String ticketId);
}
