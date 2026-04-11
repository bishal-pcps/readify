package service;

import java.sql.SQLException;
import java.util.List;

import dao.CustomerDAO;
import dao.LoyaltyDAO;
import dao.UserDAO;
import model.LoyaltyActivity;
import model.User;

public class CustomerService {
    private UserDAO userDAO;
    private LoyaltyDAO loyaltyDAO;
    private CustomerDAO customerDAO;

    public CustomerService() throws SQLException {
        this.userDAO = new UserDAO();
        this.loyaltyDAO = new LoyaltyDAO();
        this.customerDAO = new CustomerDAO();
    }

    public User validateLogin(String email, String password) throws SQLException {
        User user = userDAO.readByEmail(email);
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }

    public void registerCustomer(User user, String password) throws SQLException {
        user.setPasswordHash(password);
        user.setRoleId(2); // 2 = Customer
        int userId = userDAO.create(user);
        user.setUserId(userId);
        customerDAO.ensureCustomerProfile(userId, user.getAddressLine());
    }

    public int getLoyaltyPoints(int userId) throws SQLException {
        User user = userDAO.read(userId);
        return (user != null) ? user.getLoyaltyPoints() : 0;
    }

    public boolean redeemPoints(int userId, int pointsToRedeem, String rewardName) throws SQLException {
        User user = userDAO.read(userId);
        if (user != null && user.getLoyaltyPoints() >= pointsToRedeem) {
            user.setLoyaltyPoints(user.getLoyaltyPoints() - pointsToRedeem);
            userDAO.update(user);

            // Log redemption
            LoyaltyActivity activity = new LoyaltyActivity(userId, -pointsToRedeem, "Redeemed: " + rewardName);
            loyaltyDAO.create(activity);
            return true;
        }
        return false;
    }

    public List<LoyaltyActivity> getLoyaltyHistory(int userId) throws SQLException {
        return loyaltyDAO.readByUserId(userId);
    }
}
