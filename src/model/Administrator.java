package model;

import java.util.List;

/**
 * Administrator user with elevated permissions.
 * Can manage books, users, and view all orders.
 */
public class Administrator extends User {
    private String adminLevel;
    private String department;
    
    public Administrator() {
        this.role = Role.ADMIN;
    }
    
    /**
     * Create a new book record.
     */
    public boolean createBookRecord(Book book) {
        return book != null && book.getBookId() > 0;
    }
    
    /**
     * Update book details.
     */
    public boolean updateBookDetails(int bookId, Book book) {
        return bookId > 0 && book != null;
    }
    
    /**
     * Delete a book record.
     */
    public boolean deleteBookRecord(int bookId) {
        return bookId > 0;
    }
    
    /**
     * View all books in the system.
     */
    public List<Book> viewAllBooks() {
        return null; // Implementation in BookDAO
    }
    
    /**
     * Manage all orders in the system.
     */
    public List<Order> manageOrders() {
        return null; // Implementation in OrderDAO
    }
    
    /**
     * Generate reports.
     */
    public Report generateReport(String type) {
        Report report = new Report();
        report.setType(type);
        report.setGeneratedDate(new java.util.Date());
        return report;
    }
    
    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}