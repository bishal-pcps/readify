package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomerDAO extends GenericDAO<Object> {

    public CustomerDAO() throws SQLException {
        super();
    }

    public CustomerDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(Object entity) throws SQLException {
        return 0;
    }

    @Override
    public Object read(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Object> readAll() throws SQLException {
        return new ArrayList<>();
    }

    @Override
    public void update(Object entity) throws SQLException {

    }

    @Override
    public void delete(int id) throws SQLException {

    }

    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM customer";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    public int getTotalPurchasesByCustomer(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count " +
                     "FROM order_table o " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "WHERE c.user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    public double getTotalSpendByCustomer(int userId) throws SQLException {
        String sql = "SELECT SUM(o.total_amount) as total " +
                     "FROM order_table o " +
                     "JOIN customer c ON o.customer_id = c.customer_id " +
                     "WHERE c.user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("total");
                    return rs.wasNull() ? 0 : total;
                }
            }
        }
        return 0;
    }

    public Map<String, Integer> getCustomerGrowthByMonth() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
                     "FROM customer GROUP BY month ORDER BY month ASC LIMIT 6";
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("month"), rs.getInt("count"));
            }
        }
        return data;
    }

    public Integer readCustomerIdByUserId(int userId) throws SQLException {
        String sql = "SELECT customer_id FROM customer WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        }
        return null;
    }

    public Integer readUserIdByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT user_id FROM customer WHERE customer_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        }
        return null;
    }

    public int ensureCustomerProfile(int userId, String address) throws SQLException {
        Integer existing = readCustomerIdByUserId(userId);
        if (existing != null) {
            return existing;
        }

        String safeAddress = (address == null || address.isBlank()) ? "Address pending" : address;
        String sql = "INSERT INTO customer (user_id, address, loyalty_points) VALUES (?, ?, 0)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, safeAddress);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        Integer reRead = readCustomerIdByUserId(userId);
        if (reRead != null) {
            return reRead;
        }

        throw new SQLException("Failed to create customer profile for user_id=" + userId);
    }
}

