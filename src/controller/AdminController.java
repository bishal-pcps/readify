package controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.logging.Logger;
import java.util.logging.Level;

import model.Order;
import model.User;
import model.Role;
import model.Book;
import dao.UserDAO;
import dao.OrderDAO;

public class AdminController {
    private static final Logger logger = Logger.getLogger(AdminController.class.getName());
    private BookController bookController;
    private OrderController orderController;
    private OrderDAO orderDAO;
    private UserDAO userDAO;
    private User currentUser;
    
    public AdminController(User currentUser) {
        this.currentUser = currentUser;
        this.bookController = new BookController();
        this.orderController = new OrderController();
        this.orderDAO = new OrderDAO();
        this.userDAO = new UserDAO();
    }
    
    private boolean checkAdminAccess() {
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            logger.log(Level.WARNING, "Unauthorized access attempt by user: " + 
                       (currentUser != null ? currentUser.getEmail() : "Unknown"));
            return false;
        }
        return true;
    }
    
    public Map<String, Object> getDashboardStats() {
        if (!checkAdminAccess()) return new HashMap<>();
        
        Map<String, Object> stats = orderDAO.getDashboardStats();
        
        // Add calculated stats or defaults if missing
        stats.putIfAbsent("totalRevenue", BigDecimal.ZERO);
        stats.putIfAbsent("totalOrders", 0);
        stats.putIfAbsent("avgOrderValue", BigDecimal.ZERO);
        stats.put("totalCustomers", getCustomerCount());
        
        // Format for display
        Map<String, Object> displayStats = new HashMap<>();
        displayStats.put("totalRevenue", String.format("$%,.2f", stats.get("totalRevenue")));
        displayStats.put("totalOrders", String.format("%,d", stats.get("totalOrders")));
        displayStats.put("totalCustomers", String.format("%,d", stats.get("totalCustomers")));
        displayStats.put("avgOrderValue", String.format("$%,.2f", stats.get("avgOrderValue")));
        
        return displayStats;
    }
    
    private int getCustomerCount() {
        // Optimization: In a real app, this should also be a COUNT query in UserDAO
        List<User> users = userDAO.getAllUsers();
        return (int) users.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
    }
    
    public List<Order> getRecentOrders() {
        if (!checkAdminAccess()) return List.of();
        return orderController.getRecentOrders(10);
    }
    
    public boolean addBook(Book book) {
        if (!checkAdminAccess()) return false;
        return bookController.addBook(book);
    }
    
    public boolean updateBook(Book book) {
        if (!checkAdminAccess()) return false;
        return bookController.updateBook(book);
    }
    
    public boolean deleteBook(int bookId) {
        if (!checkAdminAccess()) return false;
        return bookController.deleteBook(bookId);
    }
    
    public void updateOrderStatus(int orderId, String status) {
        if (!checkAdminAccess()) return;
        orderController.updateOrderStatus(orderId, status);
    }
}