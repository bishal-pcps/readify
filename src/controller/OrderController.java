package controller;

import java.sql.SQLException;
import java.util.List;

import model.LineItem;
import model.Order;
import service.OrderService;

public class OrderController {
    private OrderService orderService;

    public OrderController() throws SQLException {
        this.orderService = new OrderService();
    }

    public int placeOrder(int userId, int cartId, String shippingAddress) throws SQLException {
        return placeOrder(userId, cartId, shippingAddress, null);
    }

    public int placeOrder(int userId, int cartId, String shippingAddress, List<Integer> selectedCartItemIds) throws SQLException {
        return orderService.createOrderFromCart(userId, cartId, shippingAddress, selectedCartItemIds);
    }

    public Order getOrder(int orderId) throws SQLException {
        return orderService.getOrder(orderId);
    }

    public List<Order> getCustomerOrders(int userId) throws SQLException {
        return orderService.getCustomerOrders(userId);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderService.getAllOrders();
    }

    public List<LineItem> getOrderDetails(int orderId) throws SQLException {
        return orderService.getOrderLineItems(orderId);
    }

    public void updateOrderStatus(int orderId, int statusId) throws SQLException {
        orderService.updateOrderStatus(orderId, statusId);
    }

    public void shipOrder(int orderId) throws SQLException {
        orderService.fulfillOrder(orderId);
    }

    public void cancelOrder(int orderId) throws SQLException {
        orderService.cancelOrder(orderId);
    }

    public void deleteOrder(int orderId) throws SQLException {
        orderService.deleteOrder(orderId);
    }
}
