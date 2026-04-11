package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import model.Book;

public class BookDAO extends GenericDAO<Book> {

    public BookDAO() throws SQLException {
        super();
    }

    public BookDAO(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public int create(Book book) throws SQLException {
        String sql = "INSERT INTO book (title, author_id, category_id, publisher_id, publication_date, isbn, description, price, image_url, stock_level) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getBookName());
            setNullableInt(stmt, 2, book.getAuthorId());
            setNullableInt(stmt, 3, book.getCategoryId());
            setNullableInt(stmt, 4, book.getPublisherId());
            stmt.setDate(5, book.getPublicationDate());
            stmt.setString(6, book.getIsbn());
            stmt.setString(7, book.getDescription());
            stmt.setDouble(8, book.getPrice());
            stmt.setString(9, book.getCoverImage());
            stmt.setInt(10, Math.max(0, book.getStockLevel()));

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
    public Book read(int bookId) throws SQLException {
        String sql = "SELECT * FROM book WHERE book_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Book> readAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    public List<Book> searchByName(String bookName) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book WHERE title LIKE ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + bookName + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    public List<Book> getBooksByCategory(int categoryId) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM book WHERE category_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }

    @Override
    public void update(Book book) throws SQLException {
        String sql = "UPDATE book SET title = ?, author_id = ?, category_id = ?, publisher_id = ?, publication_date = ?, isbn = ?, description = ?, price = ?, image_url = ?, stock_level = ? WHERE book_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, book.getBookName());
            setNullableInt(stmt, 2, book.getAuthorId());
            setNullableInt(stmt, 3, book.getCategoryId());
            setNullableInt(stmt, 4, book.getPublisherId());
            stmt.setDate(5, book.getPublicationDate());
            stmt.setString(6, book.getIsbn());
            stmt.setString(7, book.getDescription());
            stmt.setDouble(8, book.getPrice());
            stmt.setString(9, book.getCoverImage());
            stmt.setInt(10, Math.max(0, book.getStockLevel()));
            stmt.setInt(11, book.getBookId());
            stmt.executeUpdate();
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, int value) throws SQLException {
        if (value > 0) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    @Override
    public void delete(int bookId) throws SQLException {
        String sql = "DELETE FROM book WHERE book_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setBookName(rs.getString("title"));
        book.setAuthorId(rs.getInt("author_id"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setPublisherId(rs.getInt("publisher_id"));
        book.setPublicationDate(rs.getDate("publication_date"));
        book.setIsbn(rs.getString("isbn"));
        book.setDescription(rs.getString("description"));
        book.setPrice(rs.getDouble("price"));
        book.setCoverImage(rs.getString("image_url"));
        book.setStockLevel(rs.getInt("stock_level"));
        return book;
    }
}

