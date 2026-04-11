package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.LoyaltyActivity;

public class LoyaltyDAO extends GenericDAO<LoyaltyActivity> {

    public LoyaltyDAO() throws SQLException {
        super();
    }

    public LoyaltyDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(LoyaltyActivity activity) throws SQLException {
        String sql = "INSERT INTO loyalty_activity (user_id, points_change, description) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, activity.getUserId());
            stmt.setInt(2, activity.getPointsChange());
            stmt.setString(3, activity.getDescription());
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
    public LoyaltyActivity read(int id) throws SQLException {
        String sql = "SELECT * FROM loyalty_activity WHERE activity_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToActivity(rs);
                }
            }
        }
        return null;
    }

    public List<LoyaltyActivity> readByUserId(int userId) throws SQLException {
        List<LoyaltyActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM loyalty_activity WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapResultSetToActivity(rs));
                }
            }
        }
        return activities;
    }

    @Override
    public List<LoyaltyActivity> readAll() throws SQLException {
        List<LoyaltyActivity> activities = new ArrayList<>();
        String sql = "SELECT * FROM loyalty_activity ORDER BY created_at DESC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                activities.add(mapResultSetToActivity(rs));
            }
        }
        return activities;
    }

    @Override
    public void update(LoyaltyActivity activity) throws SQLException {
        // Loyalty activities are usually immutable, but for completeness:
        String sql = "UPDATE loyalty_activity SET user_id = ?, points_change = ?, description = ? WHERE activity_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, activity.getUserId());
            stmt.setInt(2, activity.getPointsChange());
            stmt.setString(3, activity.getDescription());
            stmt.setInt(4, activity.getActivityId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM loyalty_activity WHERE activity_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private LoyaltyActivity mapResultSetToActivity(ResultSet rs) throws SQLException {
        LoyaltyActivity activity = new LoyaltyActivity();
        activity.setActivityId(rs.getInt("activity_id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setPointsChange(rs.getInt("points_change"));
        activity.setDescription(rs.getString("description"));
        activity.setCreatedAt(rs.getTimestamp("created_at"));
        return activity;
    }
}

