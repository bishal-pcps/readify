package controller;

import java.util.List;
import model.Order;
import model.OrderStatus;
import dao.OrderDAO;

public class OrderController {
    private OrderDAO orderDAO;
    
    public OrderController() {
        this.orderDAO = new OrderDAO();
    }
    
    public int createOrder(Order order) {
        return orderDAO.createOrder(order);
    }
    
    public List<Order> getOrdersByCustomer(int customerId) {
        return orderDAO.getOrdersByCustomer(customerId);
    }
    
    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }
    
    public List<Order> getRecentOrders(int limit) {
        return orderDAO.getRecentOrders(limit);
    }
    
    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        return orderDAO.updateOrderStatus(orderId, status);
    }
    
    public boolean updateOrderStatus(int orderId, String status) {
        return orderDAO.updateOrderStatus(orderId, OrderStatus.valueOf(status));
    }
}
