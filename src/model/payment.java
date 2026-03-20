package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents a payment transaction for an order.
 * Tracks payment status, method, and amount with proper decimal precision.
 */
public class Payment {
    private int paymentId;
    private int orderId;
    private Date paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionId;
    
    /**
     * Process this payment and mark as completed.
     */
    public boolean processPayment() {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = PaymentStatus.COMPLETED;
            this.paymentDate = new Date();
            return true;
        }
        return false;
    }
    
    /**
     * Refund this payment if it was completed.
     */
    public boolean refundPayment() {
        if (status == PaymentStatus.COMPLETED) {
            this.status = PaymentStatus.REFUNDED;
            return true;
        }
        return false;
    }
    
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    @Override
    public String toString() {
        return "Payment{" +
                "orderId=" + orderId +
                ", amount=" + amount +
                ", status=" + status +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}
