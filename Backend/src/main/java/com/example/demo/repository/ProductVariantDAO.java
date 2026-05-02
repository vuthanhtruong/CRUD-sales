package com.example.demo.repository;

import com.example.demo.dto.ColorDTO;
import com.example.demo.dto.ProductVariantDTO;
import com.example.demo.dto.SizeDTO;
import com.example.demo.model.Color;
import com.example.demo.model.ProductVariant;
import com.example.demo.model.ProductVariantId;
import com.example.demo.model.Size;

import java.util.List;

public interface ProductVariantDAO {

    List<ProductVariant> findAll();
    List<ProductVariantDTO> findAllDTO();

    ProductVariant findById(ProductVariantId id);
    ProductVariantDTO findByIdDTO(ProductVariantId id);

    List<ProductVariant> findByProductId(String productId);
    List<ProductVariantDTO> findByProductIdDTO(String productId);

    void create(ProductVariant variant);

    void update(ProductVariant variant);

    void delete(ProductVariant variant);

    List<ProductVariant> createBatch(List<ProductVariant> variants);

    List<Size> findUnusedSizesByProductId(String productId);
    List<SizeDTO> findUnusedSizesByProductIdDTO(String productId);

    List<Color> findUnusedColorsByProductId(String productId);
    List<ColorDTO> findUnusedColorsByProductIdDTO(String productId);

    List<Size> findSizesByProductId(String productId);
    List<SizeDTO> findSizesByProductIdDTO(String productId);

    List<Color> findColorsByProductId(String productId);
    List<ColorDTO> findColorsByProductIdDTO(String productId);

    int decreaseQuantity(String productId, String sizeId, String colorId, int amount);
    int increaseQuantity(String productId, String sizeId, String colorId, int amount);

    int getQuantity(String productId, String sizeId, String colorId);

    boolean existsAvailableStockByProductId(String productId);

    int getTotalQuantityByProductId(String productId);

}
