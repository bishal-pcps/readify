package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Publisher;

public class PublisherDAO extends GenericDAO<Publisher> {

    public PublisherDAO() throws SQLException {
        super();
    }

    @Override
    public int create(Publisher publisher) throws SQLException {
        String sql = "INSERT INTO publisher (publisher_name, address, city, state, zip_code, email, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, publisher.getPublisherName());
            stmt.setString(2, publisher.getAddress());
            stmt.setString(3, publisher.getCity());
            stmt.setString(4, publisher.getState());
            stmt.setString(5, publisher.getZipCode());
            stmt.setString(6, publisher.getEmail());
            stmt.setString(7, publisher.getPhoneNumber());

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
    public Publisher read(int publisherId) throws SQLException {
        String sql = "SELECT * FROM publisher WHERE publisher_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, publisherId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPublisher(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Publisher> readAll() throws SQLException {
        List<Publisher> publishers = new ArrayList<>();
        String sql = "SELECT * FROM publisher";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                publishers.add(mapResultSetToPublisher(rs));
            }
        }
        return publishers;
    }

    @Override
    public void update(Publisher publisher) throws SQLException {
        String sql = "UPDATE publisher SET publisher_name = ?, address = ?, city = ?, state = ?, zip_code = ?, email = ?, phone_number = ? WHERE publisher_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, publisher.getPublisherName());
            stmt.setString(2, publisher.getAddress());
            stmt.setString(3, publisher.getCity());
            stmt.setString(4, publisher.getState());
            stmt.setString(5, publisher.getZipCode());
            stmt.setString(6, publisher.getEmail());
            stmt.setString(7, publisher.getPhoneNumber());
            stmt.setInt(8, publisher.getPublisherId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int publisherId) throws SQLException {
        String sql = "DELETE FROM publisher WHERE publisher_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, publisherId);
            stmt.executeUpdate();
        }
    }

    private Publisher mapResultSetToPublisher(ResultSet rs) throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setPublisherId(rs.getInt("publisher_id"));
        publisher.setPublisherName(rs.getString("publisher_name"));
        publisher.setAddress(rs.getString("address"));
        publisher.setCity(rs.getString("city"));
        publisher.setState(rs.getString("state"));
        publisher.setZipCode(rs.getString("zip_code"));
        publisher.setEmail(rs.getString("email"));
        publisher.setPhoneNumber(rs.getString("phone_number"));
        return publisher;
    }
}

