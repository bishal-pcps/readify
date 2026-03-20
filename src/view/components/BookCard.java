package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import model.Book;
import controller.CartController;

/**
 * JavaFX component representing a book display card.
 * Shows book information and allows adding to cart.
 */
public class BookCard extends VBox {
    
    /**
     * Constructs a BookCard with book details and add-to-cart functionality.
     * @param book The book to display
     * @param cartController The controller to handle cart operations
     */
    public BookCard(Book book, CartController cartController) {
        if (book == null || cartController == null) {
            throw new IllegalArgumentException("Book and CartController cannot be null");
        }
        
        getStyleClass().add("book-card");
        setPrefWidth(280);
        setPrefHeight(400);
        
        // 1. Image Area (Top Half)
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.setStyle("-fx-background-color: #eee; -fx-background-radius: 10 10 0 0;");
        
        // Placeholder for book cover (since we don't have actual files loaded)
        // In a real app, this would be new Image(book.getImageUrl())
        Label placeholderIcon = new Label("\uD83D\uDCD6"); // Book emoji
        placeholderIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: #ccc;");
        imageContainer.getChildren().add(placeholderIcon);
        
        // Clip to rounded corners
        Rectangle clip = new Rectangle(280, 200);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageContainer.setClip(clip);

        // 2. Content Area (Bottom Half)
        VBox contentBox = new VBox(8);
        contentBox.setPadding(new Insets(15));
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        
        // Category Tag
        Label categoryLabel = new Label(book.getCategory());
        categoryLabel.getStyleClass().add("category-tag");
        
        // Title
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setStyle("-fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #4a3b32;");
        titleLabel.setWrapText(true);
        titleLabel.setMinHeight(40); // ensure 2 lines space
        
        // Author
        Label authorLabel = new Label("by " + book.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        // Rating
        Label ratingLabel = new Label("★ " + String.format("%.1f", book.getRating()) + 
                                      " (" + book.getReviewCount() + " reviews)");
        ratingLabel.getStyleClass().add("rating-label");
        
        // Price and Button Row
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(10, 0, 0, 0));
        
        Label priceLabel = new Label("$" + book.getPrice().setScale(2, java.math.RoundingMode.HALF_UP));
        priceLabel.getStyleClass().add("price-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addToCartBtn = new Button("Add to Cart");
        addToCartBtn.getStyleClass().addAll("button", "secondary-button");
        addToCartBtn.setDisable(!book.isInStock());
        if (!book.isInStock()) {
            addToCartBtn.setText("Out of Stock");
            addToCartBtn.setStyle("-fx-background-color: #ccc;");
        }
        
        addToCartBtn.setOnAction(e -> {
            try {
                cartController.addToCart(book, 1);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Cart Updated");
                alert.setHeaderText(null);
                alert.setContentText(book.getTitle() + " has been added to your cart!");
                alert.showAndWait();
            } catch (Exception ex) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setContentText("Failed to add item to cart: " + ex.getMessage());
                errorAlert.showAndWait();
            }
        });
        
        actionRow.getChildren().addAll(priceLabel, spacer, addToCartBtn);
        
        contentBox.getChildren().addAll(categoryLabel, titleLabel, authorLabel, ratingLabel, new Region(), actionRow);
        
        getChildren().addAll(imageContainer, contentBox);
    }
}