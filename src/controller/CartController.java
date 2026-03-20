package controller;

import java.util.ArrayList;
import java.util.List;
import model.Cart;
import model.CartItem;
import model.Book;

public class CartController {
    private Cart cart;
    
    public CartController() {
        this.cart = new Cart();
    }
    
    public void addToCart(Book book, int quantity) {
        cart.addItem(book, quantity);
    }
    
    public void removeFromCart(Book book) {
        cart.removeItem(book);
    }
    
    public void updateQuantity(Book book, int quantity) {
        cart.updateQuantity(book, quantity);
    }
    
    public List<CartItem> getCartItems() {
        List<CartItem> items = new ArrayList<>();
        for (var entry : cart.getItems().entrySet()) {
            items.add(new CartItem(entry.getKey(), entry.getValue()));
        }
        return items;
    }
    
    public double getTotal() {
        java.math.BigDecimal total = cart.getTotal();
        return total != null ? total.doubleValue() : 0.0;
    }
    
    public int getItemCount() {
        return cart.getItemCount();
    }
    
    public void clearCart() {
        cart.clear();
    }
}
