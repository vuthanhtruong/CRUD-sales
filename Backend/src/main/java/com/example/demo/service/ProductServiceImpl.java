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
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Override
    public List<ProductDTO> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    ) {

        // Validate giá
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        // Convert status string → enum
        ProductStatus productStatus = null;
        if (status != null && !status.isBlank()) {
            productStatus = ProductStatus.valueOf(status);
        }

        // Gọi DAO (bạn cần mở rộng DAO)
        List<Product> products = productDAO.searchProductsAdmin(
                keyword,
                minPrice,
                maxPrice,
                productTypeId,
                productStatus
        );

        // Map sang DTO
        return products.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductUserDTO> searchUserProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {

        // validate giá
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice must be <= maxPrice");
        }

        // gọi DAO dynamic search (bạn đã viết trước đó)
        List<Product> products = productDAO.searchProducts(
                keyword,
                minPrice,
                maxPrice,
                productTypeId
        );

        // map sang DTO cho frontend
        return products.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    private ProductUserDTO toUserDTO(Product product) {

        ProductUserDTO dto = new ProductUserDTO();

        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setPrice(product.getPrice());

        String base64Image = null;

        if (product.getImages() != null && !product.getImages().isEmpty()) {

            // ưu tiên ảnh primary
            for (ProductImage img : product.getImages()) {
                if (img.isPrimary()) {
                    base64Image = java.util.Base64.getEncoder()
                            .encodeToString(img.getImageData());
                    break;
                }
            }

            // fallback
            if (base64Image == null) {
                ProductImage firstImg = product.getImages().get(0);
                base64Image = java.util.Base64.getEncoder()
                        .encodeToString(firstImg.getImageData());
            }
        }

        dto.setImage(base64Image);

        return dto;
    }
    @Override
    public List<ProductUserDTO> findUserProductsByPriceGreaterThan(BigDecimal minPrice) {

        List<Product> products = productDAO.findByPriceGreaterThan(minPrice);

        return products.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductUserDTO> findUserProductsByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }

        List<Product> products = productDAO.findByPriceBetween(minPrice, maxPrice);

        return products.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductUserDTO> findUserProductsByPriceLessThan(BigDecimal maxPrice) {

        List<Product> products = productDAO.findByPriceLessThan(maxPrice);

        return products.stream()
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductUserDTO> getProductsForUser() {

        List<Product> products = productDAO.getProductsForUser();

        List<ProductUserDTO> result = new ArrayList<>();

        for (Product product : products) {

            ProductUserDTO dto = new ProductUserDTO();

            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setPrice(product.getPrice());

            String base64Image = null;

            // 🔥 xử lý ảnh
            if (product.getImages() != null && !product.getImages().isEmpty()) {

                // 1. ưu tiên ảnh primary
                for (ProductImage img : product.getImages()) {
                    if (img.isPrimary()) {
                        base64Image = java.util.Base64.getEncoder()
                                .encodeToString(img.getImageData());
                        break;
                    }
                }

                // 2. fallback nếu không có primary
                if (base64Image == null) {
                    ProductImage firstImg = product.getImages().get(0);
                    base64Image = java.util.Base64.getEncoder()
                            .encodeToString(firstImg.getImageData());
                }
            }

            // 🔥 set ảnh (QUAN TRỌNG: phải là "image" không phải "imageId")
            dto.setImage(base64Image);

            result.add(dto);
        }

        return result;
    }

    @Override
    public int countTotalPages(int pageSize) {
        pageSize = Math.max(1, Math.min(pageSize, 100));
        long total = productDAO.countProducts();
        return (int) Math.ceil((double) total / pageSize);
    }

    @Override
    public List<ProductDTO> findAllPaged(int page, int pageSize) {
        // Clamp page size để tránh query nặng
        pageSize = Math.max(1, Math.min(pageSize, 100));
        page = Math.max(1, page);

        List<Product> products = productDAO.findAllPaged(page, pageSize);
        return products.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Autowired
    private ProductImageDAO productImageDAO;

    @Override
    public long countProducts() {
        return productDAO.countProducts();
    }

    @Override
    public ProductDTO findById(String id) {

        Product product = productDAO.findById(id);

        if (product == null) {
            return null;
        }

        return toDTO(product);
    }

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private ProductTypeDAO productType;

    @Override
    public List<ProductDTO> findAll() {

        List<Product> products = productDAO.findAll();
        List<ProductDTO> result = new ArrayList<>();

        for (Product product : products) {

            ProductDTO dto = new ProductDTO();

            dto.setProductId(product.getProductId());
            dto.setProductName(product.getProductName());
            dto.setStatus(product.getStatus() != null ? product.getStatus().name() : null);
            dto.setProductTypeId(
                    product.getProductType() != null ? product.getProductType().getId() : null
            );
            dto.setCreatedBy(
                    product.getCreatedBy() != null ? product.getCreatedBy().getId() : null
            );
            dto.setDescription(product.getDescription() != null ? product.getDescription() : null);
            dto.setPrice(product.getPrice()!= null ? product.getPrice() : null);

            result.add(dto);
        }

        return result;
    }

    @Override
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

                // 🔥 convert base64 → byte[]
                byte[] imageBytes = java.util.Base64.getDecoder()
                        .decode(imgDTO.getImageData());

                img.setImageData(imageBytes);
                img.setContentType(imgDTO.getContentType());
                img.setPrimary(imgDTO.isPrimary());
                img.setProduct(product);

                images.add(img);
            }

            productImageDAO.createBatch(images);
        }
    }

    @Override
    public void edit(ProductDTO dto, String id) {
        Product product = toEntity(dto);
        productDAO.edit(product, id);
    }

    @Override
    public void delete(ProductDTO dto) {
        Product product = toEntity(dto);
        productDAO.delete(product);
    }

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getStatus() != null ? product.getStatus().name() : null,
                product.getProductType() != null ? product.getProductType().getId() : null,
                product.getCreatedBy() != null ? product.getCreatedBy().getId() : null,
                product.getPrice() != null ? product.getPrice() : null,
                product.getDescription() != null ? product.getDescription() : null,
                null
        );
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