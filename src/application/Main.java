package application;

import javafx.application.Application;
import javafx.stage.Stage;
import view.LoginView;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Load global CSS - hacky way to apply to all scenes if we controlled them all, 
        // but for now we just start the login view. 
        // Best practice is to add to the scene when creating it in each view.
        // However, LoginView creates its own Scene. 
        // We will update LoginView to add the stylesheet.
        
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}