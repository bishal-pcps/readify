package dao;

import java.sql.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.logging.Level;

import model.User;
import model.Administrator;
import model.Customer;
import model.Role;

/**
 * Data Access Object for User entities.
 * Handles all user-related database operations including authentication and registration.
 */
public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    
    // Password hashing constants
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;
    
    /**
     * Authenticate a user with email and password.
     * Validates password against stored hash.
     * @param email The user's email
     * @param password The plaintext password to verify
     * @return The authenticated User object, or null if authentication fails
     */
    public User authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            logger.log(Level.WARNING, "Invalid authentication attempt: empty email or password");
            return null;
        }
        
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                logger.log(Level.SEVERE, "Failed to establish database connection for authentication");
                return null;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        
                        // Verify password hash
                        if (verifyPassword(password, storedHash)) {
                            String role = rs.getString("role");
                            logger.log(Level.INFO, "User authenticated successfully: " + email);
                            
                            if ("ADMIN".equals(role)) {
                                return extractAdminFromResultSet(rs);
                            } else {
                                return extractCustomerFromResultSet(rs);
                            }
                        } else {
                            logger.log(Level.WARNING, "Authentication failed: invalid password for " + email);
                        }
                    } else {
                        logger.log(Level.WARNING, "Authentication failed: user not found " + email);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during authentication: " + e.getMessage());
        }
        
        return null;
    }
    
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String role = rs.getString("role");
                if ("ADMIN".equals(role)) {
                    return extractAdminFromResultSet(rs);
                } else {
                    return extractCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Register a new customer with hashed password.
     * @param customer The customer to register (without password)
     * @param password The plaintext password to hash and store
     * @return true if registration succeeds, false otherwise
     */
    public boolean registerCustomer(Customer customer, String password) {
        if (customer == null || customer.getEmail() == null || password == null || password.isEmpty()) {
            logger.log(Level.WARNING, "Invalid customer data for registration");
            return false;
        }
        
        // Hash the password before storing
        String passwordHash = hashPassword(password);
        
        String sql = "INSERT INTO users (email, password, first_name, last_name, phone, role, " +
                     "loyalty_points, default_address) VALUES (?, ?, ?, ?, ?, 'CUSTOMER', ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                logger.log(Level.SEVERE, "Failed to establish database connection for registration");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, customer.getEmail());
                pstmt.setString(2, passwordHash);
                pstmt.setString(3, customer.getFirstName());
                pstmt.setString(4, customer.getLastName());
                pstmt.setString(5, customer.getPhone());
                pstmt.setInt(6, customer.getLoyaltyPoints());
                pstmt.setString(7, customer.getDefaultShippingAddress());
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            customer.setUserId(generatedKeys.getInt(1));
                            logger.log(Level.INFO, "Customer registered successfully: " + customer.getEmail());
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during registration: " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean updateLoyaltyPoints(int customerId, int points) {
        String sql = "UPDATE users SET loyalty_points = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, points);
            pstmt.setInt(2, customerId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Hash a password using PBKDF2 (Java built-in).
     * @param password The plaintext password
     * @return The hashed password with embedded salt
     */
    private static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            byte[] hash = hashPasswordWithSalt(password.toCharArray(), salt);
            
            String saltString = Base64.getEncoder().encodeToString(salt);
            String hashString = Base64.getEncoder().encodeToString(hash);
            
            return saltString + ":" + hashString;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error hashing password: " + e.getMessage());
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    
    /**
     * Verify a password against a stored hash.
     * @param password The plaintext password to verify
     * @param storedHash The stored hash (salt:hash format)
     * @return true if password matches, false otherwise
     */
    private static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            
            byte[] providedHash = hashPasswordWithSalt(password.toCharArray(), salt);
            
            return constantTimeCompare(providedHash, storedHashBytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error verifying password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Hash password with given salt using PBKDF2.
     */
    private static byte[] hashPasswordWithSalt(char[] password, byte[] salt) throws Exception {
        javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(
            password, salt, ITERATIONS, KEY_LENGTH);
        javax.crypto.SecretKeyFactory skf = 
            javax.crypto.SecretKeyFactory.getInstance(HASH_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }
    
    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private static boolean constantTimeCompare(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setUserId(rs.getInt("user_id"));
        customer.setEmail(rs.getString("email"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setLoyaltyPoints(rs.getInt("loyalty_points"));
        customer.setDefaultShippingAddress(rs.getString("default_address"));
        return customer;
    }
    
    private Administrator extractAdminFromResultSet(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setUserId(rs.getInt("user_id"));
        admin.setEmail(rs.getString("email"));
        admin.setFirstName(rs.getString("first_name"));
        admin.setLastName(rs.getString("last_name"));
        admin.setPhone(rs.getString("phone"));
        admin.setAdminLevel(rs.getString("admin_level"));
        admin.setDepartment(rs.getString("department"));
        return admin;
    }
    
    public java.util.List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        java.util.List<User> users = new java.util.ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String role = rs.getString("role");
                if ("ADMIN".equals(role)) {
                    users.add(extractAdminFromResultSet(rs));
                } else {
                    users.add(extractCustomerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error retrieving all users: " + e.getMessage());
        }
        
        return users;
    }
}
