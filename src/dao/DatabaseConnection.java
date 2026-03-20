package dao;

import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Database connection manager using core Java.
 * Handles MySQL connections with proper resource management.
 * Note: For production, consider using a connection pool.
 */
public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    
    // Database credentials - sourced from environment variables for security
    private static final String URL = "jdbc:mysql://localhost:3306/readify_db";
    private static final String USERNAME = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    
    static {
        try {
            Class.forName(DRIVER);
            logger.log(Level.INFO, "MySQL driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to load MySQL driver: " + e.getMessage());
        }
    }
    
    private DatabaseConnection() {
        // Utility class, not instantiable
    }
    
    /**
     * Get a new database connection.
     * Each call creates a fresh connection.
     * Caller must close the connection in a try-with-resources block.
     * @return A new Connection, or null if connection fails
     */
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            if (conn != null) {
                logger.log(Level.FINE, "Database connection established");
            }
            return conn;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create database connection: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Safely close a connection and log any errors.
     * @param conn The connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.log(Level.FINE, "Database connection closed");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing database connection: " + e.getMessage());
            }
        }
    }
}
