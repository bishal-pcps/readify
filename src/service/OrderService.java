package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dao.CartItemDAO;
import dao.BookDAO;
import dao.CustomerDAO;
import dao.LineItemDAO;
import dao.LoyaltyDAO;
import dao.OrderDAO;
import dao.PaymentDAO;
import dao.UserDAO;
import model.CartItem;
import model.Book;
import model.LineItem;
import model.LoyaltyActivity;
import model.Order;
import model.User;
import util.DatabaseConnection;
import util.OrderPricing;
import util.OrderStatusUtil;

public class OrderService {
    private OrderDAO orderDAO;
    private LineItemDAO lineItemDAO;
    private CustomerDAO customerDAO;

    public OrderService() throws SQLException {
        this.orderDAO = new OrderDAO();
        this.lineItemDAO = new LineItemDAO();
        this.customerDAO = new CustomerDAO();
    }

    public int createOrderFromCart(int userId, int cartId, String shippingAddress) throws SQLException {
        return createOrderFromCart(userId, cartId, shippingAddress, null);
    }

    public int createOrderFromCart(int userId, int cartId, String shippingAddress, List<Integer> selectedCartItemIds) throws SQLException {
        try (Connection tx = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = tx.getAutoCommit();
            tx.setAutoCommit(false);
            try {
                CustomerDAO txCustomerDAO = new CustomerDAO(tx);
                CartItemDAO txCartItemDAO = new CartItemDAO(tx);
                BookDAO txBookDAO = new BookDAO(tx);
                OrderDAO txOrderDAO = new OrderDAO(tx);
                UserDAO txUserDAO = new UserDAO(tx);
                LoyaltyDAO txLoyaltyDAO = new LoyaltyDAO(tx);
                LineItemDAO txLineItemDAO = new LineItemDAO(tx);

                int customerId = txCustomerDAO.ensureCustomerProfile(userId, "Address pending");
                List<CartItem> cartItems = txCartItemDAO.readByCartId(cartId);
                if (cartItems.isEmpty()) {
                    throw new SQLException("Cart is empty");
                }

                List<CartItem> checkoutItems = cartItems;
                Set<Integer> selectedIdSet = new HashSet<>();
                if (selectedCartItemIds != null && !selectedCartItemIds.isEmpty()) {
                    for (Integer selectedId : selectedCartItemIds) {
                        if (selectedId != null && selectedId > 0) {
                            selectedIdSet.add(selectedId);
                        }
                    }

                    List<CartItem> filteredItems = new ArrayList<>();
                    for (CartItem item : cartItems) {
                        if (selectedIdSet.contains(item.getCartItemId())) {
                            filteredItems.add(item);
                        }
                    }
                    checkoutItems = filteredItems;
                }

                if (checkoutItems.isEmpty()) {
                    throw new SQLException("No selected books to checkout");
                }

                for (CartItem item : checkoutItems) {
                    Book book = txBookDAO.read(item.getBookId());
                    if (book == null) {
                        throw new SQLException("Book not found for ID: " + item.getBookId());
                    }
                    if (book.getStockLevel() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for " + book.getBookName());
                    }
                }

                double subtotalAmount = 0;
                for (CartItem item : checkoutItems) {
                    subtotalAmount += item.getPrice() * item.getQuantity();
                }

                double orderTotalAmount = OrderPricing.orderTotal(subtotalAmount);

                Order order = new Order();
                order.setUserId(customerId);
                order.setOrderDate(new Timestamp(System.currentTimeMillis()));
                order.setTotalAmount(orderTotalAmount);
                order.setOrderStatusId(OrderStatusUtil.PENDING);
                order.setShippingAddress(shippingAddress);

                int orderId = txOrderDAO.create(order);

                int pointsEarned = (int) Math.round(subtotalAmount * 10);
                User user = txUserDAO.read(userId);
                if (user != null) {
                    user.setLoyaltyPoints(user.getLoyaltyPoints() + pointsEarned);
                    txUserDAO.update(user);
                    txLoyaltyDAO.create(new LoyaltyActivity(userId, pointsEarned, "Earned from Order #" + orderId));
                }

                for (CartItem item : checkoutItems) {
                    LineItem lineItem = new LineItem();
                    lineItem.setOrderId(orderId);
                    lineItem.setBookId(item.getBookId());
                    lineItem.setQuantity(item.getQuantity());
                    lineItem.setPrice(item.getPrice());
                    txLineItemDAO.create(lineItem);
                }

                if (selectedIdSet.isEmpty()) {
                    txCartItemDAO.deleteByCartId(cartId);
                } else {
                    txCartItemDAO.deleteByIds(new ArrayList<>(selectedIdSet));
                }

                tx.commit();
                return orderId;
            } catch (Exception ex) {
                tx.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException("Failed to create order", ex);
            } finally {
                tx.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Order getOrder(int orderId) throws SQLException {
        return orderDAO.read(orderId);
    }

    public List<Order> getCustomerOrders(int userId) throws SQLException {
        int customerId = resolveCustomerId(userId);
        return orderDAO.readByUserId(customerId);
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.readAll();
    }

    public List<LineItem> getOrderLineItems(int orderId) throws SQLException {
        return lineItemDAO.readByOrderId(orderId);
    }

    public void updateOrderStatus(int orderId, int newStatusId) throws SQLException {
        Order order = orderDAO.read(orderId);
        if (order != null) {
            order.setOrderStatusId(newStatusId);
            orderDAO.update(order);
        }
    }

    public void fulfillOrder(int orderId) throws SQLException {
        try (Connection tx = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = tx.getAutoCommit();
            tx.setAutoCommit(false);
            try {
                OrderDAO txOrderDAO = new OrderDAO(tx);
                LineItemDAO txLineItemDAO = new LineItemDAO(tx);
                BookDAO txBookDAO = new BookDAO(tx);

                Order order = txOrderDAO.read(orderId);
                if (order == null) {
                    throw new SQLException("Order not found");
                }
                if (order.getOrderStatusId() == OrderStatusUtil.SHIPPED) {
                    tx.commit();
                    return;
                }
                if (order.getOrderStatusId() == OrderStatusUtil.CANCELLED) {
                    throw new SQLException("Cancelled order cannot be shipped");
                }

                List<LineItem> items = txLineItemDAO.readByOrderId(orderId);
                for (LineItem item : items) {
                    Book book = txBookDAO.read(item.getBookId());
                    if (book == null) {
                        throw new SQLException("Book not found for ID: " + item.getBookId());
                    }
                    if (book.getStockLevel() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for " + book.getBookName());
                    }
                }

                for (LineItem item : items) {
                    Book book = txBookDAO.read(item.getBookId());
                    if (book != null) {
                        book.setStockLevel(book.getStockLevel() - item.getQuantity());
                        txBookDAO.update(book);
                    }
                }

                order.setOrderStatusId(OrderStatusUtil.SHIPPED);
                txOrderDAO.update(order);

                tx.commit();
            } catch (Exception ex) {
                tx.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException("Failed to ship order", ex);
            } finally {
                tx.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public void cancelOrder(int orderId) throws SQLException {
        Order order = orderDAO.read(orderId);
        if (order == null) {
            throw new SQLException("Order not found");
        }
        if (order.getOrderStatusId() == OrderStatusUtil.CANCELLED) {
            return;
        }
        if (order.getOrderStatusId() == OrderStatusUtil.SHIPPED) {
            throw new SQLException("Shipped orders cannot be cancelled");
        }

        updateOrderStatus(orderId, OrderStatusUtil.CANCELLED);
    }

    public void deleteOrder(int orderId) throws SQLException {
        try (Connection tx = DatabaseConnection.getConnection()) {
            boolean previousAutoCommit = tx.getAutoCommit();
            tx.setAutoCommit(false);
            try {
                OrderDAO txOrderDAO = new OrderDAO(tx);
                PaymentDAO txPaymentDAO = new PaymentDAO(tx);
                LineItemDAO txLineItemDAO = new LineItemDAO(tx);

                Order order = txOrderDAO.read(orderId);
                if (order == null) {
                    throw new SQLException("Order not found");
                }

                txPaymentDAO.deleteByOrderId(orderId);

                List<LineItem> items = txLineItemDAO.readByOrderId(orderId);
                for (LineItem item : items) {
                    txLineItemDAO.delete(item.getLineItemId());
                }

                txOrderDAO.delete(orderId);
                tx.commit();
            } catch (Exception ex) {
                tx.rollback();
                if (ex instanceof SQLException sqlEx) {
                    throw sqlEx;
                }
                throw new SQLException("Failed to delete order", ex);
            } finally {
                tx.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Map<String, Double> getRevenueByMonth() throws SQLException {
        return orderDAO.getRevenueByMonth();
    }

    public Map<String, Integer> getOrderStatusDistribution() throws SQLException {
        return orderDAO.getOrderStatusDistribution();
    }

    public Map<String, Integer> getTopSellingBooks() throws SQLException {
        return orderDAO.getTopSellingBooks();
    }

    private int resolveCustomerId(int userId) throws SQLException {
        return customerDAO.ensureCustomerProfile(userId, "Address pending");
    }
}
