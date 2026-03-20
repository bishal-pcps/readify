package controller;

import java.util.List;
import model.Book;
import dao.BookDAO;

public class BookController {
    private BookDAO bookDAO;
    
    public BookController() {
        this.bookDAO = new BookDAO();
    }
    
    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }
    
    public List<Book> getBooksByCategory(String category) {
        return bookDAO.getBooksByCategory(category);
    }
    
    public List<Book> searchBooks(String keyword) {
        return bookDAO.searchBooks(keyword);
    }
    
    public Book getBookById(int id) {
        return bookDAO.getBookById(id);
    }
    
    public boolean addBook(Book book) {
        return bookDAO.addBook(book);
    }
    
    public boolean updateBook(Book book) {
        return bookDAO.updateBook(book);
    }
    
    public boolean deleteBook(int bookId) {
        return bookDAO.deleteBook(bookId);
    }
    
    public boolean updateStock(int bookId, int newQuantity) {
        return bookDAO.updateStock(bookId, newQuantity);
    }
}
