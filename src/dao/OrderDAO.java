package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.Order;
import util.OrderStatusUtil;

public class OrderDAO extends GenericDAO<Order> {

    public OrderDAO() throws SQLException {
        super();
    }

    public OrderDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(Order order) throws SQLException {
        String sql = "INSERT INTO order_table (customer_id, total_amount, status_id, shipping_address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getUserId());
            stmt.setDouble(2, order.getTotalAmount());
            stmt.setInt(3, order.getOrderStatusId());
            stmt.setString(4, order.getShippingAddress());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public Order read(int orderId) throws SQLException {
        String sql = "SELECT * FROM order_table WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }
        return null;
    }

    public List<Order> readByUserId(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM order_table WHERE customer_id = ? ORDER BY order_date DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        return orders;
    }

    @Override
    public List<Order> readAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM order_table ORDER BY order_date DESC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        }
        return orders;
    }

    @Override
    public void update(Order order) throws SQLException {
        String sql = "UPDATE order_table SET customer_id = ?, total_amount = ?, status_id = ?, shipping_address = ? WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, order.getUserId());
            stmt.setDouble(2, order.getTotalAmount());
            stmt.setInt(3, order.getOrderStatusId());
            stmt.setString(4, order.getShippingAddress());
            stmt.setInt(5, order.getOrderId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int orderId) throws SQLException {
        String sql = "DELETE FROM order_table WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("customer_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setOrderStatusId(rs.getInt("status_id"));
        order.setShippingAddress(rs.getString("shipping_address"));
        return order;
    }

    public Map<String, Double> getRevenueByMonth() throws SQLException {
        Map<String, Double> data = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') as month, SUM(total_amount) as total " +
                     "FROM order_table WHERE status_id != " + OrderStatusUtil.CANCELLED + " GROUP BY month ORDER BY month ASC LIMIT 6";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("month"), rs.getDouble("total"));
            }
        }
        return data;
    }

    public Map<String, Integer> getOrderStatusDistribution() throws SQLException {
        Map<String, Integer> data = new HashMap<>();
        String sql = "SELECT status_id, COUNT(*) as count FROM order_table GROUP BY status_id";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int statusId = rs.getInt("status_id");
                String label = OrderStatusUtil.label(statusId);
                data.put(label, rs.getInt("count"));
            }
        }
        return data;
    }

    public Map<String, Integer> getTopSellingBooks() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT b.title, SUM(oi.quantity) as total_sold " +
                     "FROM order_item oi JOIN book b ON oi.book_id = b.book_id " +
                     "GROUP BY b.book_id ORDER BY total_sold DESC LIMIT 5";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("title"), rs.getInt("total_sold"));
            }
        }
        return data;
    }
}

