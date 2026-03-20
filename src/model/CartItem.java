package model;

import java.math.BigDecimal;

/**
 * Represents an item in the shopping cart.
 */
public class CartItem {
    private Book book;
    private int quantity;
    
    public CartItem(Book book, int quantity) {
        this.book = book;
        this.quantity = quantity;
    }
    
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    /**
     * Get the subtotal for this item (price * quantity).
     */
    public BigDecimal getSubtotal() {
        if (book == null || book.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return book.getPrice().multiply(new BigDecimal(quantity));
    }
}