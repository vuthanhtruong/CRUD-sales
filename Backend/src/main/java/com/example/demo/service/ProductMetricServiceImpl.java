package com.example.demo.service;

import com.example.demo.dto.ProductMetricDTO;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.ProductMetric;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.ProductImageDAO;
import com.example.demo.repository.ProductMetricDAO;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ProductMetricServiceImpl implements ProductMetricService {
    private final ProductMetricDAO metricDAO;
    private final ProductDAO productDAO;
    private final ProductImageDAO productImageDAO;

    public ProductMetricServiceImpl(ProductMetricDAO metricDAO, ProductDAO productDAO, ProductImageDAO productImageDAO) {
        this.metricDAO = metricDAO;
        this.productDAO = productDAO;
        this.productImageDAO = productImageDAO;
    }

    @Override
    @CacheEvict(value = "trendingProducts", allEntries = true)
    public ProductMetricDTO recordView(String productId) {
        Product product = productDAO.findById(productId);
        if (product == null) throw new RuntimeException("Product not found");
        ProductMetric metric = metricDAO.findByProductId(productId).orElseGet(() -> {
            ProductMetric created = new ProductMetric();
            created.setProduct(product);
            created.setViewCount(0L);
            return created;
        });
        metric.setViewCount((metric.getViewCount() == null ? 0 : metric.getViewCount()) + 1);
        metric.setLastViewedAt(LocalDateTime.now());
        return toDTO(metricDAO.save(metric));
    }

    @Override
    @Cacheable(value = "trendingProducts", key = "#limit")
    public List<ProductMetricDTO> trending(int limit) {
        return metricDAO.topViewedDTO(limit);
    }

    private ProductMetricDTO toDTO(ProductMetric metric) {
        Product product = metric.getProduct();
        return new ProductMetricDTO(
                product == null ? null : product.getProductId(),
                product == null ? null : product.getProductName(),
                product == null ? null : product.getPrice(),
                product == null ? null : firstImage(product.getProductId()),
                metric.getViewCount() == null ? 0L : metric.getViewCount(),
                metric.getLastViewedAt()
        );
    }

    private String firstImage(String productId) {
        if (productId == null) return null;
        return productImageDAO.findByProductId(productId).stream()
                .sorted(Comparator.comparing(ProductImage::isPrimary).reversed())
                .findFirst()
                .map(ProductImage::getImageData)
                .map(bytes -> Base64.getEncoder().encodeToString(bytes))
                .orElse(null);
    }
}
