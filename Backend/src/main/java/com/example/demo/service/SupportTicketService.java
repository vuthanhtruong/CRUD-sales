package com.example.demo.service;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.dto.SupportTicketDTO;
import com.example.demo.model.TicketStatus;

import java.util.List;

public interface SupportTicketService {
    List<SupportTicketDTO> mine();
    SupportTicketDTO create(SupportTicketDTO dto);
    SupportTicketDTO addMessage(String ticketId, SupportMessageDTO dto);
    List<SupportTicketDTO> adminFindAll(TicketStatus status);
    SupportTicketDTO updateStatus(String ticketId, TicketStatus status);
}
