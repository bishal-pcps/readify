package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;

import model.Order;
import model.Book;
import model.LineItem;
import model.OrderStatus;

public class OrderDAO {
    private static final Logger logger = Logger.getLogger(OrderDAO.class.getName());
    
    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (customer_id, order_date, shipping_address, " +
                     "total_amount, status) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return -1;
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getCustomerId());
                pstmt.setTimestamp(2, new Timestamp(order.getOrderDate().getTime()));
                pstmt.setString(3, order.getShippingAddress());
                pstmt.setBigDecimal(4, order.getTotalAmount());
                pstmt.setString(5, order.getStatus().name());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            saveOrderItems(orderId, order.getItems(), conn);
                            
                            // Commit transaction
                            conn.commit();
                            return orderId;
                        }
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                logger.log(Level.SEVERE, "Error creating order, rolling back", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
        
        return -1;
    }
    
    private void saveOrderItems(int orderId, List<LineItem> items, Connection conn) 
            throws SQLException {
        
        String sql = "INSERT INTO order_items (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (LineItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getBookId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setBigDecimal(4, item.getUnitPrice());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }
    
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching customer orders", e);
        }
        
        return orders;
    }
    
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                order.setItems(getOrderItems(order.getOrderId(), conn));
                orders.add(order);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching all orders", e);
        }
        
        return orders;
    }
    
    public List<Order> getRecentOrders(int limit) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    order.setItems(getOrderItems(order.getOrderId(), conn));
                    orders.add(order);
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching recent orders", e);
        }
        
        return orders;
    }
    
    public java.util.Map<String, Object> getDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        String sql = "SELECT COUNT(*) as total_orders, SUM(total_amount) as total_revenue, " +
                     "AVG(total_amount) as avg_order_value FROM orders";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("totalOrders", rs.getInt("total_orders"));
                stats.put("totalRevenue", rs.getBigDecimal("total_revenue"));
                stats.put("avgOrderValue", rs.getBigDecimal("avg_order_value"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching dashboard stats", e);
        }
        return stats;
    }

    public boolean updateOrderStatus(int orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, orderId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating order status", e);
            return false;
        }
    }
    
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        return order;
    }
    
    private List<LineItem> getOrderItems(int orderId, Connection conn) 
            throws SQLException {
        
        List<LineItem> items = new ArrayList<>();
        // Fixed: Joining with books table to get title and author
        String sql = "SELECT oi.*, b.title, b.author FROM order_items oi " +
                     "JOIN books b ON oi.book_id = b.book_id " +
                     "WHERE oi.order_id = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LineItem item = new LineItem(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity")
                    );
                    item.setLineItemId(rs.getInt("order_item_id"));
                    item.setOrderId(orderId);
                    items.add(item);
                }
            }
        }
        
        return items;
    }
}