package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a customer order.
 * Contains order items as LineItem objects with proper type safety.
 */
public class Order {
    private int orderId;
    private int customerId;
    private Date orderDate;
    private String shippingAddress;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<LineItem> items;
    private Payment payment;
    
    public Order() {
        this.orderDate = new Date();
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
    }
    
    /**
     * Add a line item to this order.
     */
    public void addLineItem(LineItem item) {
        if (item != null) {
            items.add(item);
            recalculateTotal();
        }
    }
    
    /**
     * Recalculate total amount based on line items.
     */
    private void recalculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            total = total.add(item.getTotalPrice());
        }
        this.totalAmount = total;
    }
    
    /**
     * Update order status with validation.
     */
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }
    
    /**
     * Cancel order if it's in PENDING or PROCESSING state.
     */
    public boolean cancelOrder() {
        if (status == OrderStatus.PENDING || status == OrderStatus.PROCESSING) {
            this.status = OrderStatus.CANCELLED;
            return true;
        }
        return false;
    }
    
    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<LineItem> getItems() { return items; }
    public void setItems(List<LineItem> items) { 
        this.items = items != null ? items : new ArrayList<>();
        recalculateTotal();
    }
    
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", itemCount=" + items.size() +
                '}';
    }
}