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
            jpql.append(" AND p.productType.productTypeId = :typeId");
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
    public void delete(Product product) {

        Product managed = entityManager.contains(product)
                ? product
                : entityManager.merge(product);

        // 1. xóa variants trước
        entityManager.createQuery(
                        "DELETE FROM ProductVariant pv WHERE pv.product.productId = :id"
                ).setParameter("id", managed.getProductId())
                .executeUpdate();

        // 2. xóa images nếu cần (nếu có FK)
        entityManager.createQuery(
                        "DELETE FROM ProductImage pi WHERE pi.product.productId = :id"
                ).setParameter("id", managed.getProductId())
                .executeUpdate();

        // 3. xóa product
        entityManager.remove(managed);
    }
}