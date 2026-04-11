package model;

public class OrderStatus {
    private int statusId;
    private String statusName;

    public OrderStatus() {
    }

    public OrderStatus(String statusName) {
        this.statusName = statusName;
    }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }
}
