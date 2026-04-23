package com.example.demo.service;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Color;
import com.example.demo.model.Size;

import java.util.List;

public interface ProductVariantService {
    List<ProductVariantDTO> findAll();

    ProductVariantDTO findById(String productId, String sizeId, String colorId);

    List<ProductVariantDTO> findByProductId(String productId);

    List<ProductVariantDTO> createBatch(List<ProductVariantDTO> dtos);

    ProductVariantDTO create(ProductVariantDTO dto);

    ProductVariantDTO update(ProductVariantDTO dto);

    boolean delete(String productId, String sizeId, String colorId);

    List<SizeDTO> findUnusedSizesByProductId(String productId);

    List<ColorDTO> findUnusedColorsByProductId(String productId);

    List<SizeDTO> findSizesByProductId(String productId);

    List<ColorDTO> findColorsByProductId(String productId);
}
