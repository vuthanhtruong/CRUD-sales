package com.example.demo.repository;

import com.example.demo.model.ProductType;

import java.util.List;

public interface ProductTypeDAO {
    List<ProductType> getProductTypes();
    ProductType getProductTypeById(String id);
    void createProductType(ProductType productType);
    void updateProductType(ProductType productType, String id);
    void deleteProductType(String id);
}
