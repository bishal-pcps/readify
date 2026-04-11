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

import model.User;

/**
 * Standardized UserDAO - Unified Auth against the 'user' table.
 */
public class UserDAO extends GenericDAO<User> {

    public UserDAO() throws SQLException {
        super();
    }

    public UserDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(User user) throws SQLException {
        String sql = "INSERT INTO user (name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFirstName() + " " + user.getLastName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setString(5, user.getRoleId() == 1 ? "ADMIN" : "CUSTOMER");
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
    public User read(int userId) throws SQLException {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
					return mapResultSetToUser(rs);
				}
            }
        }
        return null;
    }

    public User readByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
					return mapResultSetToUser(rs);
				}
            }
        }
        return null;
    }

    @Override
    public List<User> readAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user ORDER BY created_at DESC";
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE user SET name = ?, email = ?, password = ?, phone = ?, role = ?, loyalty_points = ? WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getFirstName() + " " + user.getLastName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPhoneNumber());
            stmt.setString(5, user.getRoleId() == 1 ? "ADMIN" : "CUSTOMER");
            stmt.setInt(6, Math.max(0, user.getLoyaltyPoints()));
            stmt.setInt(7, user.getUserId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM user WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));

        // Split name back to first/last if possible, else just use firstName
        String fullName = rs.getString("name");
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts[1]);
        } else {
            user.setFirstName(fullName);
            user.setLastName("");
        }

        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password"));
        user.setPhoneNumber(rs.getString("phone"));

        String roleStr = rs.getString("role");
        user.setRoleId("ADMIN".equalsIgnoreCase(roleStr) ? 1 : 2);

        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setLoyaltyPoints(rs.getInt("loyalty_points"));
        return user;
    }

    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM user WHERE role = 'CUSTOMER'";
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
                     "FROM user WHERE role = 'CUSTOMER' GROUP BY month ORDER BY month ASC LIMIT 6";
        try (Statement s = getConnection().createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                data.put(rs.getString("month"), rs.getInt("count"));
            }
        }
        return data;
    }
}

