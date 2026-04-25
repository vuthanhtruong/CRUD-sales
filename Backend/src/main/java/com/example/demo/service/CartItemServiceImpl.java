package com.example.demo.service;

import com.example.demo.dto.AddToCartRequestDTO;
import com.example.demo.dto.CartItemDTO;
import com.example.demo.model.Cart;
import com.example.demo.model.CartItem;
import com.example.demo.model.Person;
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

    @Override
    public void increaseQuantity(String cartItemId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        CartItem item = getOwnedCartItem(cartItemId);
        var v = item.getProductVariant();
        int stock = productVariantDAO.getQuantity(v.getId().getProductId(), v.getId().getSizeId(), v.getId().getColorId());
        if (item.getQuantity() + amount > stock) throw new RuntimeException("Not enough stock (available: " + stock + ")");
        item.setQuantity(item.getQuantity() + amount);
        cartItemDAO.update(item);
    }

    @Override
    public void decreaseQuantity(String cartItemId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        CartItem item = getOwnedCartItem(cartItemId);
        int newQty = item.getQuantity() - amount;
        if (newQty <= 0) cartItemDAO.delete(cartItemId);
        else {
            item.setQuantity(newQty);
            cartItemDAO.update(item);
        }
    }

    @Override
    public void checkout(List<String> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) throw new IllegalArgumentException("No cart items selected");
        List<CartItem> items = cartItemDAO.findByIds(cartItemIds);
        if (items.size() != cartItemIds.size()) throw new RuntimeException("Some cart items were not found");
        Person current = currentPerson();
        for (CartItem item : items) assertOwned(item, current.getId());
        for (CartItem item : items) {
            var v = item.getProductVariant();
            int updated = productVariantDAO.decreaseQuantity(v.getId().getProductId(), v.getId().getSizeId(), v.getId().getColorId(), item.getQuantity());
            if (updated == 0) throw new RuntimeException("Product \"" + v.getProduct().getProductName() + "\" is out of stock");
        }
        for (String id : cartItemIds) cartItemDAO.delete(id);
    }

    @Override
    public List<CartItemDTO> findCurrentUserCartItems() {
        Cart cart = currentCart();
        return cartItemDAO.findByCartId(cart.getId()).stream().map(this::toDTO).toList();
    }

    @Override
    public void create(AddToCartRequestDTO dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        Cart cart = currentCart();
        Cart managedCart = cartDAO.getReferenceById(cart.getId());

        var variantId = new ProductVariantId(dto.getProductId(), dto.getSizeId(), dto.getColorId());
        ProductVariant variant = productVariantDAO.findById(variantId);
        if (variant == null) throw new RuntimeException("Product variant not found");

        int stock = productVariantDAO.getQuantity(dto.getProductId(), dto.getSizeId(), dto.getColorId());
        if (dto.getQuantity() > stock) throw new RuntimeException("Not enough stock (available: " + stock + ")");

        Optional<CartItem> existing = cartItemDAO.findByCartAndVariant(managedCart.getId(), dto.getProductId(), dto.getSizeId(), dto.getColorId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + dto.getQuantity();
            if (newQty > stock) throw new RuntimeException("Not enough stock (available: " + stock + ")");
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

    @Override
    public Optional<CartItemDTO> findById(String id) {
        return Optional.of(toDTO(getOwnedCartItem(id)));
    }

    @Override
    public List<CartItemDTO> findByCartId(String cartId) {
        Cart cart = currentCart();
        if (!cart.getId().equals(cartId)) throw new RuntimeException("You cannot access this cart");
        return cartItemDAO.findByCartId(cartId).stream().map(this::toDTO).toList();
    }

    @Override
    public Optional<CartItemDTO> findByCartAndVariant(String cartId, String productId, String sizeId, String colorId) {
        Cart cart = currentCart();
        if (!cart.getId().equals(cartId)) throw new RuntimeException("You cannot access this cart");
        return cartItemDAO.findByCartAndVariant(cartId, productId, sizeId, colorId).map(this::toDTO);
    }

    @Override
    public void update(CartItemDTO dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        CartItem existing = getOwnedCartItem(dto.getCartItemId());
        var v = existing.getProductVariant();
        int stock = productVariantDAO.getQuantity(v.getId().getProductId(), v.getId().getSizeId(), v.getId().getColorId());
        if (dto.getQuantity() > stock) throw new RuntimeException("Not enough stock (available: " + stock + ")");
        existing.setQuantity(dto.getQuantity());
        cartItemDAO.update(existing);
    }

    @Override
    public void delete(String id) {
        getOwnedCartItem(id);
        cartItemDAO.delete(id);
    }

    @Override
    public void deleteByCartId(String cartId) {
        Cart cart = currentCart();
        if (!cart.getId().equals(cartId)) throw new RuntimeException("You cannot clear this cart");
        cartItemDAO.deleteByCartId(cartId);
    }

    private Cart currentCart() {
        Person person = currentPerson();
        Cart cart = cartDAO.findByUserId(person.getId());
        if (cart == null) {
            cart = new Cart();
            cart.setUser(person);
            cartDAO.create(cart);
        }
        return cart;
    }

    private Person currentPerson() {
        var account = accountDAO.getAccountByUsername(accountDAO.getCurrentAccountUsername());
        if (account == null || account.getUser() == null) throw new RuntimeException("Account not found");
        return account.getUser();
    }

    private CartItem getOwnedCartItem(String id) {
        CartItem item = cartItemDAO.findById(id).orElseThrow(() -> new RuntimeException("CartItem not found"));
        assertOwned(item, currentPerson().getId());
        return item;
    }

    private void assertOwned(CartItem item, String userId) {
        if (item.getCart() == null || item.getCart().getUser() == null || !userId.equals(item.getCart().getUser().getId())) {
            throw new RuntimeException("You cannot modify this cart item");
        }
    }

    private CartItemDTO toDTO(CartItem item) {
        var v = item.getProductVariant();
        return new CartItemDTO(item.getId(), v.getProduct().getProductId(), v.getProduct().getProductName(),
                v.getSize().getName(), v.getColor().getName(), item.getQuantity(),
                v.getProduct().getPrice().doubleValue(), item.getQuantity() * v.getProduct().getPrice().doubleValue());
    }
}
