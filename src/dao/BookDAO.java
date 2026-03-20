package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.math.BigDecimal;

import model.Book;

/**
 * Data Access Object for Book entities.
 * Handles all book-related database operations.
 */
public class BookDAO {
    private static final Logger logger = Logger.getLogger(BookDAO.class.getName());
    
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return books;
    }
    
    public Book getBookById(int bookId) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractBookFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public List<Book> getBooksByCategory(String category) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE category = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return books;
    }
    
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR description LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return books;
    }
    
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, publication_date, " +
                     "price, stock_level, category, description, rating, review_count, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setDate(5, book.getPublicationDate() != null ? 
                new java.sql.Date(book.getPublicationDate().getTime()) : null);
            pstmt.setBigDecimal(6, book.getPrice());
            pstmt.setInt(7, book.getStockLevel());
            pstmt.setString(8, book.getCategory());
            pstmt.setString(9, book.getDescription());
            pstmt.setDouble(10, book.getRating());
            pstmt.setInt(11, book.getReviewCount());
            pstmt.setString(12, book.getImageUrl());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, " +
                     "publication_date=?, price=?, stock_level=?, category=?, " +
                     "description=?, rating=?, review_count=?, image_url=? " +
                     "WHERE book_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getPublisher());
            pstmt.setDate(5, book.getPublicationDate() != null ? 
                new java.sql.Date(book.getPublicationDate().getTime()) : null);
            pstmt.setBigDecimal(6, book.getPrice());
            pstmt.setInt(7, book.getStockLevel());
            pstmt.setString(8, book.getCategory());
            pstmt.setString(9, book.getDescription());
            pstmt.setDouble(10, book.getRating());
            pstmt.setInt(11, book.getReviewCount());
            pstmt.setString(12, book.getImageUrl());
            pstmt.setInt(13, book.getBookId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateStock(int bookId, int newQuantity) {
        String sql = "UPDATE books SET stock_level = ? WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, bookId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublicationDate(rs.getDate("publication_date"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setStockLevel(rs.getInt("stock_level"));
        book.setCategory(rs.getString("category"));
        book.setDescription(rs.getString("description"));
        book.setRating(rs.getDouble("rating"));
        book.setReviewCount(rs.getInt("review_count"));
        book.setImageUrl(rs.getString("image_url"));
        return book;
    }
}
