package com.example.demo.repository;

import com.example.demo.dto.ProductCommentDTO;
import com.example.demo.model.CommentStatus;
import com.example.demo.model.ProductComment;

import java.util.List;
import java.util.Optional;

public interface ProductCommentDAO {
    ProductComment save(ProductComment comment);
    Optional<ProductComment> findById(String id);
    List<ProductComment> findPublicByProduct(String productId);
    List<ProductCommentDTO> findPublicByProductDTO(String productId);
    List<ProductComment> findAll(CommentStatus status);
    List<ProductCommentDTO> findAllDTO(CommentStatus status);
    List<ProductComment> findMine(String userId);
    List<ProductCommentDTO> findMineDTO(String userId);
    void delete(String id);
}
