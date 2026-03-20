package dao;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;

import model.Payment;
import model.PaymentStatus;

public class PaymentDAO {
    private static final Logger logger = Logger.getLogger(PaymentDAO.class.getName());
    
    public boolean createPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, amount, payment_method, status, transaction_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null) return false;
            
            // Transaction for atomicity
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, payment.getOrderId());
                pstmt.setBigDecimal(2, payment.getAmount());
                pstmt.setString(3, payment.getPaymentMethod());
                pstmt.setString(4, payment.getStatus().name());
                pstmt.setString(5, payment.getTransactionId());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Error creating payment, rolling back", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in createPayment", e);
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
        return false;
    }
    
    public Payment getPaymentByOrderId(int orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Payment payment = new Payment();
                payment.setPaymentId(rs.getInt("payment_id"));
                payment.setOrderId(rs.getInt("order_id"));
                payment.setPaymentDate(rs.getTimestamp("payment_date"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setStatus(PaymentStatus.valueOf(rs.getString("status")));
                payment.setTransactionId(rs.getString("transaction_id"));
                return payment;
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting payment by order ID", e);
        }
        
        return null;
    }
    
    public boolean updatePaymentStatus(int paymentId, PaymentStatus status) {
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status.name());
            pstmt.setInt(2, paymentId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating payment status", e);
            return false;
        }
    }
}