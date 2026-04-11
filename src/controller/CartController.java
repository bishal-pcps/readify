package controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.CartItem;
import service.CartService;

public class CartController {
    private CartService cartService;
    private final List<Integer> selectedCartItemIds = new ArrayList<>();

    public CartController() throws SQLException {
        this.cartService = new CartService();
    }

    public void addToCart(int userId, int bookId, int quantity, double price) throws SQLException {
        var cart = cartService.getOrCreateCart(userId);
        cartService.addItemToCart(cart.getCartId(), bookId, quantity, price);
    }

    public void removeFromCart(int cartItemId) throws SQLException {
        cartService.removeItemFromCart(cartItemId);
    }

    public void updateCartItemQuantity(int cartItemId, int quantity) throws SQLException {
        cartService.updateCartItem(cartItemId, quantity);
    }

    public List<CartItem> getCartItems(int userId) throws SQLException {
        var cart = cartService.getOrCreateCart(userId);
        return cartService.getCartItems(cart.getCartId());
    }

    public double getCartTotal(int userId) throws SQLException {
        var cart = cartService.getOrCreateCart(userId);
        return cartService.calculateCartTotal(cart.getCartId());
    }

    public int getCartIdForUser(int userId) throws SQLException {
        var cart = cartService.getOrCreateCart(userId);
        return cart.getCartId();
    }

    public void clearCart(int userId) throws SQLException {
        var cart = cartService.getOrCreateCart(userId);
        cartService.clearCart(cart.getCartId());
    }

    public void setSelectedCartItemIds(List<Integer> cartItemIds) {
        selectedCartItemIds.clear();
        if (cartItemIds == null) {
            return;
        }
        for (Integer id : cartItemIds) {
            if (id != null && id > 0 && !selectedCartItemIds.contains(id)) {
                selectedCartItemIds.add(id);
            }
        }
    }

    public List<Integer> getSelectedCartItemIds() {
        return new ArrayList<>(selectedCartItemIds);
    }

    public void clearSelectedCartItemIds() {
        selectedCartItemIds.clear();
    }
}
