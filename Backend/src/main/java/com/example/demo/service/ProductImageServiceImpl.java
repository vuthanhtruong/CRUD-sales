package com.example.demo.service;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductImageDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {
    @Override
    public void deleteImagesByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<String> validIds = new ArrayList<>();

        for (String id : ids) {
            ProductImage image = productImageDAO.findById(id);
            if (image != null) {
                validIds.add(id);
            }
        }

        if (validIds.isEmpty()) {
            throw new RuntimeException("No valid images found to delete");
        }

        productImageDAO.deleteImagesByIds(validIds);
    }

    private final ProductImageDAO productImageDAO;
    private final ProductDAO productDAO;

    public ProductImageServiceImpl(ProductImageDAO productImageDAO,
                                   ProductDAO productDAO) {
        this.productImageDAO = productImageDAO;
        this.productDAO = productDAO;
    }

    // ================= CREATE 1 =================
    @Override
    public void create(ProductImageDTO dto) {

        Product product = productDAO.findById(dto.getProductId());

        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        ProductImage image = mapToEntity(dto, product);

        productImageDAO.create(image);
    }

    // ================= DELETE =================
    @Override
    public void delete(ProductImageDTO dto) {

        ProductImage image = productImageDAO.findById(dto.getId());

        if (image == null) {
            throw new RuntimeException("Image not found");
        }

        productImageDAO.delete(image);
    }

    // ================= FIND BY ID =================
    @Override
    public ProductImageDTO findById(String id) {

        ProductImage image = productImageDAO.findById(id);

        if (image == null) {
            throw new RuntimeException("Image not found");
        }

        return mapToDTO(image);
    }

    // ================= FIND BY PRODUCT =================
    @Override
    public List<ProductImageDTO> findByProductId(String productId) {

        List<ProductImage> images = productImageDAO.findByProductId(productId);

        List<ProductImageDTO> result = new ArrayList<>();

        for (ProductImage img : images) {
            result.add(mapToDTO(img));
        }

        return result;
    }

    // ================= DELETE BY PRODUCT =================
    @Override
    public void deleteByProductId(String productId) {
        productImageDAO.deleteByProductId(productId);
    }

    // ================= CREATE BATCH =================
    @Override
    public void createBatch(List<ProductImageDTO> dtos) {

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<ProductImage> images = new ArrayList<>();

        for (ProductImageDTO dto : dtos) {

            Product product = productDAO.findById(dto.getProductId());

            if (product == null) {
                throw new RuntimeException("Product not found: " + dto.getProductId());
            }

            ProductImage image = mapToEntity(dto, product);

            images.add(image);
        }

        // 🔥 validate: chỉ 1 ảnh primary
        long primaryCount = images.stream().filter(ProductImage::isPrimary).count();

        if (primaryCount > 1) {
            throw new RuntimeException("Only one primary image allowed");
        }

        productImageDAO.createBatch(images);
    }

    // ================= MAPPING =================
    private ProductImage mapToEntity(ProductImageDTO dto, Product product) {

        if (dto == null) {
            return null;
        }

        ProductImage image = new ProductImage();

        // nếu là update thì giữ lại id
        image.setId(dto.getId());

        // 🔥 convert Base64 → byte[]
        if (dto.getImageData() != null && !dto.getImageData().isBlank()) {
            byte[] imageBytes = java.util.Base64.getDecoder()
                    .decode(dto.getImageData());

            image.setImageData(imageBytes);
        } else {
            throw new RuntimeException("Image data cannot be empty");
        }

        // content type (image/png, image/jpeg…)
        image.setContentType(dto.getContentType());

        // primary flag
        image.setPrimary(dto.isPrimary());

        // 🔥 cực kỳ quan trọng: set product
        image.setProduct(product);

        return image;
    }

    private ProductImageDTO mapToDTO(ProductImage image) {

        if (image == null) return null;

        ProductImageDTO dto = new ProductImageDTO();

        dto.setId(image.getId());
        dto.setContentType(image.getContentType());
        dto.setPrimary(image.isPrimary());

        // 🔥 convert byte[] → Base64 string
        if (image.getImageData() != null) {
            String base64 = java.util.Base64.getEncoder()
                    .encodeToString(image.getImageData());
            dto.setImageData(base64);
        }

        if (image.getProduct() != null) {
            dto.setProductId(image.getProduct().getProductId());
        }

        return dto;
    }
}