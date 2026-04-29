package com.example.demo.repository;

import com.example.demo.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
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

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountDAO accountDAO;

    public ProductDAOImpl(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

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
        appendAdminFilters(jpql, keyword, minPrice, maxPrice, productTypeId, status);
        jpql.append(" ORDER BY p.createdDate DESC");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        setAdminFilterParams(query, keyword, minPrice, maxPrice, productTypeId, status);
        return query.getResultList();
    }

    @Override
    public List<Product> searchProductsAdminPaged(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status,
            int page,
            int pageSize
    ) {
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(pageSize, 100));

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        appendAdminFilters(jpql, keyword, minPrice, maxPrice, productTypeId, status);
        jpql.append(" ORDER BY p.createdDate DESC");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        setAdminFilterParams(query, keyword, minPrice, maxPrice, productTypeId, status);

        return query
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long countSearchProductsAdmin(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    ) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE 1=1");
        appendAdminFilters(jpql, keyword, minPrice, maxPrice, productTypeId, status);

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        setAdminFilterParams(query, keyword, minPrice, maxPrice, productTypeId, status);
        return query.getSingleResult();
    }

    @Override
    public List<Product> searchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        appendUserFilters(jpql, keyword, minPrice, maxPrice, productTypeId);
        jpql.append(" ORDER BY p.createdDate DESC");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        setUserFilterParams(query, keyword, minPrice, maxPrice, productTypeId);
        return query.getResultList();
    }

    @Override
    public List<Product> searchProductsPaged(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            int page,
            int pageSize
    ) {
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(pageSize, 100));

        StringBuilder jpql = new StringBuilder("SELECT p FROM Product p WHERE 1=1");
        appendUserFilters(jpql, keyword, minPrice, maxPrice, productTypeId);
        jpql.append(" ORDER BY p.createdDate DESC");

        TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
        setUserFilterParams(query, keyword, minPrice, maxPrice, productTypeId);

        return query
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    @Override
    public long countSearchProducts(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
        StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM Product p WHERE 1=1");
        appendUserFilters(jpql, keyword, minPrice, maxPrice, productTypeId);

        TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
        setUserFilterParams(query, keyword, minPrice, maxPrice, productTypeId);
        return query.getSingleResult();
    }

    private void appendAdminFilters(
            StringBuilder jpql,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) ")
                    .append("OR LOWER(p.description) LIKE LOWER(:keyword))");
        }
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }
        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            jpql.append(" AND p.productType.id = :typeId");
        }
        if (status != null) {
            jpql.append(" AND p.status = :status");
        }
    }

    private void setAdminFilterParams(
            jakarta.persistence.Query query,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId,
            ProductStatus status
    ) {
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
    }

    private void appendUserFilters(
            StringBuilder jpql,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            jpql.append(" AND (LOWER(p.productName) LIKE LOWER(:keyword) ")
                    .append("OR LOWER(p.description) LIKE LOWER(:keyword))");
        }
        if (minPrice != null) {
            jpql.append(" AND p.price >= :minPrice");
        }
        if (maxPrice != null) {
            jpql.append(" AND p.price <= :maxPrice");
        }
        if (productTypeId != null && !productTypeId.trim().isEmpty()) {
            jpql.append(" AND p.productType.id = :typeId");
        }
        jpql.append(" AND p.status = :status");
    }

    private void setUserFilterParams(
            jakarta.persistence.Query query,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String productTypeId
    ) {
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
        query.setParameter("status", ProductStatus.ACTIVE);
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
        page = Math.max(1, page);
        pageSize = Math.max(1, Math.min(pageSize, 100));

        return entityManager.createQuery(
                        "SELECT p FROM Product p ORDER BY p.createdDate DESC",
                        Product.class
                )
                .setFirstResult((page - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
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

        entityManager.createNativeQuery("""
        UPDATE product_comment
        SET parent_id = NULL
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM product_comment
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM product_review
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM wishlist_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM product_metric
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM product_image
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM cart_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM order_item
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

        entityManager.createNativeQuery("""
        DELETE FROM product_variant
        WHERE product_id = :productId
    """)
                .setParameter("productId", productId)
                .executeUpdate();

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
