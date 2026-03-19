package model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private int loyaltyPoints;
    private String defaultShippingAddress;
    private List<Order> orderHistory;
    private Cart cart;
    
    public Customer() {
        this.orderHistory = new ArrayList<>();
        this.cart = new Cart();
        this.role = "CUSTOMER";
    }
    
    // Methods from your diagram
    public List<Order> viewOrderHistory() {
        return orderHistory;
    }
    
    public void addToCart(Book book, int quantity) {
        cart.addItem(book, quantity);
    }
    
    public Order placeOrder() {
        Order order = new Order();
        order.setCustomerId(this.userId);
        order.setItems(new ArrayList<>(cart.getItems().entrySet()));
        order.setTotalAmount(cart.getTotal());
        order.setShippingAddress(this.defaultShippingAddress);
        order.setStatus(OrderStatus.PENDING);
        
        // Clear cart after order
        cart.clear();
        
        return order;
    }
    
    // Getters and Setters
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    
    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String defaultShippingAddress) { 
        this.defaultShippingAddress = defaultShippingAddress; 
    }
    
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}