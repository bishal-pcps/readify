package view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import controller.AuthController;

public class LoginView {
    private AuthController authController;
    
    public LoginView() {
        this.authController = new AuthController();
    }
    
    public void start(Stage stage) {
        stage.setTitle("Readify - Sign In");
        
        // Main container
        StackPane root = new StackPane();
        root.getStyleClass().add("root");
        
        // Background decorative circles
        Circle c1 = new Circle(150);
        c1.setFill(javafx.scene.paint.Color.web("#f2e8cf"));
        c1.setTranslateX(-400);
        c1.setTranslateY(-200);
        
        Circle c2 = new Circle(250);
        c2.setFill(javafx.scene.paint.Color.web("#f5f0e6"));
        c2.setTranslateX(400);
        c2.setTranslateY(200);
        
        root.getChildren().addAll(c1, c2);
        
        // Content Container
        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.CENTER);
        
        // Logo Section
        VBox logoBox = new VBox(10);
        logoBox.setAlignment(Pos.CENTER);
        
        Label iconLabel = new Label("\uD83D\uDCD6"); // Book Emoji
        iconLabel.setStyle("-fx-font-size: 40px; -fx-text-fill: #8d4034; -fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 5px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        Label titleLabel = new Label("Welcome to Readify");
        titleLabel.getStyleClass().add("h1");
        
        Label subtitleLabel = new Label("Sign in to continue your literary journey");
        subtitleLabel.getStyleClass().add("subtitle");
        
        logoBox.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        
        // Login Card
        VBox card = new VBox(20);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(400);
        
        // Form
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);
        formGrid.setHgap(10);
        formGrid.setAlignment(Pos.CENTER);
        
        // Email
        Label emailLabel = new Label("Email Address");
        emailLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextField emailField = new TextField();
        emailField.setPromptText("you@example.com");
        emailField.getStyleClass().add("text-field");
        
        // Password
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("text-field");
        
        // Options
        HBox optionsBox = new HBox();
        CheckBox rememberMeCheck = new CheckBox("Remember me");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setStyle("-fx-text-fill: #8d4034;");
        optionsBox.getChildren().addAll(rememberMeCheck, spacer, forgotPasswordLink);
        
        // Sign In Button
        Button signInButton = new Button("Sign In");
        signInButton.getStyleClass().add("button");
        signInButton.getStyleClass().add("primary-button");
        signInButton.setMaxWidth(Double.MAX_VALUE);
        signInButton.setPrefHeight(45);
        
        // Social Login
        Label orLabel = new Label("Or continue with");
        orLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");
        
        HBox socialBox = new HBox(15);
        socialBox.setAlignment(Pos.CENTER);
        
        Button googleButton = new Button("G  Google");
        googleButton.getStyleClass().addAll("button", "social-button");
        googleButton.setPrefWidth(150);
        
        Button githubButton = new Button("GitHub"); // Github icon omitted for simplicity
        githubButton.getStyleClass().addAll("button", "social-button");
        githubButton.setPrefWidth(150);
        
        socialBox.getChildren().addAll(googleButton, githubButton);
        
        // Add to card
        card.getChildren().addAll(
            emailLabel, emailField, 
            passwordLabel, passwordField, 
            optionsBox, 
            signInButton,
            new Separator(),
            orLabel,
            socialBox
        );
        
        contentBox.getChildren().addAll(logoBox, card);
        root.getChildren().add(contentBox);
        
        // Action
        signInButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            
            boolean success = authController.login(email, password, stage);
            
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText(null);
                alert.setContentText("Invalid email or password");
                alert.showAndWait();
            }
        });
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}