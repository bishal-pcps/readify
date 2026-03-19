package model;

import java.util.Date;

public class Payment {
    private int paymentId;
    private int orderId;
    private Date paymentDate;
    private double amount;
    private String paymentMethod; // "CREDIT_CARD", "PAYPAL", etc.
    private PaymentStatus status;
    private String transactionId;
    
    // Methods from your diagram
    public boolean processPayment() {
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = new Date();
        return true;
    }
    
    public boolean refundPayment() {
        if (status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
            return true;
        }
        return false;
    }
    
    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}