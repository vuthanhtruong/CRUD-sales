// CartItemServiceImpl.java - FULL FILE
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

@Service
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private final CartItemDAO cartItemDAO;
    private final CartDAO cartDAO;
    private final AccountDAO accountDAO;
    private final ProductVariantDAO productVariantDAO;

    public CartItemServiceImpl(CartItemDAO cartItemDAO, CartDAO cartDAO,
                               AccountDAO accountDAO, ProductVariantDAO productVariantDAO) {
        this.cartItemDAO = cartItemDAO;
        this.cartDAO = cartDAO;
        this.accountDAO = accountDAO;
        this.productVariantDAO = productVariantDAO;
    }

    // ================= INCREASE QUANTITY (có check kho) =================
    @Override
    public void increaseQuantity(String cartItemId, int amount) {

        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");

        CartItem item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        var v = item.getProductVariant();
        int stock = productVariantDAO.getQuantity(
                v.getId().getProductId(),
                v.getId().getSizeId(),
                v.getId().getColorId()
        );

        if (item.getQuantity() + amount > stock) {
            throw new RuntimeException("Không đủ hàng trong kho (tồn kho: " + stock + ")");
        }

        item.setQuantity(item.getQuantity() + amount);
        cartItemDAO.update(item);
    }

    // ================= DECREASE QUANTITY =================
    @Override
    public void decreaseQuantity(String cartItemId, int amount) {

        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");

        CartItem item = cartItemDAO.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        int newQty = item.getQuantity() - amount;

        if (newQty <= 0) {
            cartItemDAO.delete(cartItemId);
        } else {
            item.setQuantity(newQty);
            cartItemDAO.update(item);
        }
    }

    // ================= CHECKOUT (trừ kho + xóa item đã chọn) =================
    @Override
    public void checkout(List<String> cartItemIds) {

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("Không có sản phẩm nào được chọn");
        }

        List<CartItem> items = cartItemDAO.findByIds(cartItemIds);

        if (items.size() != cartItemIds.size()) {
            throw new RuntimeException("Một số sản phẩm không tồn tại trong giỏ hàng");
        }

        // Trừ kho từng item
        for (CartItem item : items) {
            var v = item.getProductVariant();
            int updated = productVariantDAO.decreaseQuantity(
                    v.getId().getProductId(),
                    v.getId().getSizeId(),
                    v.getId().getColorId(),
                    item.getQuantity()
            );

            if (updated == 0) {
                throw new RuntimeException(
                        "Sản phẩm \"" + v.getProduct().getProductName() +
                                "\" (" + v.getSize().getName() + " / " + v.getColor().getName() +
                                ") không đủ hàng trong kho"
                );
            }
        }

        // Xóa các item đã chọn khỏi giỏ
        for (String id : cartItemIds) {
            cartItemDAO.delete(id);
        }
    }

    // ================= GET CURRENT USER CART =================
    @Override
    public List<CartItemDTO> findCurrentUserCartItems() {

        String username = accountDAO.getCurrentAccountUsername();
        var account = accountDAO.getAccountByUsername(username);
        var user = (com.example.demo.model.User) account.getUser();

        Cart cart = cartDAO.findByUserId(user.getId());
        if (cart == null) throw new RuntimeException("Cart not found");

        return cartItemDAO.findByCartId(cart.getId())
                .stream().map(this::toDTO).toList();
    }

    // ================= CREATE =================
    @Override
    public void create(AddToCartRequestDTO dto) {

        String username = accountDAO.getCurrentAccountUsername();
        var account = accountDAO.getAccountByUsername(username);
        var user = (com.example.demo.model.User) account.getUser();

        Cart cart = cartDAO.findByUserId(user.getId());
        if (cart == null) throw new RuntimeException("Cart not found");

        Cart managedCart = cartDAO.getReferenceById(cart.getId());

        var variantId = new ProductVariantId(dto.getProductId(), dto.getSizeId(), dto.getColorId());
        ProductVariant variant = productVariantDAO.findById(variantId);
        if (variant == null) throw new RuntimeException("Product variant not found");

        // Check tồn kho
        int stock = productVariantDAO.getQuantity(dto.getProductId(), dto.getSizeId(), dto.getColorId());
        if (dto.getQuantity() > stock) {
            throw new RuntimeException("Không đủ hàng trong kho (tồn kho: " + stock + ")");
        }

        Optional<CartItem> existing = cartItemDAO.findByCartAndVariant(
                managedCart.getId(), dto.getProductId(), dto.getSizeId(), dto.getColorId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + dto.getQuantity();
            if (newQty > stock) {
                throw new RuntimeException("Không đủ hàng trong kho (tồn kho: " + stock + ")");
            }
            item.setQuantity(newQty);
            cartItemDAO.update(item);
        } else {
            CartItem entity = new CartItem();
            entity.setCart(managedCart);
            entity.setProductVariant(variant);
            entity.setQuantity(dto.getQuantity());
            cartItemDAO.create(entity);
        }
    }

    // ================= FIND BY ID =================
    @Override
    public Optional<CartItemDTO> findById(String id) {
        return cartItemDAO.findById(id).map(this::toDTO);
    }

    // ================= FIND BY CART =================
    @Override
    public List<CartItemDTO> findByCartId(String cartId) {
        return cartItemDAO.findByCartId(cartId).stream().map(this::toDTO).toList();
    }

    // ================= FIND BY CART + VARIANT =================
    @Override
    public Optional<CartItemDTO> findByCartAndVariant(String cartId, String productId,
                                                      String sizeId, String colorId) {
        return cartItemDAO.findByCartAndVariant(cartId, productId, sizeId, colorId).map(this::toDTO);
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