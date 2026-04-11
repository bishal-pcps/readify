package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.CartItem;

public class CartItemDAO extends GenericDAO<CartItem> {

    public CartItemDAO() throws SQLException {
        super();
    }

    public CartItemDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(CartItem cartItem) throws SQLException {
        String sql = "INSERT INTO cart_item (cart_id, book_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cartItem.getCartId());
            stmt.setInt(2, cartItem.getBookId());
            stmt.setInt(3, cartItem.getQuantity());
            stmt.setDouble(4, cartItem.getPrice());

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
    public CartItem read(int cartItemId) throws SQLException {
        String sql = "SELECT * FROM cart_item WHERE cart_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCartItem(rs);
                }
            }
        }
        return null;
    }

    public List<CartItem> readByCartId(int cartId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.*, b.title AS book_title FROM cart_item ci LEFT JOIN book b ON b.book_id = ci.book_id WHERE ci.cart_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToCartItem(rs));
                }
            }
        }
        return items;
    }

    @Override
    public List<CartItem> readAll() throws SQLException {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT * FROM cart_item";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(mapResultSetToCartItem(rs));
            }
        }
        return items;
    }

    @Override
    public void update(CartItem cartItem) throws SQLException {
        String sql = "UPDATE cart_item SET quantity = ?, price = ? WHERE cart_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartItem.getQuantity());
            stmt.setDouble(2, cartItem.getPrice());
            stmt.setInt(3, cartItem.getCartItemId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int cartItemId) throws SQLException {
        String sql = "DELETE FROM cart_item WHERE cart_item_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartItemId);
            stmt.executeUpdate();
        }
    }

    public void deleteByCartId(int cartId) throws SQLException {
        String sql = "DELETE FROM cart_item WHERE cart_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, cartId);
            stmt.executeUpdate();
        }
    }

    public void deleteByIds(List<Integer> cartItemIds) throws SQLException {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }

        List<Integer> sanitizedIds = new ArrayList<>();
        for (Integer id : cartItemIds) {
            if (id != null && id > 0 && !sanitizedIds.contains(id)) {
                sanitizedIds.add(id);
            }
        }
        if (sanitizedIds.isEmpty()) {
            return;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < sanitizedIds.size(); i++) {
            if (i > 0) {
                placeholders.append(",");
            }
            placeholders.append("?");
        }

        String sql = "DELETE FROM cart_item WHERE cart_item_id IN (" + placeholders + ")";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < sanitizedIds.size(); i++) {
                stmt.setInt(i + 1, sanitizedIds.get(i));
            }
            stmt.executeUpdate();
        }
    }

    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setCartId(rs.getInt("cart_id"));
        item.setBookId(rs.getInt("book_id"));
        try {
            item.setBookName(rs.getString("book_title"));
        } catch (SQLException ignored) {
            item.setBookName(null);
        }
        item.setQuantity(rs.getInt("quantity"));
        item.setPrice(rs.getDouble("price"));
        return item;
    }
}

