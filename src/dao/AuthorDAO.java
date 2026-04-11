package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Author;

public class AuthorDAO extends GenericDAO<Author> {

    public AuthorDAO() throws SQLException {
        super();
    }

    @Override
    public int create(Author author) throws SQLException {
        String sql = "INSERT INTO author (author_name, biography, email, phone_number, country) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, author.getAuthorName());
            stmt.setString(2, author.getBiography());
            stmt.setString(3, author.getEmail());
            stmt.setString(4, author.getPhoneNumber());
            stmt.setString(5, author.getCountry());

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
    public Author read(int authorId) throws SQLException {
        String sql = "SELECT * FROM author WHERE author_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuthor(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Author> readAll() throws SQLException {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT * FROM author";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                authors.add(mapResultSetToAuthor(rs));
            }
        }
        return authors;
    }

    @Override
    public void update(Author author) throws SQLException {
        String sql = "UPDATE author SET author_name = ?, biography = ?, email = ?, phone_number = ?, country = ? WHERE author_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, author.getAuthorName());
            stmt.setString(2, author.getBiography());
            stmt.setString(3, author.getEmail());
            stmt.setString(4, author.getPhoneNumber());
            stmt.setString(5, author.getCountry());
            stmt.setInt(6, author.getAuthorId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int authorId) throws SQLException {
        String sql = "DELETE FROM author WHERE author_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            stmt.executeUpdate();
        }
    }

    private Author mapResultSetToAuthor(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setAuthorId(rs.getInt("author_id"));
        author.setAuthorName(rs.getString("author_name"));
        author.setBiography(rs.getString("biography"));
        author.setEmail(rs.getString("email"));
        author.setPhoneNumber(rs.getString("phone_number"));
        author.setCountry(rs.getString("country"));
        return author;
    }
}

