package controller;

import java.sql.SQLException;
import java.util.List;

import model.LoyaltyActivity;
import service.CustomerService;

public class CustomerController {
    private CustomerService customerService;

    public CustomerController() throws SQLException {
        this.customerService = new CustomerService();
    }

    public int getLoyaltyPoints(int userId) throws SQLException {
        return customerService.getLoyaltyPoints(userId);
    }

    public void redeemPoints(int userId, int points, String rewardName) throws SQLException {
        customerService.redeemPoints(userId, points, rewardName);
    }

    public List<LoyaltyActivity> getLoyaltyHistory(int userId) throws SQLException {
        return customerService.getLoyaltyHistory(userId);
    }
}
