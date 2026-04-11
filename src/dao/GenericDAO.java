package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class GenericDAO<T> {
    protected Connection connection;

    public GenericDAO() throws SQLException {
        this.connection = util.DatabaseConnection.getConnection();
    }

    public GenericDAO(Connection connection) throws SQLException {
        this.connection = connection != null ? connection : util.DatabaseConnection.getConnection();
    }

    protected synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = util.DatabaseConnection.getConnection();
        }
        return connection;
    }

    public abstract int create(T entity) throws SQLException;

    public abstract T read(int id) throws SQLException;

    public abstract List<T> readAll() throws SQLException;

    public abstract void update(T entity) throws SQLException;

    public abstract void delete(int id) throws SQLException;

    protected void closeConnection() {
        util.DatabaseConnection.closeConnection(connection);
        connection = null;
    }
}
