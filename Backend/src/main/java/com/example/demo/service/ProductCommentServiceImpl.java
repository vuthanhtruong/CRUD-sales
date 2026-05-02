package com.example.demo.service;

import com.example.demo.dto.ProductCommentDTO;
import com.example.demo.model.*;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.ProductCommentDAO;
import com.example.demo.repository.ProductDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Transactional
public class ProductCommentServiceImpl implements ProductCommentService {
    private final ProductCommentDAO commentDAO;
    private final ProductDAO productDAO;
    private final AccountDAO accountDAO;

    public ProductCommentServiceImpl(ProductCommentDAO commentDAO, ProductDAO productDAO, AccountDAO accountDAO) {
        this.commentDAO = commentDAO;
        this.productDAO = productDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public List<ProductCommentDTO> publicThread(String productId) {
        List<ProductCommentDTO> flat = commentDAO.findPublicByProductDTO(productId);
        Map<String, ProductCommentDTO> byId = new LinkedHashMap<>();
        List<ProductCommentDTO> roots = new ArrayList<>();

        for (ProductCommentDTO dto : flat) {
            dto.setReplies(new ArrayList<>());
            byId.put(dto.getId(), dto);
        }

        for (ProductCommentDTO dto : flat) {
            if (dto.getParentId() != null && byId.containsKey(dto.getParentId())) {
                byId.get(dto.getParentId()).getReplies().add(dto);
            } else {
                roots.add(dto);
            }
        }
        return roots;
    }

    @Override
    public List<ProductCommentDTO> mine() {
        return commentDAO.findMineDTO(currentUser().getId());
    }

    @Override
    public ProductCommentDTO create(ProductCommentDTO dto) {
        if (dto.getProductId() == null || dto.getProductId().isBlank()) throw new IllegalArgumentException("Product id is required");
        Product product = productDAO.findById(dto.getProductId());
        if (product == null) throw new RuntimeException("Product not found");

        ProductComment comment = new ProductComment();
        comment.setProduct(product);
        comment.setUser(currentUser());
        comment.setContent(dto.getContent() == null ? "" : dto.getContent().trim());
        comment.setStatus(CommentStatus.PUBLISHED);
        comment.setHelpfulCount(0);

        if (dto.getParentId() != null && !dto.getParentId().isBlank()) {
            ProductComment parent = commentDAO.findById(dto.getParentId()).orElseThrow(() -> new RuntimeException("Parent comment not found"));
            if (parent.getProduct() == null || !dto.getProductId().equals(parent.getProduct().getProductId())) {
                throw new IllegalArgumentException("Reply must belong to the same product");
            }
            comment.setParent(parent);
        }

        return toDTO(commentDAO.save(comment));
    }

    @Override
    public ProductCommentDTO markHelpful(String id) {
        ProductComment comment = commentDAO.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setHelpfulCount((comment.getHelpfulCount() == null ? 0 : comment.getHelpfulCount()) + 1);
        return toDTO(commentDAO.save(comment));
    }

    @Override
    public List<ProductCommentDTO> adminFindAll(CommentStatus status) {
        return commentDAO.findAllDTO(status);
    }

    @Override
    public ProductCommentDTO moderate(String id, CommentStatus status) {
        if (status == null) throw new IllegalArgumentException("Comment status is required");
        ProductComment comment = commentDAO.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus(status);
        return toDTO(commentDAO.save(comment));
    }

    @Override
    public void delete(String id) {
        ProductComment comment = commentDAO.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        String role = accountDAO.getCurrentAccountRole(accountDAO.getCurrentAccountUsername());
        Person user = currentUser();
        if (!"ADMIN".equals(role) && (comment.getUser() == null || !user.getId().equals(comment.getUser().getId()))) {
            throw new RuntimeException("You cannot delete this comment");
        }
        commentDAO.delete(id);
    }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private ProductCommentDTO toDTO(ProductComment c) {
        ProductCommentDTO dto = new ProductCommentDTO();
        dto.setId(c.getId());
        dto.setProductId(c.getProduct() == null ? null : c.getProduct().getProductId());
        dto.setProductName(c.getProduct() == null ? null : c.getProduct().getProductName());
        dto.setParentId(c.getParent() == null ? null : c.getParent().getId());
        dto.setContent(c.getContent());
        dto.setStatus(c.getStatus());
        dto.setHelpfulCount(c.getHelpfulCount() == null ? 0 : c.getHelpfulCount());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setReplies(new ArrayList<>());
        if (c.getUser() != null) {
            dto.setCustomerName((safe(c.getUser().getFirstName()) + " " + safe(c.getUser().getLastName())).trim());
            Account account = accountDAO.getAccountById(c.getUser().getId());
            dto.setUsername(account == null ? null : account.getUsername());
        }
        return dto;
    }

    private String safe(String value) { return value == null ? "" : value; }
}
