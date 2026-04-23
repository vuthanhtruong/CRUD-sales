package com.example.demo.repository;

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
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}