package com.example.demo.repository;

import com.example.demo.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class ProductDAOImpl implements ProductDAO {

    @Override
    public boolean existsByColorId(String colorId) {

        String jpql = """
        SELECT COUNT(p)
        FROM Product p
        JOIN p.variants v
        WHERE v.color.id = :colorId
    """;

        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("colorId", colorId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean existsBySizeId(String sizeId) {

        String jpql = """
        SELECT COUNT(p)
        FROM Product p
        JOIN p.variants v
        WHERE v.size.id = :sizeId
    """;

        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("sizeId", sizeId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public boolean existsByProductType(String productTypeId) {

        String jpql = """
        SELECT COUNT(p)
        FROM Product p
        WHERE p.productType.id = :typeId
    """;

        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("typeId", productTypeId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Product> searchProductsAdmin(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    ) {

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        // 🔍 keyword (name + description)
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) " +
                    "OR LOWER(p.description) LIKE LOWER(:keyword))");
        }

        // 💰 min price
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }

        // 💰 max price
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }

        // 🏷️ product type
        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            jpql.append(" AND p.productType.id = :typeId");
        }

        // 📌 status (QUAN TRỌNG - admin mới có)
        if (status != null) {
            jpql.append(" AND p.status = :status");
        }
        jpql.append(" ORDER BY p.createdDate DESC");

        var query = entityManager.createQuery(jpql.toString(), Product.class);

        // set params
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword.trim() + "%");
        }

        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            query.setParameter("typeId", productTypeId.trim());
        }

        if (status != null) {
            query.setParameter("status", status);
        }


        return query.getResultList();
    }

    @Override
    public List<Product> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");

        // 🔍 keyword
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) " +
                    "OR LOWER(p.description) LIKE LOWER(:keyword))");
        }

        // 💰 price
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }

        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }

        // 🏷️ type
        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            jpql.append(" AND p.productType.id = :typeId"); // 🔥 FIX luôn
        }

        // 🔥 CHỈ LẤY ACTIVE
        jpql.append(" AND p.status = :status");

        // 📊 sort
        jpql.append(" ORDER BY p.createdDate DESC");

        var query = entityManager.createQuery(jpql.toString(), Product.class);

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.setParameter("keyword", "%" + keyword.trim() + "%");
        }

        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            query.setParameter("typeId", productTypeId.trim());
        }

        // 🔥 set ACTIVE cố định
        query.setParameter("status", ProductStatus.ACTIVE);

        return query.getResultList();
    }


    @Override
    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {

        String jpql = "SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice";

        return entityManager.createQuery(jpql, Product.class)
                .setParameter("minPrice", minPrice)
                .setParameter("maxPrice", maxPrice)
                .getResultList();
    }

    @Override
    public List<Product> findByPriceGreaterThan(java.math.BigDecimal minPrice) {
        String jpql = "SELECT p FROM Product p WHERE p.price > :minPrice";

        return entityManager.createQuery(jpql, Product.class)
                .setParameter("minPrice", minPrice)
                .getResultList();
    }

    @Override
    public List<Product> findByPriceLessThan(java.math.BigDecimal maxPrice) {
        String jpql = "SELECT p FROM Product p WHERE p.price <= :maxPrice";

        return entityManager.createQuery(jpql, Product.class)
                .setParameter("maxPrice", maxPrice)
                .getResultList();
    }

    @Override
    public List<Product> getProductsForUser() {
        return entityManager.createQuery(
                        "SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdDate DESC",
                        Product.class
                )
                .setParameter("status", ProductStatus.ACTIVE)
                .getResultList();
    }

    @Override
    public List<Product> findAllPaged(int page, int pageSize) {
        return entityManager.createQuery(
                        "SELECT p FROM Product p ORDER BY p.createdDate DESC",
                        Product.class
                )
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private final AccountDAO accountDAO;

    public ProductDAOImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @Override
    public long countProducts() {
        String jpql = "SELECT COUNT(p) FROM Product p";
        return entityManager.createQuery(jpql, Long.class)
                .getSingleResult();
    }

    @Override
    public Product findById(String id) {
        return entityManager.find(Product.class, id);
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> findAll() {
        String jpql = "SELECT p FROM Product p ORDER BY p.createdDate DESC";
        return entityManager.createQuery(jpql, Product.class)
                .getResultList();
    }

    @Override
    public void create(Product product) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }

        String username = authentication.getName();

        System.out.println(username);

        Account account = accountDAO.getAccountByUsername(username);

        if (account == null) {
            throw new RuntimeException("Account not found");
        }

        Person person = account.getUser();

        if (!(person instanceof Admin)) {
            throw new RuntimeException("Only admin can create product");
        }

        Admin admin = (Admin) person;

        product.setCreatedBy(admin);
        product.setCreatedDate(LocalDateTime.now());

        entityManager.persist(product);
    }

    @Override
    public void edit(Product product, String id) {
        Product existingProduct = entityManager.find(Product.class, id);

        if (existingProduct != null) {
            existingProduct.setProductName(product.getProductName());
            existingProduct.setStatus(product.getStatus());
            existingProduct.setProductType(product.getProductType());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setDescription(product.getDescription());
            entityManager.merge(existingProduct);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {

        Product managed = entityManager.find(Product.class, id);

        if (managed == null) {
            throw new RuntimeException("Product not found");
        }

        String productId = managed.getProductId();

        /*
         * ProductComment có self FK parent_id.
         * Phải cắt parent trước, nếu không khi DELETE comment có thể lỗi FK.
         */
        entityManager.createNativeQuery("""
        UPDATE product_comment
        SET parent_id = NULL
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 1. Xóa comment của product
        entityManager.createNativeQuery("""
        DELETE FROM product_comment
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 2. Xóa review của product
        entityManager.createNativeQuery("""
        DELETE FROM product_review
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 3. Xóa wishlist liên quan tới product
        entityManager.createNativeQuery("""
        DELETE FROM wishlist_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 4. Xóa metric của product
        entityManager.createNativeQuery("""
        DELETE FROM product_metric
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 5. Xóa image của product
        entityManager.createNativeQuery("""
        DELETE FROM product_image
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        /*
         * ProductVariant đang bị CartItem và OrderItem tham chiếu.
         * Phải xóa các bảng này trước khi xóa product_variant.
         */

        // 6. Xóa cart item đang giữ variant của product
        entityManager.createNativeQuery("""
        DELETE FROM cart_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 7. Xóa order item đang giữ variant của product
        entityManager.createNativeQuery("""
        DELETE FROM order_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 8. Xóa variants
        entityManager.createNativeQuery("""
        DELETE FROM product_variant
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        // 9. Cuối cùng mới xóa product
        entityManager.createNativeQuery("""
        DELETE FROM product
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();
    }
}