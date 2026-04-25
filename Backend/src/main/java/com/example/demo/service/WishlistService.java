package com.example.demo.service;

import com.example.demo.dto.WishlistItemDTO;

import java.util.List;
import java.util.Map;

public interface WishlistService {
    List<WishlistItemDTO> findMine();
    WishlistItemDTO add(String productId);
    void remove(String productId);
    Map<String, Object> status(String productId);
}
