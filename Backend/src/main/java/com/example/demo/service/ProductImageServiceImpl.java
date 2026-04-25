package com.example.demo.service;

import com.example.demo.dto.ProductImageDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductImageDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageDAO productImageDAO;
    private final ProductDAO productDAO;

    public ProductImageServiceImpl(ProductImageDAO productImageDAO, ProductDAO productDAO) {
        this.productImageDAO = productImageDAO;
        this.productDAO = productDAO;
    }

    @Override
    public void deleteImagesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<String> validIds = new ArrayList<>();
        for (String id : ids) {
            if (productImageDAO.findById(id) != null) validIds.add(id);
        }
        if (validIds.isEmpty()) throw new RuntimeException("No valid images found to delete");
        productImageDAO.deleteImagesByIds(validIds);
    }

    @Override
    public void create(ProductImageDTO dto) {
        Product product = productDAO.findById(dto.getProductId());
        if (product == null) throw new RuntimeException("Product not found");
        List<ProductImage> existing = productImageDAO.findByProductId(product.getProductId());
        boolean shouldBePrimary = dto.isPrimary() || existing.isEmpty();
        if (shouldBePrimary) productImageDAO.clearPrimary(product.getProductId());
        ProductImage image = mapToEntity(dto, product);
        image.setPrimary(shouldBePrimary);
        productImageDAO.create(image);
    }

    @Override
    public void delete(ProductImageDTO dto) {
        ProductImage image = productImageDAO.findById(dto.getId());
        if (image == null) throw new RuntimeException("Image not found");
        String productId = image.getProduct() == null ? null : image.getProduct().getProductId();
        boolean wasPrimary = image.isPrimary();
        productImageDAO.delete(image);
        if (wasPrimary && productId != null) {
            List<ProductImage> remaining = productImageDAO.findByProductId(productId);
            if (!remaining.isEmpty()) productImageDAO.setPrimary(remaining.get(0).getId());
        }
    }

    @Override
    public ProductImageDTO findById(String id) {
        ProductImage image = productImageDAO.findById(id);
        if (image == null) throw new RuntimeException("Image not found");
        return mapToDTO(image);
    }

    @Override
    public List<ProductImageDTO> findByProductId(String productId) {
        return productImageDAO.findByProductId(productId).stream().map(this::mapToDTO).toList();
    }

    @Override
    public void deleteByProductId(String productId) {
        productImageDAO.deleteByProductId(productId);
    }

    @Override
    public void createBatch(List<ProductImageDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        String productId = dtos.get(0).getProductId();
        Product product = productDAO.findById(productId);
        if (product == null) throw new RuntimeException("Product not found: " + productId);
        boolean allSameProduct = dtos.stream().allMatch(dto -> productId.equals(dto.getProductId()));
        if (!allSameProduct) throw new RuntimeException("Batch upload must target one product at a time");

        long chosenPrimary = dtos.stream().filter(ProductImageDTO::isPrimary).count();
        if (chosenPrimary > 1) throw new RuntimeException("Only one primary image is allowed");

        List<ProductImage> existing = productImageDAO.findByProductId(productId);
        boolean shouldSetFirstAsPrimary = existing.isEmpty() && chosenPrimary == 0;
        if (chosenPrimary == 1 || shouldSetFirstAsPrimary) productImageDAO.clearPrimary(productId);

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            ProductImage image = mapToEntity(dtos.get(i), product);
            image.setPrimary(dtos.get(i).isPrimary() || (shouldSetFirstAsPrimary && i == 0));
            images.add(image);
        }
        productImageDAO.createBatch(images);
    }

    @Override
    public void setPrimary(String imageId) {
        productImageDAO.setPrimary(imageId);
    }

    private ProductImage mapToEntity(ProductImageDTO dto, Product product) {
        if (dto == null) return null;
        ProductImage image = new ProductImage();
        image.setId(dto.getId());
        if (dto.getImageData() == null || dto.getImageData().isBlank()) throw new RuntimeException("Image data cannot be empty");
        image.setImageData(Base64.getDecoder().decode(dto.getImageData()));
        image.setContentType(dto.getContentType());
        image.setPrimary(dto.isPrimary());
        image.setProduct(product);
        return image;
    }

    private ProductImageDTO mapToDTO(ProductImage image) {
        if (image == null) return null;
        ProductImageDTO dto = new ProductImageDTO();
        dto.setId(image.getId());
        dto.setContentType(image.getContentType());
        dto.setPrimary(image.isPrimary());
        if (image.getImageData() != null) dto.setImageData(Base64.getEncoder().encodeToString(image.getImageData()));
        if (image.getProduct() != null) dto.setProductId(image.getProduct().getProductId());
        return dto;
    }
}
