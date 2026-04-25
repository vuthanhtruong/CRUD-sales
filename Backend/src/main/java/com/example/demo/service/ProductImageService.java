package com.example.demo.service;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.model.ProductImage;

import java.util.List;

public interface ProductImageService {
    void create(ProductImageDTO image);

    void delete(ProductImageDTO image);

    ProductImageDTO findById(String id);

    List<ProductImageDTO> findByProductId(String productId);

    void deleteByProductId(String productId);

    void createBatch(List<ProductImageDTO> images);

    void deleteImagesByIds(List<String> ids);
    void setPrimary(String imageId);
}
