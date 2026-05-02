package com.example.demo.repository;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.model.ProductImage;

import java.util.List;

public interface ProductImageDAO {
    void create(ProductImage image);
    void delete(ProductImage image);
    ProductImage findById(String id);
    ProductImageDTO findByIdDTO(String id);
    List<ProductImage> findByProductId(String productId);
    List<ProductImageDTO> findByProductIdDTO(String productId);
    void deleteByProductId(String productId);
    void createBatch(List<ProductImage> images);
    void deleteImagesByIds(List<String> ids);
    void setPrimary(String imageId);
    void clearPrimary(String productId);
}
