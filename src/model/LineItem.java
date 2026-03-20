package model;

import java.math.BigDecimal;

/**
 * Represents a single line item in an order.
 * Contains book information and quantity ordered.
 */
public class LineItem {
    private int lineItemId;
    private int orderId;
    private int bookId;
    private String bookTitle;
    private String bookAuthor;
    private BigDecimal unitPrice;
    private int quantity;
    
    /**
     * Constructs a LineItem with essential information.
     */
    public LineItem(int bookId, String bookTitle, String bookAuthor, 
                    BigDecimal unitPrice, int quantity) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
    
    /**
     * Get the total price for this line item (unitPrice * quantity).
     */
    public BigDecimal getTotalPrice() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(new BigDecimal(quantity));
    }
    
    // Getters and Setters
    public int getLineItemId() { return lineItemId; }
    public void setLineItemId(int lineItemId) { this.lineItemId = lineItemId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    @Override
    public String toString() {
        return "LineItem{" +
                "bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
