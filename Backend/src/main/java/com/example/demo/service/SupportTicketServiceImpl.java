package com.example.demo.service;

import com.example.demo.dto.SupportMessageDTO;
import com.example.demo.dto.SupportTicketDTO;
import com.example.demo.model.*;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.SupportTicketDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {
    private final SupportTicketDAO ticketDAO;
    private final AccountDAO accountDAO;

    public SupportTicketServiceImpl(SupportTicketDAO ticketDAO, AccountDAO accountDAO) {
        this.ticketDAO = ticketDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public List<SupportTicketDTO> mine() {
        return hydrateTickets(ticketDAO.findMineDTO(currentUser().getId()));
    }

    @Override
    public SupportTicketDTO create(SupportTicketDTO dto) {
        Person user = currentUser();
        SupportTicket ticket = new SupportTicket();
        ticket.setUser(user);
        ticket.setSubject(dto.getSubject().trim());
        ticket.setCategory(blankToDefault(dto.getCategory(), "GENERAL"));
        ticket.setPriority(blankToDefault(dto.getPriority(), "NORMAL"));
        ticket.setStatus(TicketStatus.WAITING_ADMIN);

        TicketMessage first = new TicketMessage();
        first.setSender(user);
        first.setMessage(blankToDefault(dto.getInitialMessage(), dto.getSubject()));
        first.setInternalNote(false);
        ticket.addMessage(first);
        return toDTO(ticketDAO.save(ticket));
    }

    @Override
    public SupportTicketDTO addMessage(String ticketId, SupportMessageDTO dto) {
        SupportTicket ticket = ticketDAO.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        Person sender = currentUser();
        String role = accountDAO.getCurrentAccountRole(accountDAO.getCurrentAccountUsername());
        boolean admin = "ADMIN".equals(role);

        if (!admin && (ticket.getUser() == null || !ticket.getUser().getId().equals(sender.getId()))) {
            throw new RuntimeException("You cannot message in this ticket");
        }

        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(dto.getMessage().trim());
        message.setInternalNote(admin && dto.isInternalNote());
        ticketDAO.saveMessage(message);

        if (!message.isInternalNote()) {
            ticket.setStatus(admin ? TicketStatus.WAITING_CUSTOMER : TicketStatus.WAITING_ADMIN);
            ticketDAO.save(ticket);
        }
        return toDTO(ticketDAO.findById(ticketId).orElse(ticket));
    }

    @Override
    public List<SupportTicketDTO> adminFindAll(TicketStatus status) {
        return hydrateTickets(ticketDAO.findAllDTO(status));
    }

    @Override
    public SupportTicketDTO updateStatus(String ticketId, TicketStatus status) {
        SupportTicket ticket = ticketDAO.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        return toDTO(ticketDAO.save(ticket));
    }


    private List<SupportTicketDTO> hydrateTickets(List<SupportTicketDTO> tickets) {
        for (SupportTicketDTO ticket : tickets) {
            List<SupportMessageDTO> messages = ticketDAO.findMessagesByTicketIdDTO(ticket.getId());
            ticket.setMessages(messages);
            if (!messages.isEmpty()) {
                ticket.setLastMessage(messages.get(messages.size() - 1).getMessage());
                if (ticket.getInitialMessage() == null) {
                    ticket.setInitialMessage(messages.get(0).getMessage());
                }
            }
        }
        return tickets;
    }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private SupportTicketDTO toDTO(SupportTicket t) {
        SupportTicketDTO dto = new SupportTicketDTO();
        dto.setId(t.getId());
        dto.setSubject(t.getSubject());
        dto.setCategory(t.getCategory());
        dto.setPriority(t.getPriority());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        if (t.getUser() != null) {
            dto.setCustomerName((safe(t.getUser().getFirstName()) + " " + safe(t.getUser().getLastName())).trim());
            Account account = accountDAO.getAccountById(t.getUser().getId());
            dto.setUsername(account == null ? null : account.getUsername());
        }
        List<SupportMessageDTO> messages = t.getMessages() == null ? List.of() : t.getMessages().stream()
                .sorted(Comparator.comparing(TicketMessage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toMessageDTO)
                .toList();
        dto.setMessages(messages);
        if (!messages.isEmpty()) dto.setLastMessage(messages.get(messages.size() - 1).getMessage());
        return dto;
    }

    private SupportMessageDTO toMessageDTO(TicketMessage m) {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setId(m.getId());
        dto.setTicketId(m.getTicket() == null ? null : m.getTicket().getId());
        dto.setMessage(m.getMessage());
        dto.setInternalNote(m.isInternalNote());
        dto.setCreatedAt(m.getCreatedAt());
        if (m.getSender() != null) {
            dto.setSenderName((safe(m.getSender().getFirstName()) + " " + safe(m.getSender().getLastName())).trim());
            Account account = accountDAO.getAccountById(m.getSender().getId());
            dto.setUsername(account == null ? null : account.getUsername());
        }
        return dto;
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safe(String value) { return value == null ? "" : value; }
}
