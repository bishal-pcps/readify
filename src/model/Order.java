package model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Order {
    private int orderId;
    private int customerId;
    private Date orderDate;
    private String shippingAddress;
    private double totalAmount;
    private OrderStatus status;
    private List<Map.Entry<Book, Integer>> items;
    private Payment payment;
    
    public Order() {
        this.orderDate = new Date();
    }
    
    // Methods from your diagram
    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
    
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
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public List<Map.Entry<Book, Integer>> getItems() { return items; }
    public void setItems(List<Map.Entry<Book, Integer>> items) { this.items = items; }
    
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
}