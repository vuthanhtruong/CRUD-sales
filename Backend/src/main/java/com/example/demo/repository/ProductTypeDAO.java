package com.example.demo.repository;

import com.example.demo.dto.ProductTypeDTO;
import com.example.demo.model.ProductType;

import java.util.List;

public interface ProductTypeDAO {
    List<ProductType> getProductTypes();
    List<ProductTypeDTO> getProductTypeDTOs();
    ProductType getProductTypeById(String id);
    ProductTypeDTO getProductTypeDTOById(String id);
    void createProductType(ProductType productType);
    void updateProductType(ProductType productType, String id);
    void deleteProductType(String id);
}
