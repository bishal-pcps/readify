package service;

import java.sql.SQLException;
import java.util.List;

import dao.BookDAO;

import model.Book;


public class BookService {
    private BookDAO bookDAO;
//    private InventoryDAO inventoryDAO; // Removed as stock is in book table

    public BookService() throws SQLException {
        this.bookDAO = new BookDAO();
//        this.inventoryDAO = new InventoryDAO();
    }

    public int addBook(Book book) throws SQLException {
        return bookDAO.create(book);
    }

    public Book getBook(int bookId) throws SQLException {
        return bookDAO.read(bookId);
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.readAll();
    }

    public List<Book> searchBooks(String bookName) throws SQLException {
        return bookDAO.searchByName(bookName);
    }

    public List<Book> getBooksByCategory(int categoryId) throws SQLException {
        return bookDAO.getBooksByCategory(categoryId);
    }

    public void updateBook(Book book) throws SQLException {
        bookDAO.update(book);
    }

    public void deleteBook(int bookId) throws SQLException {
        bookDAO.delete(bookId);
    }

    public boolean isBookAvailable(int bookId, int requiredQuantity) throws SQLException {
        Book book = bookDAO.read(bookId);
        return book != null && book.getStockLevel() >= requiredQuantity;
    }

    public int getAvailableQuantity(int bookId) throws SQLException {
        Book book = bookDAO.read(bookId);
        return book != null ? book.getStockLevel() : 0;
    }
}
