package controller;

import java.sql.SQLException;
import java.util.Map;

import dao.UserDAO;
import model.Book;
import model.Order;
import service.AdminService;
import service.BookService;
import service.OrderService;
import util.OrderStatusUtil;

public class AdminController {
    private AdminService adminService;
    private BookService bookService;
    private OrderService orderService;
    private UserDAO userDAO;

    public AdminController() throws SQLException {
        this.adminService = new AdminService();
        this.bookService = new BookService();
        this.orderService = new OrderService();
        this.userDAO = new UserDAO();
    }

    // Book management
    public int addBook(Book book) throws SQLException {
        return bookService.addBook(book);
    }

    public void updateBook(Book book) throws SQLException {
        bookService.updateBook(book);
    }

    public void deleteBook(int bookId) throws SQLException {
        bookService.deleteBook(bookId);
    }

    // Admin account management
    public int createAdmin(String firstName, String lastName, String email, String password) throws SQLException {
        return adminService.createAdmin(firstName, lastName, email, password);
    }

    public void updateAdminProfile(int adminId, String firstName, String lastName, String phone) throws SQLException {
        adminService.updateAdminProfile(adminId, firstName, lastName, phone);
    }

    public void updateAdminPassword(int adminId, String newPassword) throws SQLException {
        adminService.updateAdminPassword(adminId, newPassword);
    }

    // Reports and analytics
    public int getTotalCustomers() throws SQLException {
        return userDAO.getTotalCustomers();
    }

    public double getTotalRevenue() throws SQLException {
        double total = 0;
        for (Order order : orderService.getAllOrders()) {
            if (!OrderStatusUtil.isCancelled(order.getOrderStatusId())) {
                total += order.getTotalAmount();
            }
        }
        return total;
    }

    public int getTotalOrders() throws SQLException {
        return orderService.getAllOrders().size();
    }

    public int getTotalCustomerPurchases(int userId) throws SQLException {
        return userDAO.getTotalPurchasesByCustomer(userId);
    }

    public double getTotalCustomerSpend(int userId) throws SQLException {
        return userDAO.getTotalSpendByCustomer(userId);
    }

    public Map<String, Double> getRevenueData() throws SQLException {
        return orderService.getRevenueByMonth();
    }

    public Map<String, Integer> getOrderStatusData() throws SQLException {
        return orderService.getOrderStatusDistribution();
    }

    public Map<String, Integer> getTopSellingBooksData() throws SQLException {
        return orderService.getTopSellingBooks();
    }

    public Map<String, Integer> getCustomerGrowthData() throws SQLException {
        return userDAO.getCustomerGrowthByMonth();
    }
}
