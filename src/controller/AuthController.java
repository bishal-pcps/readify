package controller;

import model.User;
import model.Role;
import dao.UserDAO;
import javafx.stage.Stage;

public class AuthController {
    private UserDAO userDAO;
    private User currentUser;
    
    public AuthController() {
        this.userDAO = new UserDAO();
    }
    
    public boolean login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        
        User user = userDAO.authenticate(email, password);
        
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public boolean login(String email, String password, javafx.stage.Stage stage) {
        if (login(email, password)) {
            view.BrowseBooksView browseView = new view.BrowseBooksView();
            browseView.start(stage, this);
            return true;
        }
        return false;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void logout(Stage stage) {
        this.currentUser = null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
}
