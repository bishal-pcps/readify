package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/readify_db";
    private static final String USER = "root";
    private static final String PASSWORD = "bishal";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver not found!");
            e.printStackTrace();
        }
    }

    private static Connection singleConnection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (singleConnection == null || singleConnection.isClosed()) {
            singleConnection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return singleConnection;
    }

    public static void closeConnection(Connection conn) {
        // App now uses a single persistent connection, so we do not close it here.
    }
}
