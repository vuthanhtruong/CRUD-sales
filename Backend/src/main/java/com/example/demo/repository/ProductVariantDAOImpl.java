package com.example.demo.repository;

import com.example.demo.model.Color;
import com.example.demo.model.ProductVariant;
import com.example.demo.model.ProductVariantId;
import com.example.demo.model.Size;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class ProductVariantDAOImpl implements ProductVariantDAO {

    @Override
    public int getTotalQuantityByProductId(String productId) {

        String jpql = """
        SELECT COALESCE(SUM(v.quantity), 0)
        FROM ProductVariant v
        WHERE v.product.productId = :productId
    """;

        Long total = entityManager.createQuery(jpql, Long.class)
                .setParameter("productId", productId)
                .getSingleResult();

        return total.intValue();
    }

    @Override
    public boolean existsAvailableStockByProductId(String productId) {

        String jpql = """
        SELECT COUNT(v)
        FROM ProductVariant v
        WHERE v.product.productId = :productId
        AND v.quantity > 0
    """;

        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("productId", productId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public int getQuantity(String productId, String sizeId, String colorId) {

        String jpql = """
        SELECT v.quantity
        FROM ProductVariant v
        WHERE v.id.productId = :productId
        AND v.id.sizeId = :sizeId
        AND v.id.colorId = :colorId
    """;

        List<Integer> result = entityManager.createQuery(jpql, Integer.class)
                .setParameter("productId", productId)
                .setParameter("sizeId", sizeId)
                .setParameter("colorId", colorId)
                .getResultList();

        return result.isEmpty() ? 0 : result.get(0);
    }

    @Override
    public int decreaseQuantity(String productId, String sizeId, String colorId, int amount) {

        String jpql = """
        UPDATE ProductVariant v
        SET v.quantity = v.quantity - :amount
        WHERE v.id.productId = :productId
        AND v.id.sizeId = :sizeId
        AND v.id.colorId = :colorId
        AND v.quantity >= :amount
    """;

        return entityManager.createQuery(jpql)
                .setParameter("amount", amount)
                .setParameter("productId", productId)
                .setParameter("sizeId", sizeId)
                .setParameter("colorId", colorId)
                .executeUpdate();
    }

    @Override
    public List<Size> findSizesByProductId(String productId) {

        String jpql = """
        SELECT DISTINCT v.size
        FROM ProductVariant v
        WHERE v.product.productId = :productId
    """;

        return entityManager.createQuery(jpql, Size.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public List<Color> findColorsByProductId(String productId) {

        String jpql = """
        SELECT DISTINCT v.color
        FROM ProductVariant v
        WHERE v.product.productId = :productId
    """;

        return entityManager.createQuery(jpql, Color.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public List<Size> findUnusedSizesByProductId(String productId) {

        String jpql = """
        SELECT s
        FROM Size s
        WHERE s.id NOT IN (
            SELECT v.size.id
            FROM ProductVariant v
            WHERE v.product.productId = :productId
        )
    """;

        return entityManager.createQuery(jpql, Size.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public List<Color> findUnusedColorsByProductId(String productId) {

        String jpql = """
        SELECT c
        FROM Color c
        WHERE c.id NOT IN (
            SELECT v.color.id
            FROM ProductVariant v
            WHERE v.product.productId = :productId
        )
    """;

        return entityManager.createQuery(jpql, Color.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProductVariant> findAll() {
        String jpql = "SELECT v FROM ProductVariant v";
        return entityManager.createQuery(jpql, ProductVariant.class)
                .getResultList();
    }

    @Override
    public ProductVariant findById(ProductVariantId id) {
        return entityManager.find(ProductVariant.class, id);
    }

    @Override
    public List<ProductVariant> findByProductId(String productId) {
        String jpql = "SELECT v FROM ProductVariant v WHERE v.product.productId = :productId";

        return entityManager.createQuery(jpql, ProductVariant.class)
                .setParameter("productId", productId)
                .getResultList();
    }

    @Override
    public void create(ProductVariant variant) {

        ProductVariantId id = variant.getId();

        ProductVariant existing = entityManager.find(ProductVariant.class, id);

        if (existing != null) {
            // 🔥 đã tồn tại → cộng số lượng
            existing.setQuantity(
                    existing.getQuantity() + variant.getQuantity()
            );

            entityManager.merge(existing);
        } else {
            // ✅ chưa có → insert mới
            entityManager.persist(variant);
        }
    }

    @Override
    public void update(ProductVariant variant) {
        entityManager.merge(variant);
    }

    @Override
    public void delete(ProductVariant variant) {
        ProductVariant managed = entityManager.contains(variant)
                ? variant
                : entityManager.merge(variant);

        entityManager.remove(managed);
    }

    @Override
    public List<ProductVariant> createBatch(List<ProductVariant> variants) {

        for (int i = 0; i < variants.size(); i++) {

            ProductVariant v = variants.get(i);

            ProductVariant existing = entityManager.find(ProductVariant.class, v.getId());

            if (existing != null) {
                // 🔥 cộng số lượng
                existing.setQuantity(
                        existing.getQuantity() + v.getQuantity()
                );
                entityManager.merge(existing);
            } else {
                entityManager.persist(v);
            }

            // tránh memory leak
            if (i % 20 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        return variants;
    }
}