package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductUserDTO;
import com.example.demo.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<ProductDTO> findAll();
    void create(ProductDTO product);
    void edit(ProductDTO product, String id);
    void delete(String id);
    ProductDTO findById(String id);
    long countProducts();
    List<ProductDTO> findAllPaged(int page, int pageSize);
    int countTotalPages(int pageSize);
    List<ProductUserDTO> getProductsForUser();
    List<ProductUserDTO> findUserProductsByPriceLessThan(BigDecimal maxPrice);

    List<ProductUserDTO> findUserProductsByPriceGreaterThan(BigDecimal minPrice);

    List<ProductUserDTO> findUserProductsByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<ProductUserDTO> searchUserProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    );
    List<ProductDTO> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    );
}
