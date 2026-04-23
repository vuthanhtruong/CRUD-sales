package com.example.demo.service;

import com.example.demo.dto.ProductTypeDTO;
import com.example.demo.model.ProductType;
import com.example.demo.repository.ProductTypeDAO;

import java.util.List;

public interface ProductTypueService {
    List<ProductTypeDTO> getProductTypes();
    ProductTypeDTO getProductTypeById(String id);
    void createProductType(ProductTypeDTO productType);
    void updateProductType(ProductTypeDTO productType, String id);
    void deleteProductType(String id);
}
