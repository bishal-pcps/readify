package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.math.BigDecimal;

import model.Book;
import controller.BookController;
import controller.CartController;
import controller.AuthController;
import view.components.BookCard;

public class BrowseBooksView {
    private BookController bookController;
    private CartController cartController;
    private AuthController authController;
    private FlowPane booksGrid;
    
    public BrowseBooksView() {
        this.bookController = new BookController();
        this.cartController = new CartController();
    }
    
    public void start(Stage stage, AuthController authController) {
        this.authController = authController;
        
        stage.setTitle("Readify - Browse Books");
        
        // Main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Top navigation bar
        HBox navBar = createNavigationBar(stage);
        root.setTop(navBar);
        
        // Center content with sidebar and book grid
        // Using BorderPane for content to allow sidebar on left
        BorderPane contentPane = new BorderPane();
        contentPane.setPadding(new Insets(20));
        
        // Left sidebar with filters
        VBox sidebar = createSidebar();
        contentPane.setLeft(sidebar);
        
        // Right content area (Search + Grid)
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(0, 0, 0, 20));
        
        // Search bar
        HBox searchBar = createSearchBar();
        mainContent.getChildren().add(searchBar);
        
        // Books grid
        booksGrid = new FlowPane();
        booksGrid.setHgap(20);
        booksGrid.setVgap(20);
        booksGrid.setPrefWrapLength(900);
        
        refreshBooksGrid(bookController.getAllBooks());
        
        ScrollPane scrollPane = new ScrollPane(booksGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        mainContent.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        contentPane.setCenter(mainContent);
        
        root.setCenter(contentPane);
        
        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createNavigationBar(Stage stage) {
        HBox navBar = new HBox(30);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        // Logo
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("\uD83D\uDCD6"); // Book Emoji
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #8d4034; -fx-background-color: #fcfbf7; -fx-background-radius: 5px; -fx-padding: 2px 5px;");
        Label logoText = new Label("Readify");
        logoText.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #4a3b32;");
        logoBox.getChildren().addAll(iconLabel, logoText);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Navigation Links
        HBox navLinks = new HBox(15);
        navLinks.setAlignment(Pos.CENTER_RIGHT);
        
        Button browseBtn = new Button("Browse");
        browseBtn.getStyleClass().addAll("button", "pill-button"); // Active state style
        
        Button ordersBtn = new Button("Orders");
        ordersBtn.getStyleClass().addAll("button", "nav-button");
        
        Button loyaltyBtn = new Button("Loyalty");
        loyaltyBtn.getStyleClass().addAll("button", "nav-button");
        
        Button cartBtn = new Button("Cart (" + cartController.getItemCount() + ")");
        cartBtn.getStyleClass().addAll("button", "secondary-button");
        
        navLinks.getChildren().addAll(browseBtn, ordersBtn, loyaltyBtn, cartBtn);
        
        if (authController != null && authController.isAdmin()) {
            Button adminBtn = new Button("Admin");
            adminBtn.getStyleClass().addAll("button", "nav-button");
            navLinks.getChildren().add(adminBtn);
        }
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "nav-button"); // Changed to nav-button style for cleaner look
        logoutBtn.setOnAction(e -> {
            if (authController != null) authController.logout();
            new LoginView().start(stage);
        });
        navLinks.getChildren().add(logoutBtn);
        
        navBar.getChildren().addAll(logoBox, spacer, navLinks);
        
        return navBar;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20, 10, 20, 0));
        sidebar.setPrefWidth(200);
        
        Label filtersLabel = new Label("Categories");
        filtersLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #4a3b32;");
        
        String[] categories = {
            "All Books",
            "Classic Literature",
            "Contemporary Fiction",
            "Mystery & Thriller",
            "Cooking & Food",
            "Science & Education",
            "Fantasy & Sci-Fi"
        };
        
        sidebar.getChildren().add(filtersLabel);
        
        ToggleGroup categoryGroup = new ToggleGroup();
        
        for (String category : categories) {
            RadioButton radioBtn = new RadioButton(category);
            radioBtn.setToggleGroup(categoryGroup);
            radioBtn.getStyleClass().add("radio-button");
            radioBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: #4a3b32;");
            
            if (category.equals("All Books")) {
                radioBtn.setSelected(true);
                radioBtn.setOnAction(e -> refreshBooksGrid(bookController.getAllBooks()));
            } else {
                radioBtn.setOnAction(e -> filterByCategory(category));
            }
            
            sidebar.getChildren().add(radioBtn);
        }
        
        return sidebar;
    }
    
    private HBox createSearchBar() {
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search by title, author, or ISBN...");
        searchField.setPrefWidth(400);
        searchField.getStyleClass().add("text-field");
        
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().addAll("button", "primary-button");
        searchButton.setOnAction(e -> {
            List<Book> results = bookController.searchBooks(searchField.getText());
            refreshBooksGrid(results);
        });
        
        searchBar.getChildren().addAll(searchField, searchButton);
        
        return searchBar;
    }
    
    private void filterByCategory(String category) {
        List<Book> filteredBooks = bookController.getBooksByCategory(category);
        refreshBooksGrid(filteredBooks);
    }
    
    private void refreshBooksGrid(List<Book> books) {
        if (booksGrid != null) {
            booksGrid.getChildren().clear();
            
            if (books.isEmpty()) {
                // If DB is empty, provide some sample data for UI demonstration
                books.add(new Book(1, "The Great Classic", "Jane Austen", new BigDecimal("24.99"), 
                    "Classic Literature", 4.8, 1250));
                books.add(new Book(2, "Modern Tales", "Sarah Johnson", new BigDecimal("19.99"), 
                    "Contemporary Fiction", 4.6, 892));
                books.add(new Book(3, "Mystery in the Shadows", "Robert Blake", new BigDecimal("22.99"), 
                    "Mystery & Thriller", 4.7, 1045));
                books.add(new Book(4, "Culinary Delights", "Chef Mario", new BigDecimal("34.50"), 
                    "Cooking & Food", 4.9, 530));
                books.add(new Book(5, "Cosmos", "Carl Sagan", new BigDecimal("18.00"), 
                    "Science & Education", 4.9, 2100));
            }
            
            for (Book book : books) {
                booksGrid.getChildren().add(new BookCard(book, cartController));
            }
        }
    }
}