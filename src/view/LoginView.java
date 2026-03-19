package view;

import controller.AuthController;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    private AuthController authController;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheck;
    private JButton signInButton;
    private JButton googleButton;
    private JButton githubButton;
    
    public LoginView() {
        this.authController = new AuthController();
        authController.setLoginView(this);
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Readify - Sign In");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome to Readify");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue your literary journey");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 12));
        emailField = new JTextField(20);
        emailField.setText("you@example.com");
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passwordField = new JPasswordField(20);
        passwordField.setText("password");
        
        // Remember me and forgot password
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rememberMeCheck = new JCheckBox("Remember me");
        JLabel forgotPassword = new JLabel("Forgot password?");
        forgotPassword.setForeground(Color.BLUE);
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionsPanel.add(rememberMeCheck);
        optionsPanel.add(forgotPassword);
        
        // Sign In button
        signInButton = new JButton("Sign In");
        signInButton.setBackground(new Color(70, 130, 180));
        signInButton.setForeground(Color.WHITE);
        signInButton.setFont(new Font("Arial", Font.BOLD, 14));
        signInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Social login
        JLabel orLabel = new JLabel("Or continue with");
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JPanel socialPanel = new JPanel(new FlowLayout());
        googleButton = new JButton("G Google");
        githubButton = new JButton("GitHub");
        socialPanel.add(googleButton);
        socialPanel.add(githubButton);
        
        // Add action listeners
        signInButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            authController.login(email, password);
        });
        
        // Add components
        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(emailLabel);
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(passwordLabel);
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(optionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(signInButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(orLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(socialPanel);
        
        add(mainPanel);
    }
    
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}