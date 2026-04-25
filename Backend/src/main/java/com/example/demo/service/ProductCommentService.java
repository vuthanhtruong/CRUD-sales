package com.example.demo.service;

import com.example.demo.dto.ProductCommentDTO;
import com.example.demo.model.CommentStatus;

import java.util.List;

public interface ProductCommentService {
    List<ProductCommentDTO> publicThread(String productId);
    List<ProductCommentDTO> mine();
    ProductCommentDTO create(ProductCommentDTO dto);
    ProductCommentDTO markHelpful(String id);
    List<ProductCommentDTO> adminFindAll(CommentStatus status);
    ProductCommentDTO moderate(String id, CommentStatus status);
    void delete(String id);
}
