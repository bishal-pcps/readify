package model;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int orderId;
    private double amount;
    private Timestamp paymentDate;
    private int paymentMethodId;
    private int paymentStatusId;
    private String transactionId;

    public Payment() {
    }

    public Payment(int orderId, double amount, int paymentMethodId, int paymentStatusId, String transactionId) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethodId = paymentMethodId;
        this.paymentStatusId = paymentStatusId;
        this.transactionId = transactionId;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public int getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(int paymentMethodId) { this.paymentMethodId = paymentMethodId; }

    public int getPaymentStatusId() { return paymentStatusId; }
    public void setPaymentStatusId(int paymentStatusId) { this.paymentStatusId = paymentStatusId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
