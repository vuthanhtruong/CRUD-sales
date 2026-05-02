package com.example.demo.service;

import com.example.demo.dto.WishlistItemDTO;
import com.example.demo.model.Person;
import com.example.demo.model.Product;
import com.example.demo.model.ProductImage;
import com.example.demo.model.WishlistItem;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.ProductDAO;
import com.example.demo.repository.WishlistDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {
    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;
    private final AccountDAO accountDAO;

    public WishlistServiceImpl(WishlistDAO wishlistDAO, ProductDAO productDAO, AccountDAO accountDAO) {
        this.wishlistDAO = wishlistDAO;
        this.productDAO = productDAO;
        this.accountDAO = accountDAO;
    }

    @Override
    public List<WishlistItemDTO> findMine() {
        return wishlistDAO.findByUserIdDTO(currentUser().getId());
    }

    @Override
    public WishlistItemDTO add(String productId) {
        Person user = currentUser();
        return wishlistDAO.findByUserAndProductDTO(user.getId(), productId)
                .orElseGet(() -> {
                    Product product = productDAO.findById(productId);
                    if (product == null) throw new RuntimeException("Product not found");
                    WishlistItem item = new WishlistItem();
                    item.setUser(user);
                    item.setProduct(product);
                    WishlistItem saved = wishlistDAO.save(item);
                    return wishlistDAO.findByUserAndProductDTO(user.getId(), productId)
                            .orElseGet(() -> toDTO(saved));
                });
    }

    @Override
    public void remove(String productId) {
        wishlistDAO.deleteByUserAndProduct(currentUser().getId(), productId);
    }

    @Override
    public Map<String, Object> status(String productId) {
        Person user = currentUser();
        boolean liked = wishlistDAO.findByUserAndProduct(user.getId(), productId).isPresent();
        long count = wishlistDAO.countByProduct(productId);
        return Map.of("liked", liked, "count", count);
    }

    private Person currentUser() {
        Person user = accountDAO.getCurrentUser();
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    private WishlistItemDTO toDTO(WishlistItem item) {
        Product p = item.getProduct();
        return new WishlistItemDTO(item.getId(), p.getProductId(), p.getProductName(), p.getPrice(), image(p), item.getCreatedAt());
    }

    private String image(Product p) {
        if (p.getImages() == null || p.getImages().isEmpty()) return null;
        ProductImage selected = p.getImages().stream().filter(ProductImage::isPrimary).findFirst().orElse(p.getImages().get(0));
        return Base64.getEncoder().encodeToString(selected.getImageData());
    }
}
