package com.example.demo.service;

import com.example.demo.dto.ExportFileDTO;
import com.example.demo.dto.PageResponseDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ProductUserDTO;

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

    PageResponseDTO<ProductDTO> findProductsPage(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status,
            int page,
            int pageSize
    );

    ExportFileDTO exportProducts(
            String format,
            String scope,
            Integer page,
            Integer pageSize,
            List<Integer> pages,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    );

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

    PageResponseDTO<ProductUserDTO> searchUserProductsPage(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            int page,
            int pageSize
    );

    List<ProductDTO> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            String status
    );

    boolean existsByProductType(String productTypeId);
    boolean existsByColorId(String colorId);
    boolean existsBySizeId(String sizeId);
}
