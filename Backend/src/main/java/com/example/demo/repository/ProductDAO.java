package com.example.demo.repository;

import com.example.demo.model.Product;
import com.example.demo.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public interface ProductDAO {
    List<Product> findAll();
    void create(Product product);
    void edit(Product product, String id);
    void delete(String id);
    Product findById(String id);
    long countProducts();
    List<Product> findAllPaged(int page, int pageSize);
    List<Product> getProductsForUser();
    List<Product> findByPriceLessThan(java.math.BigDecimal maxPrice);
    List<Product> findByPriceGreaterThan(java.math.BigDecimal minPrice);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> searchProducts(
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
    boolean existsByProductType(String productTypeId);
    boolean existsByColorId(String colorId);
    boolean existsBySizeId(String sizeId);

}
