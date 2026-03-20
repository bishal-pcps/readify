package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.CartItem;

import java.util.List;

import controller.AuthController;
import controller.CartController;

public class CartView {
    private CartController cartController;
    private AuthController authController;
    
    public void start(Stage stage, AuthController authController, CartController cartController) {
        this.authController = authController;
        this.cartController = cartController;
        
        stage.setTitle("Readify - Shopping Cart");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Top navigation
        HBox navBar = createNavigationBar(stage);
        root.setTop(navBar);
        
        // Center content
        VBox content = new VBox(25);
        content.setPadding(new Insets(30, 100, 30, 100)); // Large horizontal padding for "centered" look
        
        Label title = new Label("Your Shopping Cart");
        title.getStyleClass().add("h1");
        
        // Cart Container
        VBox cartContainer = new VBox(20);
        cartContainer.getStyleClass().add("cart-card");
        
        // Header Row
        HBox headerRow = new HBox(20);
        headerRow.setStyle("-fx-border-color: transparent transparent #ddd transparent; -fx-padding: 0 0 10 0;");
        Label itemHeader = new Label("Product");
        Label quantityHeader = new Label("Quantity");
        Label subtotalHeader = new Label("Subtotal");
        itemHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #999;");
        quantityHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #999;");
        subtotalHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #999;");
        
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        headerRow.getChildren().addAll(itemHeader, hSpacer, quantityHeader, new Region() {{ setPrefWidth(50); }}, subtotalHeader, new Region() {{ setPrefWidth(100); }});
        
        // Items
        VBox itemsBox = createCartItems();
        
        cartContainer.getChildren().addAll(headerRow, itemsBox);
        
        // Summary
        VBox summary = createOrderSummary(stage);
        
        content.getChildren().addAll(title, cartContainer, summary);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);
        
        Scene scene = new Scene(root, 1100, 700);
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
        Label iconLabel = new Label("\uD83D\uDCD6");
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #8d4034; -fx-background-color: #fcfbf7; -fx-background-radius: 5px; -fx-padding: 2px 5px;");
        Label logoText = new Label("Readify");
        logoText.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 24px; -fx-text-fill: #4a3b32;");
        logoBox.getChildren().addAll(iconLabel, logoText);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox navLinks = new HBox(15);
        navLinks.setAlignment(Pos.CENTER_RIGHT);
        
        Button browseBtn = new Button("Browse");
        browseBtn.getStyleClass().addAll("button", "nav-button");
        browseBtn.setOnAction(e -> new BrowseBooksView().start(stage, authController));
        
        Button cartBtn = new Button("Cart (" + cartController.getItemCount() + ")");
        cartBtn.getStyleClass().addAll("button", "pill-button");
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "nav-button");
        logoutBtn.setOnAction(e -> {
            authController.logout();
            new LoginView().start(stage);
        });
        
        navLinks.getChildren().addAll(browseBtn, cartBtn, logoutBtn);
        navBar.getChildren().addAll(logoBox, spacer, navLinks);
        
        return navBar;
    }
    
    private VBox createCartItems() {
        VBox itemsBox = new VBox();
        List<CartItem> items = cartController.getCartItems();
        
        if (items.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.getStyleClass().add("subtitle");
            emptyLabel.setStyle("-fx-font-size: 18px;");
            emptyBox.getChildren().add(emptyLabel);
            return emptyBox;
        } else {
            for (CartItem item : items) {
                itemsBox.getChildren().add(createCartItemRow(item));
            }
        }
        
        return itemsBox;
    }
    
    private HBox createCartItemRow(CartItem item) {
        HBox row = new HBox(20);
        row.getStyleClass().add("cart-item-row");
        row.setAlignment(Pos.CENTER_LEFT);
        
        VBox details = new VBox(5);
        Label title = new Label(item.getBook().getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label author = new Label("by " + item.getBook().getAuthor());
        author.getStyleClass().add("subtitle");
        details.getChildren().addAll(title, author);
        details.setPrefWidth(300);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Spinner<Integer> quantitySpinner = new Spinner<>(0, 99, item.getQuantity());
        quantitySpinner.getStyleClass().add("spinner");
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == 0) {
                cartController.removeFromCart(item.getBook());
            } else {
                cartController.updateQuantity(item.getBook(), newVal);
            }
            // In a real app, refresh the view here
        });
        
        Label price = new Label("$" + String.format("%.2f", item.getSubtotal()));
        price.getStyleClass().add("price-label");
        price.setPrefWidth(100);
        price.setAlignment(Pos.CENTER_RIGHT);
        
        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().addAll("button", "nav-button");
        removeBtn.setStyle("-fx-text-fill: #e74c3c;");
        removeBtn.setOnAction(e -> cartController.removeFromCart(item.getBook()));
        
        row.getChildren().addAll(details, spacer1, quantitySpinner, new Region() {{ setPrefWidth(30); }}, price, removeBtn);
        
        return row;
    }
    
    private VBox createOrderSummary(Stage stage) {
        VBox outerContainer = new VBox();
        outerContainer.setAlignment(Pos.CENTER_RIGHT);
        
        VBox summaryBox = new VBox(15);
        summaryBox.getStyleClass().add("cart-card");
        summaryBox.setMaxWidth(400);
        summaryBox.setAlignment(Pos.CENTER_RIGHT);
        
        HBox subtotalRow = new HBox();
        Label subLabel = new Label("Subtotal");
        subLabel.getStyleClass().add("summary-label");
        Label subValue = new Label("$" + String.format("%.2f", cartController.getTotal()));
        subValue.setStyle("-fx-font-weight: bold;");
        Region s1 = new Region(); HBox.setHgrow(s1, Priority.ALWAYS);
        subtotalRow.getChildren().addAll(subLabel, s1, subValue);
        
        HBox shippingRow = new HBox();
        Label shipLabel = new Label("Shipping");
        shipLabel.getStyleClass().add("summary-label");
        Label shipValue = new Label("$5.00");
        Region s2 = new Region(); HBox.setHgrow(s2, Priority.ALWAYS);
        shippingRow.getChildren().addAll(shipLabel, s2, shipValue);
        
        Separator sep = new Separator();
        
        HBox totalRow = new HBox();
        Label totalLabel = new Label("Total");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label totalValue = new Label("$" + String.format("%.2f", cartController.getTotal() + 5.0));
        totalValue.getStyleClass().add("total-label");
        Region s3 = new Region(); HBox.setHgrow(s3, Priority.ALWAYS);
        totalRow.getChildren().addAll(totalLabel, s3, totalValue);
        
        Button checkoutBtn = new Button("Proceed to Checkout");
        checkoutBtn.getStyleClass().addAll("button", "primary-button");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.setPrefHeight(50);
        
        summaryBox.getChildren().addAll(subtotalRow, shippingRow, sep, totalRow, checkoutBtn);
        outerContainer.getChildren().add(summaryBox);
        
		return outerContainer;
    }
}
