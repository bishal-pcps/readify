package controller;

import java.sql.SQLException;

import model.User;
import service.CustomerService;

public class AuthController {
    private CustomerService customerService;
    private static User currentUser;

    public AuthController() throws SQLException {
        this.customerService = new CustomerService();
    }

    public boolean customerLogin(String email, String password) throws SQLException {
        User user = customerService.validateLogin(email, password);
        if (user != null && user.getRoleId() == 2) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean adminLogin(String email, String password) throws SQLException {
        User user = customerService.validateLogin(email, password);
        if (user != null && user.getRoleId() == 1) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean login(String email, String password) throws SQLException {
        User user = customerService.validateLogin(email, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean customerRegister(String firstName, String lastName, String email, String phone,
                                    String address, String city, String state, String zipCode,
                                    String country, String password) throws SQLException {
        try {
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPhoneNumber(phone);
            newUser.setAddressLine(address);
            newUser.setCity(city);
            newUser.setState(state);
            newUser.setZipCode(zipCode);
            newUser.setCountry(country);
            newUser.setRoleId(2); // 2 = Customer

            customerService.registerCustomer(newUser, password);
            currentUser = newUser;
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void logout() {
        currentUser = null;
    }

    public static User getCurrentCustomer() {
        return currentUser; // Many views assume this returns the current logged in user
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static User getCurrentAdmin() {
        return currentUser;
    }

    public static boolean isCustomerLoggedIn() {
        return currentUser != null && currentUser.getRoleId() == 2;
    }

    public static boolean isAdminLoggedIn() {
        return currentUser != null && currentUser.getRoleId() == 1; // 1 = Admin
    }
}
