package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductUserDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.ProductStatus;
import com.example.demo.model.ProductType;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductImageDAO;
import com.example.demo.repository.ProductTypeDAO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProductImageDAO productImageDAO;

    @Autowired
    private ProductTypeDAO productType;

    // ==================== ADMIN ====================

    @Override
    @Cacheable(value = "products",
            key = "'search:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId + ':' + #status")
    public List<ProductDTO> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        ProductStatus productStatus = null;
        if (status != null && !status.isBlank()) {
            productStatus = ProductStatus.valueOf(status);
        }

        List<Product> products = productDAO.searchProductsAdmin(
                keyword, minPrice, maxPrice, productTypeId, productStatus
        );

        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products", key = "'paged:' + #page + ':' + #pageSize")
    public List<ProductDTO> findAllPaged(int page, int pageSize) {
        pageSize = Math.max(1, Math.min(pageSize, 100));
        page = Math.max(1, page);

        List<Product> products = productDAO.findAllPaged(page, pageSize);
        return products.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "products", key = "'totalPages:' + #pageSize")
    public int countTotalPages(int pageSize) {
        pageSize = Math.max(1, Math.min(pageSize, 100));
        long total = productDAO.countProducts();
        return (int) Math.ceil((double) total / pageSize);
    }

    @Override
    @Cacheable(value = "products", key = "'count'")
    public long countProducts() {
        return productDAO.countProducts();
    }

    @Override
    @Cacheable(value = "products", key = "'all'")
    public List<ProductDTO> findAll() {
        List<Product> products = productDAO.findAll();
        List<ProductDTO> result = new ArrayList<>();

        for (Product product : products) {
            ProductDTO dto = new ProductDTO();
            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
            dto.setProductTypeId(
                    product.getProductType() != null ? product.getProductType().getId() : null);
            dto.setCreatedBy(
                    product.getCreatedBy() != null ? product.getCreatedBy().getId() : null);
            dto.setDescription(product.getDescription());
            dto.setPrice(product.getPrice());
            result.add(dto);
        }

        return result;
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductDTO findById(String id) {
        Product product = productDAO.findById(id);
        if (product == null) return null;
        return toDTO(product);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void create(ProductDTO dto) {
        Product product = toEntity(dto);
        productDAO.create(product);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();

            for (var imgDTO : dto.getImages()) {
                if (imgDTO.getImageData() == null || imgDTO.getImageData().isBlank()) {
                    throw new RuntimeException("Image data cannot be empty");
                }

                ProductImage img = new ProductImage();
                img.setImageData(java.util.Base64.getDecoder().decode(imgDTO.getImageData()));
                img.setContentType(imgDTO.getContentType());
                img.setPrimary(imgDTO.isPrimary());
                img.setProduct(product);
                images.add(img);
            }

            productImageDAO.createBatch(images);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#id"),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void edit(ProductDTO dto, String id) {
        Product product = toEntity(dto);
        productDAO.edit(product, id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "product", key = "#dto.productId"),
            @CacheEvict(value = "products", allEntries = true),
            @CacheEvict(value = "userProducts", allEntries = true)
    })
    public void delete(ProductDTO dto) {
        Product product = toEntity(dto);
        productDAO.delete(product);
    }

    // ==================== USER ====================

    @Override
    @Cacheable(value = "userProducts",
            key = "'search:' + #keyword + ':' + #minPrice + ':' + #maxPrice + ':' + #productTypeId")
    public List<ProductUserDTO> searchUserProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        List<Product> products = productDAO.searchProducts(keyword, minPrice, maxPrice, productTypeId);

        return products.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "userProducts", key = "'all'")
    public List<ProductUserDTO> getProductsForUser() {
        List<Product> products = productDAO.getProductsForUser();
        List<ProductUserDTO> result = new ArrayList<>();

        for (Product product : products) {
            result.add(toUserDTO(product));
        }

        return result;
    }

    @Override
    @Cacheable(value = "userProducts", key = "'gt:' + #minPrice")
    public List<ProductUserDTO> findUserProductsByPriceGreaterThan(BigDecimal minPrice) {
        return productDAO.findByPriceGreaterThan(minPrice)
                .stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "userProducts", key = "'between:' + #minPrice + ':' + #maxPrice")
    public List<ProductUserDTO> findUserProductsByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        return productDAO.findByPriceBetween(minPrice, maxPrice)
                .stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "userProducts", key = "'lt:' + #maxPrice")
    public List<ProductUserDTO> findUserProductsByPriceLessThan(BigDecimal maxPrice) {
        return productDAO.findByPriceLessThan(maxPrice)
                .stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    // ==================== HELPER ====================

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getStatus() != null ? product.getStatus().name() : null,
                product.getProductType() != null ? product.getProductType().getId() : null,
                product.getCreatedBy() != null ? product.getCreatedBy().getId() : null,
                product.getPrice(),
                product.getDescription(),
                null
        );
    }

    private ProductUserDTO toUserDTO(Product product) {
        ProductUserDTO dto = new ProductUserDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());

        String base64Image = null;

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            for (ProductImage img : product.getImages()) {
                if (img.isPrimary()) {
                    base64Image = java.util.Base64.getEncoder().encodeToString(img.getImageData());
                    break;
                }
            }
            if (base64Image == null) {
                base64Image = java.util.Base64.getEncoder()
                        .encodeToString(product.getImages().get(0).getImageData());
            }
        }

        dto.setImage(base64Image);
        return dto;
    }

    private Product toEntity(ProductDTO dto) {
        if (dto == null) return null;

        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setProductName(dto.getProductName());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(dto.getStatus()));
        }

        if (dto.getProductTypeId() != null) {
            ProductType type = productType.getProductTypeById(dto.getProductTypeId());
            product.setProductType(type);
        }

        return product;
    }
}