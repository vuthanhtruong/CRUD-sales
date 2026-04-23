package com.example.demo.repository;

import com.example.demo.model.ProductImage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ProductImageDAOImpl implements ProductImageDAO {

    @Override
    public void deleteImagesByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            return;
        }

        String jpql = "DELETE FROM ProductImage i WHERE i.id IN :ids";

        entityManager.createQuery(jpql)
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public void createBatch(List<ProductImage> images) {

        if (images == null || images.isEmpty()) return;

        int batchSize = 20; // tối ưu performance

        for (int i = 0; i < images.size(); i++) {
            entityManager.persist(images.get(i));

            // batch insert để tránh memory overflow
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(ProductImage image) {
        entityManager.persist(image);
    }

    @Override
    public void delete(ProductImage image) {
        ProductImage managed = entityManager.contains(image)
                ? image
                : entityManager.merge(image);

        entityManager.remove(managed);
    }

    @Override
    public ProductImage findById(String id) {
        return entityManager.find(ProductImage.class, id);
    }

    @Override
    public List<ProductImage> findByProductId(String productId) {
        String jpql = "SELECT i FROM ProductImage i WHERE i.product.productId = :productId";

        return entityManager.createQuery(jpql, ProductImage.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public void deleteByProductId(String productId) {
        String jpql = "DELETE FROM ProductImage i WHERE i.product.productId = :productId";

        entityManager.createQuery(jpql)
                .setParameter("productId", productId)
                .executeUpdate();
    }
}