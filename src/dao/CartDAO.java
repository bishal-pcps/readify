package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Cart;

public class CartDAO extends GenericDAO<Cart> {

    public CartDAO() throws SQLException {
        super();
    }

    @Override
    public int create(Cart cart) throws SQLException {
        String sql = "INSERT INTO cart (customer_id) VALUES (?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cart.getUserId());

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
    public Cart read(int cartId) throws SQLException {
        String sql = "SELECT * FROM cart WHERE cart_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            }
        }
        return null;
    }

    public Cart readByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM cart WHERE customer_id = ? ORDER BY creation_date DESC LIMIT 1";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Cart> readAll() throws SQLException {
        List<Cart> carts = new ArrayList<>();
        String sql = "SELECT * FROM cart";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                carts.add(mapResultSetToCart(rs));
            }
        }
        return carts;
    }

    @Override
    public void update(Cart cart) throws SQLException {
        String sql = "UPDATE cart SET customer_id = ? WHERE cart_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cart.getUserId());
            stmt.setInt(2, cart.getCartId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int cartId) throws SQLException {
        String sql = "DELETE FROM cart WHERE cart_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
        }
    }

    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setCartId(rs.getInt("cart_id"));
        cart.setUserId(rs.getInt("customer_id"));
        cart.setCreatedAt(rs.getTimestamp("creation_date"));
        cart.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cart;
    }
}

