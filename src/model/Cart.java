package model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Cart {
    private int cartId;
    private int customerId;
    private Date creationDate;
    private Map<Book, Integer> items;
    
    public Cart() {
        this.items = new HashMap<>();
        this.creationDate = new Date();
    }
    
    public void addItem(Book book, int quantity) {
        items.put(book, items.getOrDefault(book, 0) + quantity);
    }
    
    public void removeItem(Book book) {
        items.remove(book);
    }
    
    public void updateQuantity(Book book, int quantity) {
        if (quantity <= 0) {
            removeItem(book);
        } else {
            items.put(book, quantity);
        }
    }
    
    public double getTotal() {
        double total = 0;
        for (Map.Entry<Book, Integer> entry : items.entrySet()) {
            total += entry.getKey().getPrice() * entry.getValue();
        }
        return total;
    }
    
    public int getItemCount() {
        int count = 0;
        for (int quantity : items.values()) {
            count += quantity;
        }
        return count;
    }
    
    public void clear() {
        items.clear();
    }
    
    // Getters and Setters
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Date getCreationDate() { return creationDate; }
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    
    public Map<Book, Integer> getItems() { return items; }
    public void setItems(Map<Book, Integer> items) { this.items = items; }
}