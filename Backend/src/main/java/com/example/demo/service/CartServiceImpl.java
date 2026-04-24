package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.repository.AccountDAO;
import com.example.demo.repository.CartDAO;
import com.example.demo.repository.CartItemDAO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final AccountDAO accountDAO;

    public CartServiceImpl(CartDAO cartDAO, CartItemDAO cartItemDAO, AccountDAO accountDAO) {
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.accountDAO = accountDAO;
    }

    // ================= CREATE =================
    @Override
    public void create(CartDTO dto) {

        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());

        // chỉ set user bằng reference (tránh detached)
        var userRef = new com.example.demo.model.User();
        userRef.setId(dto.getUserId());
        cart.setUser(userRef);

        cartDAO.create(cart);
    }

    // ================= FIND BY ID =================
    @Override
    public Optional<CartDTO> findById(String id) {

        return cartDAO.findById(id)
                .map(this::toDTO);
    }

    // ================= FIND BY USER =================
    @Override
    public CartDTO findByUserId(String userId) {

        Cart cart = cartDAO.findByUserId(userId);

        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        return toDTO(cart);
    }

    // ================= UPDATE =================
    @Override
    public void update(CartDTO dto) {

        Cart existing = cartDAO.findById(dto.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartDAO.update(existing);
    }

    // ================= DELETE =================
    @Override
    public void delete(String id) {
        cartDAO.delete(id);
    }

    // ================= GET REFERENCE =================
    @Override
    public CartDTO getReferenceById(String id) {

        Cart cart = cartDAO.getReferenceById(id);

        return toDTO(cart);
    }

    // ================= MAPPER =================
    private CartDTO toDTO(Cart cart) {

        // lấy items từ DB (tránh lazy lỗi)
        List<CartItem> items = cartItemDAO.findByCartId(cart.getId());

        List<CartItemDTO> itemDTOs = items.stream().map(item -> {

            var v = item.getProductVariant();

            double price = v.getProduct().getPrice().doubleValue();
            int quantity = item.getQuantity();

            return new CartItemDTO(
                    item.getId(),
                    v.getProduct().getProductId(),
                    v.getProduct().getProductName(),
                    v.getSize().getName(),
                    v.getColor().getName(),
                    quantity,
                    price,
                    price * quantity
            );

        }).toList();

        return new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                itemDTOs
        );
    }
}