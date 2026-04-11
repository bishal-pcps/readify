package service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import dao.UserDAO;
import model.Administrator;
import model.User;

public class AdminService {
    private UserDAO userDAO;

    public AdminService() throws SQLException {
        this.userDAO = new UserDAO();
    }

    public Administrator loginAdmin(String email, String password) throws SQLException {
        User user = userDAO.readByEmail(email);
        if ((user == null) || !verifyPassword(password, user.getPasswordHash()) || (user.getRoleId() != 1)) {
			return null; // not an admin
		}

        if (user instanceof Administrator) {
			return (Administrator) user;
		}

        Administrator admin = new Administrator();
        admin.setUserId(user.getUserId());
        admin.setFirstName(user.getFirstName());
        admin.setLastName(user.getLastName());
        admin.setEmail(user.getEmail());
        admin.setPhoneNumber(user.getPhoneNumber());
        admin.setRoleId(user.getRoleId());
        admin.setAdminName(user.getFirstName() + " " + user.getLastName());
        return admin;
    }

    public int createAdmin(String firstName, String lastName, String email, String password) throws SQLException {
        User existingUser = userDAO.readByEmail(email);
        if (existingUser != null) {
            throw new SQLException("Email already exists");
        }

        Administrator admin = new Administrator();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(email);
        admin.setPasswordHash(hashPassword(password));
        admin.setRoleId(1); // 1 = Admin role
        admin.setAdminName(firstName + " " + lastName);

        return userDAO.create(admin);
    }

    public Administrator getAdmin(int adminId) throws SQLException {
        User user = userDAO.read(adminId);
        if (user instanceof Administrator) {
            return (Administrator) user;
        }
        return null;
    }

    public void updateAdminPassword(int adminId, String newPassword) throws SQLException {
        User admin = userDAO.read(adminId);
        if (admin != null && admin.getRoleId() == 1) {
            admin.setPasswordHash(hashPassword(newPassword));
            userDAO.update(admin);
        }
    }

    public void updateAdminProfile(int adminId, String firstName, String lastName,
                                   String phone) throws SQLException {
        User user = userDAO.read(adminId);
        if (user != null && user.getRoleId() == 1) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phone);
            if (user instanceof Administrator) {
                ((Administrator) user).setAdminName(firstName + " " + lastName);
            }
            userDAO.update(user);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private boolean verifyPassword(String password, String storedValue) {
        if (storedValue == null) {
			return false;
		}
        // Support plain-text passwords stored in DB (legacy data)
        if (storedValue.equals(password)) {
			return true;
		}
        // Also support SHA-256 hashed passwords (newly registered users)
        return hashPassword(password).equals(storedValue);
    }
}
