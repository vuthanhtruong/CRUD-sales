package com.example.demo.service;

import com.example.demo.dto.ProductTypeDTO;
import com.example.demo.model.ProductType;
import com.example.demo.repository.ProductTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductTypueServiceImpl implements ProductTypueService {

    @Autowired
    private ProductTypeDAO productTypeDAO;

    @Override
    public List<ProductTypeDTO> getProductTypes() {
        return productTypeDAO.getProductTypeDTOs();
    }

    @Override
    public ProductTypeDTO getProductTypeById(String id) {
        return productTypeDAO.getProductTypeDTOById(id);
    }

    @Override
    public void createProductType(ProductTypeDTO dto) {
        ProductType entity = convertToEntity(dto);
        productTypeDAO.createProductType(entity);
    }

    @Override
    public void updateProductType(ProductTypeDTO dto, String id) {
        ProductType existing = productTypeDAO.getProductTypeById(id);

        if (existing != null) {
            existing.setTypeName(dto.getTypeName());
            productTypeDAO.updateProductType(existing, id);
        }
    }

    @Override
    public void deleteProductType(String id) {
        productTypeDAO.deleteProductType(id);
    }

    private ProductTypeDTO convertToDTO(ProductType entity) {
        return new ProductTypeDTO(
                entity.getId(),
                entity.getTypeName()
        );
    }

    private ProductType convertToEntity(ProductTypeDTO dto) {
        return new ProductType(
                dto.getId(),
                dto.getTypeName()
        );
    }
}