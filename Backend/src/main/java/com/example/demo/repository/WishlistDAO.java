package com.example.demo.repository;

import com.example.demo.dto.WishlistItemDTO;
import com.example.demo.model.WishlistItem;

import java.util.List;
import java.util.Optional;

public interface WishlistDAO {
    WishlistItem save(WishlistItem item);
    List<WishlistItem> findByUserId(String userId);
    List<WishlistItemDTO> findByUserIdDTO(String userId);
    Optional<WishlistItem> findByUserAndProduct(String userId, String productId);
    Optional<WishlistItemDTO> findByUserAndProductDTO(String userId, String productId);
    void delete(String id);
    void deleteByUserAndProduct(String userId, String productId);
    long countByProduct(String productId);
    long countByUser(String userId);
}
