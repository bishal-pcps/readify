package model;

public class PaymentStatus {
    private int paymentStatusId;
    private String statusName;

    public PaymentStatus() {
    }

    public PaymentStatus(String statusName) {
        this.statusName = statusName;
    }

    public int getPaymentStatusId() { return paymentStatusId; }
    public void setPaymentStatusId(int paymentStatusId) { this.paymentStatusId = paymentStatusId; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
