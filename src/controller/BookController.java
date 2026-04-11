package controller;

import java.sql.SQLException;
import java.util.List;

import model.Book;
import service.BookService;

public class BookController {
    private BookService bookService;

    public BookController() throws SQLException {
        this.bookService = new BookService();
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookService.getAllBooks();
    }

    public Book getBook(int bookId) throws SQLException {
        return bookService.getBook(bookId);
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookService.searchBooks(keyword);
    }

    public List<Book> getBooksByCategory(int categoryId) throws SQLException {
        return bookService.getBooksByCategory(categoryId);
    }

    public boolean isBookAvailable(int bookId, int quantity) throws SQLException {
        return bookService.isBookAvailable(bookId, quantity);
    }

    public int getAvailableQuantity(int bookId) throws SQLException {
        return bookService.getAvailableQuantity(bookId);
    }

    public int addBook(Book book) throws SQLException {
        return bookService.addBook(book);
    }

    public void updateBook(Book book) throws SQLException {
        bookService.updateBook(book);
    }

    public void deleteBook(int bookId) throws SQLException {
        bookService.deleteBook(bookId);
    }
}
