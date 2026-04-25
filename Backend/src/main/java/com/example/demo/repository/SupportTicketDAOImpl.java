package com.example.demo.repository;

import com.example.demo.model.SupportTicket;
import com.example.demo.model.TicketMessage;
import com.example.demo.model.TicketStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class SupportTicketDAOImpl implements SupportTicketDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SupportTicket save(SupportTicket ticket) {
        if (ticket.getId() == null) {
            entityManager.persist(ticket);
            return ticket;
        }
        return entityManager.merge(ticket);
    }

    @Override
    public TicketMessage saveMessage(TicketMessage message) {
        if (message.getId() == null) {
            entityManager.persist(message);
            return message;
        }
        return entityManager.merge(message);
    }

    @Override
    public Optional<SupportTicket> findById(String id) {
        return entityManager.createQuery(
                        "SELECT DISTINCT t FROM SupportTicket t " +
                                "LEFT JOIN FETCH t.user u " +
                                "LEFT JOIN FETCH t.messages m " +
                                "LEFT JOIN FETCH m.sender s " +
                                "WHERE t.id = :id",
                        SupportTicket.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<SupportTicket> findMine(String userId) {
        return entityManager.createQuery(
                        "SELECT DISTINCT t FROM SupportTicket t LEFT JOIN FETCH t.messages m " +
                                "WHERE t.user.id = :userId ORDER BY t.updatedAt DESC",
                        SupportTicket.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<SupportTicket> findAll(TicketStatus status) {
        String jpql = "SELECT DISTINCT t FROM SupportTicket t LEFT JOIN FETCH t.user u LEFT JOIN FETCH t.messages m ";
        if (status != null) jpql += "WHERE t.status = :status ";
        jpql += "ORDER BY t.updatedAt DESC";
        var query = entityManager.createQuery(jpql, SupportTicket.class);
        if (status != null) query.setParameter("status", status);
        return query.getResultList();
    }
}
