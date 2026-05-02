package com.example.demo.repository;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductUserDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDAO {
    List<Product> findAll();
    List<ProductDTO> findAllDTO();
    void create(Product product);
    void edit(Product product, String id);
    void delete(String id);
    Product findById(String id);
    ProductDTO findByIdDTO(String id);
    long countProducts();
    List<Product> findAllPaged(int page, int pageSize);
    List<ProductDTO> findAllPagedDTO(int page, int pageSize);
    List<Product> getProductsForUser();
    List<ProductUserDTO> getProductsForUserDTO();
    List<Product> findByPriceLessThan(java.math.BigDecimal maxPrice);
    List<ProductUserDTO> findByPriceLessThanDTO(java.math.BigDecimal maxPrice);
    List<Product> findByPriceGreaterThan(java.math.BigDecimal minPrice);
    List<ProductUserDTO> findByPriceGreaterThanDTO(java.math.BigDecimal minPrice);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductUserDTO> findByPriceBetweenDTO(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    );

    List<ProductUserDTO> searchProductsDTO(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    );

    List<Product> searchProductsPaged(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            int page,
            int pageSize
    );

    List<ProductUserDTO> searchProductsPagedDTO(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            int page,
            int pageSize
    );

    long countSearchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    );

    List<Product> searchProductsAdmin(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    );

    List<ProductDTO> searchProductsAdminDTO(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    );

    List<Product> searchProductsAdminPaged(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status,
            int page,
            int pageSize
    );

    List<ProductDTO> searchProductsAdminPagedDTO(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status,
            int page,
            int pageSize
    );

    long countSearchProductsAdmin(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    );

    boolean existsByProductType(String productTypeId);
    boolean existsByColorId(String colorId);
    boolean existsBySizeId(String sizeId);
}
