package view.components;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import controller.BookController;
import controller.CartController;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Book;

public class BrowseView {
    private static final Pattern SEARCH_ALLOWED_PATTERN = Pattern.compile("[a-zA-Z0-9\\s@._'\\-:,()&]*");

    private BookController bookController;
    private CartController cartController;
    private int currentUserId;
    private Runnable onCartClick;
    private Runnable onOrdersClick;
    private Runnable onLoyaltyClick;
    private Runnable onLogoutClick;

    public BrowseView(Stage stage, BookController bookController, CartController cartController,
            int userId, Runnable onCartClick) {
        this.bookController = bookController;
        this.cartController = cartController;
        this.currentUserId = userId;
        this.onCartClick = onCartClick;
        this.onOrdersClick = null;
        this.onLoyaltyClick = null;
        this.onLogoutClick = null;
    }

    public void setNavCallbacks(Runnable onOrders, Runnable onLoyalty, Runnable onLogout) {
        this.onOrdersClick = onOrders;
        this.onLoyaltyClick = onLoyalty;
        this.onLogoutClick = onLogout;
    }

    public Scene createBrowseScene() throws Exception {
        // Get cart item count for badge
        int cartCount = 0;
        try {
            cartCount = cartController.getCartItems(currentUserId).size();
        } catch (Exception ex) {
            System.err.println("Browse cart count load failed: " + ex.getMessage());
        }
        final int finalCartCount = cartCount;

        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-customer");

        // Navigation bar
        HBox nav = NavigationBar.create(false, "browse",
            null,           // browse (already here)
            onCartClick,
            onOrdersClick,
            onLoyaltyClick,
            null, null, null, null,
            onLogoutClick,
            finalCartCount);
        root.getChildren().add(nav);

        // Content
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 48, 40, 48));
        content.getStyleClass().add("page-content");

        // ── Header ──
        VBox headerTextWrapper = new VBox(4);
        Label titleLabel = new Label("Discover Your Next Great Read");
        titleLabel.getStyleClass().add("page-title");
        Label subTitle = new Label("Explore our curated collection of timeless classics and modern masterpieces");
        subTitle.getStyleClass().add("page-subtitle");
        headerTextWrapper.getChildren().addAll(titleLabel, subTitle);
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Region hSpacer = new Region(); HBox.setHgrow(hSpacer, Priority.ALWAYS);

        // Search
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_RIGHT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search books or authors...");
        searchField.setPrefWidth(400);
        searchField.setPrefHeight(46);
        searchField.getStyleClass().add("search-field");
        searchField.setTextFormatter(createValidatedFormatter(80, SEARCH_ALLOWED_PATTERN));
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("button-customer-primary");
        searchRow.getChildren().addAll(searchField, searchBtn);
        headerBox.getChildren().addAll(headerTextWrapper, hSpacer, searchRow);

        // Books grid (FlowPane)
        FlowPane booksGrid = new FlowPane(24, 32);
        booksGrid.setPadding(new Insets(10, 0, 40, 0));
        booksGrid.setAlignment(Pos.TOP_LEFT);
        booksGrid.setPrefWrapLength(1100);

        String loadError = null;
        List<Book> loadedBooks;
        try {
            loadedBooks = bookController.getAllBooks();
        } catch (Exception e) {
            loadedBooks = java.util.Collections.emptyList();
            loadError = e.getMessage();
        }
        final List<Book> cachedBooks = loadedBooks;

        // Category filter chips
        HBox chips = new HBox(12);
        chips.setAlignment(Pos.CENTER_LEFT);
        String[] categories = new String[]{"All", "Classic Literature", "Contemporary Fiction", "Mystery & Thriller", "Cooking & Food", "Science & Education", "Fantasy & Sci-Fi"};
        for (String cat : categories) {
            Label chip = new Label(cat);
            chip.getStyleClass().add("chip");
            updateChipState(chip, "All".equals(cat));

            chip.setOnMouseClicked(e -> {
                List<Book> filtered = "All".equals(cat)
                    ? cachedBooks
                    : cachedBooks.stream()
                        .filter(b -> cat.equals(getCategoryName(b)))
                        .collect(Collectors.toList());

                renderBooks(booksGrid, filtered, "No books found for category \"" + cat + "\"");

                for (javafx.scene.Node node : chips.getChildren()) {
                    if (node instanceof Label lbl) {
                        updateChipState(lbl, false);
                    }
                }
                updateChipState(chip, true);
            });
            chips.getChildren().add(chip);
        }

        if (loadError != null) {
            Label err = new Label("Error loading books: " + loadError);
            err.getStyleClass().add("error-text");
            booksGrid.getChildren().add(err);
        } else {
            renderBooks(booksGrid, cachedBooks, "No books available");
        }

        // Search handler (client-side for faster UX)
        searchBtn.setOnAction(ev -> {
            String raw = normalizeInput(searchField.getText());

            String queryError = validateSearchQuery(raw);
            if (queryError != null) {
                showWarning("Search Validation", queryError);
                return;
            }

            if (raw.isEmpty()) {
                renderBooks(booksGrid, cachedBooks, "No books available");
                return;
            }

            String kw = raw.toLowerCase();
            List<Book> results = cachedBooks.stream().filter(book -> {
                String title = book.getBookName() == null ? "" : book.getBookName().toLowerCase();
                String isbn = book.getIsbn() == null ? "" : book.getIsbn().toLowerCase();
                String category = getCategoryName(book).toLowerCase();
                return title.contains(kw) || isbn.contains(kw) || category.contains(kw);
            }).collect(Collectors.toList());

            renderBooks(booksGrid, results, "No books found for \"" + raw + "\"");
        });

        // Enter key search
        searchField.setOnAction(ev -> searchBtn.fire());

        ScrollPane scrollPane = new ScrollPane(booksGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(headerBox, chips, scrollPane);
        root.getChildren().add(content);
        VBox.setVgrow(content, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox createBookCard(Book book) {
        VBox card = new VBox(14);
        card.setPrefWidth(280);
        card.setPadding(new Insets(0, 0, 16, 0));
        card.getStyleClass().add("book-card");
        
        // Initial premium styling
        card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(33, 25, 19, 0.05), 14, 0, 0, 4); " +
                      "-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: rgba(90, 70, 56, 0.1); -fx-border-radius: 16px;");

        // Hover physics
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(155, 75, 58, 0.18), 30, 0, 0, 12); " +
                          "-fx-translate-y: -5px; " +
                          "-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: rgba(155, 75, 58, 0.3); -fx-border-radius: 16px;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(33, 25, 19, 0.05), 14, 0, 0, 4); " +
                          "-fx-translate-y: 0px; " +
                          "-fx-background-color: white; -fx-background-radius: 16px; -fx-border-color: rgba(90, 70, 56, 0.1); -fx-border-radius: 16px;");
        });

        // Elevated Book cover
        StackPane bookCover = new StackPane();
        bookCover.setPrefHeight(220);
        bookCover.setStyle("-fx-background-color: linear-gradient(to bottom right, #f4eee6, #e3dacd); -fx-background-radius: 16px 16px 0 0;");
        SVGPath bookIcon = new SVGPath();
        bookIcon.setContent("M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20");
        bookIcon.setStroke(Color.web("#a38c7f"));
        bookIcon.setStrokeWidth(2); bookIcon.setFill(Color.TRANSPARENT);
        bookIcon.setScaleX(2.0); bookIcon.setScaleY(2.0);
        bookCover.getChildren().add(bookIcon);

        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(16, 20, 10, 20));

        // Info
        VBox infoBox = new VBox(6);
        Label catLabel = new Label(getCategoryName(book));
        catLabel.getStyleClass().add("book-category-pill");
        catLabel.setStyle("-fx-background-color: #f7f3ec; -fx-text-fill: #856a56; -fx-padding: 3 8; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        Label nameLabel = new Label(book.getBookName());
        nameLabel.setWrapText(true);
        nameLabel.getStyleClass().add("book-title");
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #3b2e2a;");
        
        Label authorLabel = new Label("by " + (book.getAuthorId() > 0 ? "Author #" + book.getAuthorId() : "Unknown"));
        authorLabel.getStyleClass().add("book-author");
        authorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #8a7b72;");
        
        // Rating
        HBox ratingBox = new HBox(4);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label star = new Label("★");
        star.getStyleClass().add("rating-star");
        star.setStyle("-fx-text-fill: #E6B422; -fx-font-size: 14px;");
        Label rateText = new Label("4.8");
        rateText.getStyleClass().add("rating-value");
        rateText.setStyle("-fx-font-weight: bold; -fx-text-fill: #443c38; -fx-font-size: 13px;");
        Label reviews = new Label("(1,250 reviews)");
        reviews.getStyleClass().add("rating-reviews");
        reviews.setStyle("-fx-text-fill: #a99d94; -fx-font-size: 12px;");
        ratingBox.getChildren().addAll(star, rateText, reviews);

        infoBox.getChildren().addAll(catLabel, nameLabel, authorLabel, ratingBox);

        // Price + actions
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("$" + String.format("%.2f", book.getPrice()));
        priceLabel.getStyleClass().add("price-highlight");
        priceLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: #7A9E6B;");
        Region fSpacer = new Region(); HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Button addBtn = new Button();
        SVGPath cartIcon = new SVGPath();
        cartIcon.setContent("M9 20a1 1 0 1 0 0 2 1 1 0 0 0 0-2zm7 0a1 1 0 1 0 0 2 1 1 0 0 0 0-2zm1.75-2.25L19.5 5H5.21l-.25-1H1v2h2.3l2.87 11.5a2 2 0 0 0 2 1.5h9.58a2 2 0 0 0 2-1.75z");
        cartIcon.setFill(Color.WHITE); cartIcon.setScaleX(0.8); cartIcon.setScaleY(0.8);
        addBtn.setGraphic(cartIcon);
        addBtn.setText("Add");
        addBtn.getStyleClass().add("button-customer-primary");
        addBtn.setStyle("-fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-weight: bold;");
        
        addBtn.setOnAction(ev -> {
            if (addBtn.isDisable()) {
                return;
            }
            addBtn.setDisable(true);
            try {
                cartController.addToCart(currentUserId, book.getBookId(), 1, book.getPrice());
                addBtn.setText("Added!");
                addBtn.setStyle("-fx-background-color: #7A9E6B; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-weight: bold;");
                PauseTransition resetDelay = new PauseTransition(Duration.seconds(1.5));
                resetDelay.setOnFinished(done -> {
                    addBtn.setText("Add");
                    addBtn.setStyle("-fx-background-color: #9B4B3A; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-weight: bold;");
                    addBtn.setDisable(false);
                });
                resetDelay.play();
            } catch (Exception ex) {
                addBtn.setText("Error!");
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Add to Cart Failed");
                a.setHeaderText(null);
                a.setContentText(ex.getMessage());
                a.showAndWait();
                addBtn.setText("Add");
                addBtn.setDisable(false);
            }
        });

        footer.getChildren().addAll(priceLabel, fSpacer, addBtn);
        
        // Buy Now Button
        Button buyBtn = new Button("Buy Now");
        buyBtn.setMaxWidth(Double.MAX_VALUE);
        buyBtn.getStyleClass().add("button-outline");
        buyBtn.setStyle("-fx-border-color: #9B4B3A; -fx-text-fill: #9B4B3A; -fx-background-color: transparent; -fx-border-radius: 8px; -fx-font-weight: bold; -fx-padding: 8 0;");
        
        if (book.getStockLevel() <= 0) {
            addBtn.setText("Out of Stock");
            addBtn.setDisable(true);
            addBtn.setStyle("-fx-background-color: #cccccc; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8 16; -fx-font-weight: bold;");
            buyBtn.setText("Sold Out");
            buyBtn.setDisable(true);
            buyBtn.setStyle("-fx-border-color: #cccccc; -fx-text-fill: #cccccc; -fx-background-color: transparent; -fx-border-radius: 8px; -fx-font-weight: bold; -fx-padding: 8 0;");
        }
        
        buyBtn.setOnAction(ev -> {
            if (buyBtn.isDisable()) {
                return;
            }
            buyBtn.setDisable(true);
            try {
                cartController.addToCart(currentUserId, book.getBookId(), 1, book.getPrice());
                if (onCartClick != null) onCartClick.run();
            } catch (Exception ex) {
                buyBtn.setDisable(false);
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Buy Now Failed");
                a.setHeaderText(null);
                a.setContentText(ex.getMessage());
                a.showAndWait();
            }
        });

        contentBox.getChildren().addAll(infoBox, footer, buyBtn);
        card.getChildren().addAll(bookCover, contentBox);
        return card;
    }

    private void renderBooks(FlowPane booksGrid, List<Book> books, String emptyMessage) {
        booksGrid.getChildren().clear();
        if (books == null || books.isEmpty()) {
            if ("No books available".equals(emptyMessage)) {
                booksGrid.getChildren().add(buildEmptyState());
            } else {
                Label noRes = new Label(emptyMessage);
                noRes.getStyleClass().add("empty-copy");
                noRes.setPadding(new Insets(40));
                booksGrid.getChildren().add(noRes);
            }
            return;
        }

        for (Book book : books) {
            booksGrid.getChildren().add(createBookCard(book));
        }
    }

    private String getCategoryName(Book book) {
        return switch (book.getCategoryId()) {
            case 1 -> "Classic Literature";
            case 2 -> "Contemporary Fiction";
            case 3 -> "Mystery & Thriller";
            case 4 -> "Cooking & Food";
            case 5 -> "Science & Education";
            case 6 -> "Fantasy & Sci-Fi";
            default -> "General";
        };
    }

    private void updateChipState(Label chip, boolean active) {
        chip.getStyleClass().remove("chip-active");
        if (active) {
            chip.getStyleClass().add("chip-active");
        }
    }

    private TextFormatter<String> createValidatedFormatter(int maxLength, Pattern allowedPattern) {
        return new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next.length() > maxLength) {
                return null;
            }
            if (!allowedPattern.matcher(next).matches()) {
                return null;
            }
            return change;
        });
    }

    private String validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        if (!SEARCH_ALLOWED_PATTERN.matcher(query).matches()) {
            return "Search contains unsupported characters.";
        }
        boolean hasAlphaNumeric = query.chars().anyMatch(Character::isLetterOrDigit);
        if (!hasAlphaNumeric) {
            return "Enter at least one letter or number to search.";
        }
        return null;
    }

    private String normalizeInput(String value) {
        return value == null ? "" : value.trim();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private VBox buildEmptyState() {
        VBox e = new VBox(14); e.setAlignment(Pos.CENTER); e.setPadding(new Insets(80));
        Label icon = new Label("📚");
        icon.getStyleClass().add("empty-icon");
        Label t = new Label("No books available");
        t.getStyleClass().add("empty-title");
        Label s = new Label("Check back soon for new titles!");
        s.getStyleClass().add("empty-copy");
        e.getChildren().addAll(icon, t, s);
        return e;
    }

}
