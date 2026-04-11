package service;

import java.sql.SQLException;
import java.util.List;

import dao.BookDAO;
import dao.CartDAO;
import dao.CartItemDAO;
import dao.CustomerDAO;

import model.Book;
import model.Cart;
import model.CartItem;

public class CartService {
    private CartDAO cartDAO;
    private CartItemDAO cartItemDAO;
    private CustomerDAO customerDAO;
    private BookDAO bookDAO;

    public CartService() throws SQLException {
        this.cartDAO = new CartDAO();
        this.cartItemDAO = new CartItemDAO();
        this.customerDAO = new CustomerDAO();
        this.bookDAO = new BookDAO();
    }

    public Cart getOrCreateCart(int userId) throws SQLException {
        int customerId = resolveCustomerId(userId);
        Cart cart = cartDAO.readByUserId(customerId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(customerId);
            int cartId = cartDAO.create(cart);
            cart.setCartId(cartId);
        }
        return cart;
    }

    private int resolveCustomerId(int userId) throws SQLException {
        return customerDAO.ensureCustomerProfile(userId, "Address pending");
    }

    public void addItemToCart(int cartId, int bookId, int quantity, double price) throws SQLException {
        if (quantity <= 0) {
            throw new SQLException("Quantity must be greater than zero");
        }

        Book book = bookDAO.read(bookId);
        if (book == null) {
            throw new SQLException("Book not found");
        }
        int maxStock = Math.max(0, book.getStockLevel());
        if (maxStock == 0) {
            throw new SQLException("Book is out of stock");
        }

        CartItem existingItem = null;
        List<CartItem> items = cartItemDAO.readByCartId(cartId);
        for (CartItem item : items) {
            if (item.getBookId() == bookId) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            int mergedQty = Math.min(maxStock, existingItem.getQuantity() + quantity);
            if (mergedQty <= 0) {
                throw new SQLException("Unable to add book to cart");
            }
            existingItem.setQuantity(mergedQty);
            cartItemDAO.update(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCartId(cartId);
            cartItem.setBookId(bookId);
            cartItem.setQuantity(Math.min(maxStock, quantity));
            cartItem.setPrice(price);
            cartItemDAO.create(cartItem);
        }
    }

    public void removeItemFromCart(int cartItemId) throws SQLException {
        cartItemDAO.delete(cartItemId);
    }

    public void updateCartItem(int cartItemId, int quantity) throws SQLException {
        CartItem item = cartItemDAO.read(cartItemId);
        if (item != null) {
            if (quantity <= 0) {
                cartItemDAO.delete(cartItemId);
                return;
            }

            Book book = bookDAO.read(item.getBookId());
            int maxStock = book == null ? quantity : Math.max(1, book.getStockLevel());
            item.setQuantity(Math.min(quantity, maxStock));
            cartItemDAO.update(item);
        }
    }

    public List<CartItem> getCartItems(int cartId) throws SQLException {
        return cartItemDAO.readByCartId(cartId);
    }

    public double calculateCartTotal(int cartId) throws SQLException {
        List<CartItem> items = cartItemDAO.readByCartId(cartId);
        double total = 0;
        for (CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public void clearCart(int cartId) throws SQLException {
        cartItemDAO.deleteByCartId(cartId);
    }
}
