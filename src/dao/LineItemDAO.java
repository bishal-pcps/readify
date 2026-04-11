package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.LineItem;

public class LineItemDAO extends GenericDAO<LineItem> {

    public LineItemDAO() throws SQLException {
        super();
    }

    public LineItemDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(LineItem lineItem) throws SQLException {
        String sql = "INSERT INTO order_item (order_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, lineItem.getOrderId());
            stmt.setInt(2, lineItem.getBookId());
            stmt.setInt(3, lineItem.getQuantity());
            stmt.setDouble(4, lineItem.getPrice());

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
    public LineItem read(int lineItemId) throws SQLException {
        String sql = "SELECT * FROM order_item WHERE order_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, lineItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLineItem(rs);
                }
            }
        }
        return null;
    }

    public List<LineItem> readByOrderId(int orderId) throws SQLException {
        List<LineItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_item WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToLineItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<LineItem> readAll() throws SQLException {
        List<LineItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_item";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToLineItem(rs));
            }
        }
        return items;
    }

    @Override
    public void update(LineItem lineItem) throws SQLException {
        String sql = "UPDATE order_item SET quantity = ?, price = ? WHERE order_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, lineItem.getQuantity());
            stmt.setDouble(2, lineItem.getPrice());
            stmt.setInt(3, lineItem.getLineItemId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int lineItemId) throws SQLException {
        String sql = "DELETE FROM order_item WHERE order_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, lineItemId);
            stmt.executeUpdate();
        }
    }

    private LineItem mapResultSetToLineItem(ResultSet rs) throws SQLException {
        LineItem item = new LineItem();
        item.setLineItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setBookId(rs.getInt("book_id"));
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getDouble("price"));
        return item;
    }
}

