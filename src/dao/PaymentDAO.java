package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Payment;

public class PaymentDAO extends GenericDAO<Payment> {

    public PaymentDAO() throws SQLException {
        super();
    }

    public PaymentDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(Payment payment) throws SQLException {
        String sql = "INSERT INTO payment (order_id, amount, payment_method_id, payment_status_id, transaction_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setInt(3, payment.getPaymentMethodId());
            stmt.setInt(4, payment.getPaymentStatusId());
            stmt.setString(5, payment.getTransactionId());

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
    public Payment read(int paymentId) throws SQLException {
        String sql = "SELECT * FROM payment WHERE payment_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }
        return null;
    }

    public Payment readByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM payment WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }
        return null;
    }

    public List<Payment> readAllByOrderId(int orderId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }
        return payments;
    }

    @Override
    public List<Payment> readAll() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    @Override
    public void update(Payment payment) throws SQLException {
        String sql = "UPDATE payment SET order_id = ?, amount = ?, payment_method_id = ?, payment_status_id = ?, transaction_id = ? WHERE payment_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setInt(3, payment.getPaymentMethodId());
            stmt.setInt(4, payment.getPaymentStatusId());
            stmt.setString(5, payment.getTransactionId());
            stmt.setInt(6, payment.getPaymentId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int paymentId) throws SQLException {
        String sql = "DELETE FROM payment WHERE payment_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paymentId);
            stmt.executeUpdate();
        }
    }

    public void deleteByOrderId(int orderId) throws SQLException {
        String sql = "DELETE FROM payment WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.executeUpdate();
        }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentDate(rs.getTimestamp("payment_date"));
        payment.setPaymentMethodId(rs.getInt("payment_method_id"));
        payment.setPaymentStatusId(rs.getInt("payment_status_id"));
        payment.setTransactionId(rs.getString("transaction_id"));
        return payment;
    }
}

