package com.example.demo.service;

import com.example.demo.dto.AddToCartRequestDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.ProductVariant;
import com.example.demo.model.ProductVariantId;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.CartDAO;
import com.example.demo.repository.CartItemDAO;
import com.example.demo.repository.ProductVariantDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

    @Override
    public List<CartItemDTO> findCurrentUserCartItems() {

        // ===== 1. lấy username từ security =====
        String username = accountDAO.getCurrentAccountUsername();

        // ===== 2. lấy user =====
        var account = accountDAO.getAccountByUsername(username);
        var user = (com.example.demo.model.User) account.getUser();

        // ===== 3. lấy cart =====
        Cart cart = cartDAO.findByUserId(user.getId());

        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        // ===== 4. lấy cart items =====
        return cartItemDAO.findByCartId(cart.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private final CartItemDAO cartItemDAO;
    private final CartDAO cartDAO;
    private final AccountDAO accountDAO;
    private final ProductVariantDAO productVariantDAO;

    public CartItemServiceImpl(CartItemDAO cartItemDAO, CartDAO cartDAO, AccountDAO accountDAO, ProductVariantDAO productVariantDAO) {
        this.cartItemDAO = cartItemDAO;
        this.cartDAO = cartDAO;
        this.accountDAO = accountDAO;
        this.productVariantDAO = productVariantDAO;
    }

    // ================= CREATE =================
    @Override
    public void create(AddToCartRequestDTO dto) {

        // ===== 1. lấy user hiện tại =====
        String username = accountDAO.getCurrentAccountUsername();

        var account = accountDAO.getAccountByUsername(username);
        var user = (com.example.demo.model.User) account.getUser();

        // ===== 2. lấy cart =====
        Cart cart = cartDAO.findByUserId(user.getId());

        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        // 🔥 FIX: lấy managed entity
        Cart managedCart = cartDAO.getReferenceById(cart.getId());

        // ===== 3. lấy product variant =====
        var variantId = new com.example.demo.model.ProductVariantId(
                dto.getProductId(),
                dto.getSizeId(),
                dto.getColorId()
        );

        ProductVariant variant = productVariantDAO.findById(variantId);

        if (variant == null) {
            throw new RuntimeException("Product variant not found");
        }

        // ===== 4. check item tồn tại =====
        Optional<CartItem> existing = cartItemDAO.findByCartAndVariant(
                managedCart.getId(),
                dto.getProductId(),
                dto.getSizeId(),
                dto.getColorId()
        );

        if (existing.isPresent()) {

            // ✔ tăng quantity
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());

            cartItemDAO.update(item);

        } else {

            // ✔ tạo mới
            CartItem entity = new CartItem();
            entity.setCart(managedCart);
            entity.setProductVariant(variant); // variant lấy từ DAO → OK
            entity.setQuantity(dto.getQuantity());

            cartItemDAO.create(entity);
        }
    }

    // ================= FIND BY ID =================
    @Override
    public Optional<CartItemDTO> findById(String id) {
        return cartItemDAO.findById(id)
                .map(this::toDTO);
    }

    // ================= FIND BY CART =================
    @Override
    public List<CartItemDTO> findByCartId(String cartId) {
        return cartItemDAO.findByCartId(cartId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ================= FIND BY CART + VARIANT =================
    @Override
    public Optional<CartItemDTO> findByCartAndVariant(String cartId, String productId, String sizeId, String colorId) {
        return cartItemDAO.findByCartAndVariant(cartId, productId, sizeId, colorId)
                .map(this::toDTO);
    }

    // ================= UPDATE =================
    @Override
    public void update(CartItemDTO dto) {

        CartItem existing = cartItemDAO.findById(dto.getCartItemId())
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        existing.setQuantity(dto.getQuantity());

        cartItemDAO.update(existing);
    }

    // ================= DELETE =================
    @Override
    public void delete(String id) {
        cartItemDAO.delete(id);
    }

    // ================= DELETE BY CART =================
    @Override
    public void deleteByCartId(String cartId) {
        cartItemDAO.deleteByCartId(cartId);
    }

    // ================= MAPPER =================
    private CartItemDTO toDTO(CartItem item) {

        var v = item.getProductVariant();

        return new CartItemDTO(
                item.getId(),
                v.getProduct().getProductId(),
                v.getProduct().getProductName(),
                v.getSize().getName(),
                v.getColor().getName(),
                item.getQuantity(),
                v.getProduct().getPrice().doubleValue(),
                item.getQuantity() * v.getProduct().getPrice().doubleValue()
        );
    }
}