package model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Shopping cart for customers.
 * Uses BigDecimal for monetary calculations to avoid precision issues.
 */
public class Cart {
    private int cartId;
    private int customerId;
    private Date creationDate;
    private Map<Book, Integer> items;
    
    public Cart() {
        this.items = new HashMap<>();
        this.creationDate = new Date();
    }
    
    /**
     * Add an item to the cart.
     */
    public void addItem(Book book, int quantity) {
        if (book != null && quantity > 0) {
            items.put(book, items.getOrDefault(book, 0) + quantity);
        }
    }
    
    /**
     * Remove an item from the cart.
     */
    public void removeItem(Book book) {
        if (book != null) {
            items.remove(book);
        }
    }
    
    /**
     * Update quantity of an item in cart.
     */
    public void updateQuantity(Book book, int quantity) {
        if (book == null) return;
        
        if (quantity <= 0) {
            removeItem(book);
        } else {
            items.put(book, quantity);
        }
    }
    
    /**
     * Get the total price of all items in cart with precise decimal handling.
     */
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Book, Integer> entry : items.entrySet()) {
            Book book = entry.getKey();
            int quantity = entry.getValue();
            BigDecimal price = book.getPrice();
            if (price != null) {
                total = total.add(price.multiply(new BigDecimal(quantity)));
            }
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
    
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Date getCreationDate() { return creationDate; }
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }
    
    public Map<Book, Integer> getItems() { return items; }
    public void setItems(Map<Book, Integer> items) { this.items = items; }
}