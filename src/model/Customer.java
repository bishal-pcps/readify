package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Customer user who can browse books, add to cart, and place orders.
 */
public class Customer extends User {
    private int loyaltyPoints;
    private String defaultShippingAddress;
    private List<Order> orderHistory;
    private Cart cart;
    
    public Customer() {
        this.orderHistory = new ArrayList<>();
        this.cart = new Cart();
        this.role = Role.CUSTOMER;
    }
    
    /**
     * Get order history for this customer.
     */
    public List<Order> viewOrderHistory() {
        return new ArrayList<>(orderHistory);
    }
    
    /**
     * Add a book to the shopping cart.
     */
    public void addToCart(Book book, int quantity) {
        if (book != null && quantity > 0) {
            cart.addItem(book, quantity);
        }
    }
    
    /**
     * Place an order from the current cart.
     */
    public Order placeOrder() {
        if (cart.getItems().isEmpty()) {
            return null;
        }
        
        Order order = new Order();
        order.setCustomerId(this.userId);
        order.setShippingAddress(this.defaultShippingAddress);
        order.setStatus(OrderStatus.PENDING);
        
        // Convert cart items to line items
        for (var entry : cart.getItems().entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();
            LineItem lineItem = new LineItem(
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                quantity
            );
            order.addLineItem(lineItem);
        }
        
        cart.clear();
        return order;
    }
    
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    
    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String defaultShippingAddress) { 
        this.defaultShippingAddress = defaultShippingAddress; 
    }
    
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    
    @Override
    public String toString() {
        return "Customer{" +
                "email='" + email + '\'' +
                ", loyaltyPoints=" + loyaltyPoints +
                ", cartSize=" + cart.getItems().size() +
                '}';
    }
}