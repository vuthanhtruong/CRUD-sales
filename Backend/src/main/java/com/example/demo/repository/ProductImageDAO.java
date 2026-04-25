package com.example.demo.repository;

import com.example.demo.model.ProductImage;

import java.util.List;

public interface ProductImageDAO {
    void create(ProductImage image);
    void delete(ProductImage image);
    ProductImage findById(String id);
    List<ProductImage> findByProductId(String productId);
    void deleteByProductId(String productId);
    void createBatch(List<ProductImage> images);
    void deleteImagesByIds(List<String> ids);
    void setPrimary(String imageId);
    void clearPrimary(String productId);
}
