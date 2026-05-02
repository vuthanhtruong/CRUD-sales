package com.example.demo.repository;

import com.example.demo.dto.ProductTypeDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ProductTypeDAOImpl implements ProductTypeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    private final ProductDAO productDAO;

    public ProductTypeDAOImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public List<ProductType> getProductTypes() {
        return entityManager
                .createQuery("SELECT p FROM ProductType p", ProductType.class)
                .getResultList();
    }

    @Override
    public ProductType getProductTypeById(String id) {
        return entityManager.find(ProductType.class, id);
    }

    @Override
    public void createProductType(ProductType productType) {
        entityManager.persist(productType);
    }

    @Override
    public void updateProductType(ProductType productType, String id) {
        ProductType existing = entityManager.find(ProductType.class, id);
        if (existing != null) {
            existing.setTypeName(productType.getTypeName());
            entityManager.merge(existing);
        }
    }

    @Override
    public void deleteProductType(String id) {
        ProductType entity = entityManager.find(ProductType.class, id);

        List<Product> list=productDAO.findAll();
        for (Product product : list) {
            if(product.getProductType().equals(entity)) {
                productDAO.delete(product.getProductId());
            }
        }

        if (entity != null) {
            entityManager.remove(entity);
        }
    }

    @Override
    public List<ProductTypeDTO> getProductTypeDTOs() {
        return entityManager
                .createQuery("SELECT new com.example.demo.dto.ProductTypeDTO(p.id, p.typeName) FROM ProductType p", ProductTypeDTO.class)
                .getResultList();
    }

    @Override
    public ProductTypeDTO getProductTypeDTOById(String id) {
        return entityManager
                .createQuery("SELECT new com.example.demo.dto.ProductTypeDTO(p.id, p.typeName) FROM ProductType p WHERE p.id = :id", ProductTypeDTO.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

}
