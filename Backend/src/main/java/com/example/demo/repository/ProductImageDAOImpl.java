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

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deleteImagesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        entityManager.createQuery("DELETE FROM ProductImage i WHERE i.id IN :ids")
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public void createBatch(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return;
        int batchSize = 20;
        for (int i = 0; i < images.size(); i++) {
            entityManager.persist(images.get(i));
            if (i > 0 && i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void create(ProductImage image) {
        entityManager.persist(image);
    }

    @Override
    public void delete(ProductImage image) {
        ProductImage managed = entityManager.contains(image) ? image : entityManager.merge(image);
        entityManager.remove(managed);
    }

    @Override
    public ProductImage findById(String id) {
        return entityManager.find(ProductImage.class, id);
    }

    @Override
    public List<ProductImage> findByProductId(String productId) {
        return entityManager.createQuery(
                        "SELECT i FROM ProductImage i WHERE i.product.productId = :productId ORDER BY i.isPrimary DESC, i.id ASC",
                        ProductImage.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public void deleteByProductId(String productId) {
        entityManager.createQuery("DELETE FROM ProductImage i WHERE i.product.productId = :productId")
                .setParameter("productId", productId)
                .executeUpdate();
    }

    @Override
    public void setPrimary(String imageId) {
        ProductImage image = findById(imageId);
        if (image == null || image.getProduct() == null) throw new RuntimeException("Image not found");
        clearPrimary(image.getProduct().getProductId());
        image.setPrimary(true);
        entityManager.merge(image);
    }

    @Override
    public void clearPrimary(String productId) {
        entityManager.createQuery("UPDATE ProductImage i SET i.isPrimary = false WHERE i.product.productId = :productId")
                .setParameter("productId", productId)
                .executeUpdate();
    }
}
