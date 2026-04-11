package view.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import controller.AuthController;
import controller.BookController;
import controller.CartController;
import controller.OrderController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Book;
import model.CartItem;

/**
 * Modernized Shopping Cart View.
 * Card-based items, Sticky total bar, integrated NavigationBar.
 */
public class CartView {
    private Stage stage;
    private CartController cartController;
    private Runnable onCheckoutClick;
    private Runnable onBackClick;
    private Runnable onOrdersClick;
    private Runnable onLoyaltyClick;
    private Runnable onLogoutClick;
    private BookController bookController;
    private final Set<Integer> selectedCartItemIds = new HashSet<>();
    private boolean selectionInitialized;
    private boolean isRefreshingScene;

    public CartView(Stage stage, CartController cartController, OrderController orderController,
            AuthController authController, Runnable onCheckoutClick, Runnable onBackClick) {
        this.stage = stage;
        this.cartController = cartController;
        this.onCheckoutClick = onCheckoutClick;
        this.onBackClick = onBackClick;
        this.onOrdersClick = null;
        this.onLoyaltyClick = null;
        this.selectionInitialized = false;
        this.isRefreshingScene = false;
        this.onLogoutClick = () -> {
            authController.logout();
            onBackClick.run();
        };
    }

    public void setNavCallbacks(Runnable onOrders, Runnable onLoyalty, Runnable onLogout) {
        this.onOrdersClick = onOrders;
        this.onLoyaltyClick = onLoyalty;
        if (onLogout != null) {
            this.onLogoutClick = onLogout;
        }
    }

    public Scene createCartScene() throws Exception {
        int userId = AuthController.getCurrentCustomer().getUserId();
        List<CartItem> items = cartController.getCartItems(userId);
        syncSelectedItems(items);

        int selectedCount = countSelectedItems(items);
        double selectedTotal = calculateSelectedTotal(items);

        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-customer");

        // Nav
        javafx.scene.layout.HBox nav = NavigationBar.create(false, "cart",
            onBackClick, null, onOrdersClick, onLoyaltyClick,
            null, null, null, null,
            onLogoutClick, items.size());

        // Body
        VBox body = new VBox(24);
        body.setPadding(new Insets(32, 48, 40, 48));
        body.getStyleClass().add("page-content");

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#2C6E6E", "M9 20a1 1 0 1 0 0 2 1 1 0 1 0 0-2z M20 20a1 1 0 1 0 0 2 1 1 0 1 0 0-2z M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6");
        Label title = new Label("Your Shopping Cart");
        title.getStyleClass().add("page-title-sm");
        header.getChildren().addAll(iconBox, title);

        // List
        VBox list = new VBox(16);
        if (!items.isEmpty()) {
            list.getChildren().add(buildSelectionBar(items));
        }
        if (items.isEmpty()) {
            list.getChildren().add(buildEmptyState());
        } else {
            for (CartItem item : items) {
                String displayName = item.getBookName();
                list.getChildren().add(createCartItemCard(item, displayName, selectedCartItemIds.contains(item.getCartItemId())));
            }
        }

        ScrollPane sp = new ScrollPane(list);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);

        // Summary bar
        HBox summary = new HBox(20);
        summary.setAlignment(Pos.CENTER_RIGHT);
        summary.setPadding(new Insets(24, 32, 24, 32));
        summary.getStyleClass().add("summary-panel");

        VBox totalTxt = new VBox(4);
        totalTxt.setAlignment(Pos.CENTER_RIGHT);
        Label sub = new Label("Subtotal (" + selectedCount + " selected item(s)):");
        sub.getStyleClass().add("muted-copy");
        Label totalLbl = new Label("$" + String.format("%.2f", selectedTotal));
        totalLbl.getStyleClass().add("price-highlight");
        totalTxt.getChildren().addAll(sub, totalLbl);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        Button contBtn = new Button("Continue Shopping");
        contBtn.getStyleClass().add("button-outline");
        contBtn.setOnAction(e -> onBackClick.run());

        Button checkoutBtn = new Button("Proceed to Checkout");
        checkoutBtn.getStyleClass().add("button-customer-primary");
        checkoutBtn.setDisable(items.isEmpty() || selectedCount == 0);
        checkoutBtn.setOnAction(e -> {
            List<Integer> selectedForCheckout = new ArrayList<>();
            for (CartItem item : items) {
                if (selectedCartItemIds.contains(item.getCartItemId())) {
                    selectedForCheckout.add(item.getCartItemId());
                }
            }

            if (selectedForCheckout.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Select Books");
                a.setHeaderText(null);
                a.setContentText("Please select at least one book to checkout.");
                a.showAndWait();
                return;
            }

            cartController.setSelectedCartItemIds(selectedForCheckout);
            onCheckoutClick.run();
        });

        summary.getChildren().addAll(contBtn, spacer, totalTxt, checkoutBtn);

        body.getChildren().addAll(header, sp);
        root.getChildren().addAll(nav, body, summary);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Entrance animation
        body.setOpacity(0);
        body.setTranslateY(15);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), body);
        ft.setToValue(1.0);
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(500), body);
        tt.setToY(0);
        new javafx.animation.ParallelTransition(ft, tt).play();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private HBox createCartItemCard(CartItem item, String resolvedBookName, boolean selected) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.getStyleClass().add("surface-card-compact");

        CheckBox selectBox = new CheckBox();
        selectBox.setSelected(selected);
        selectBox.setOnAction(e -> {
            if (selectBox.isSelected()) {
                selectedCartItemIds.add(item.getCartItemId());
            } else {
                selectedCartItemIds.remove(item.getCartItemId());
            }
            refreshCartScene();
        });

        StackPane bookIcon = makeIconBox("#FDF8F0", "M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1 0-5H20");
        ((SVGPath)bookIcon.getChildren().get(0)).setStroke(Color.web("#9B4B3A"));

        VBox info = new VBox(6);
        HBox.setHgrow(info, Priority.ALWAYS);
        String title = (resolvedBookName != null && !resolvedBookName.isBlank())
            ? resolvedBookName
            : "Book #" + item.getBookId();
        Label name = new Label(title);
        name.getStyleClass().add("book-title");
        Label priceLbl = new Label("Book ID: " + item.getBookId() + "    Unit Price: $" + String.format("%.2f", item.getPrice()));
        priceLbl.getStyleClass().add("muted-copy");
        info.getChildren().addAll(name, priceLbl);

        VBox right = new VBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label sub = new Label("$" + String.format("%.2f", item.getPrice() * item.getQuantity()));
        sub.getStyleClass().add("price-highlight");

        HBox act = new HBox(12);
        act.setAlignment(Pos.CENTER_RIGHT);

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("qty-button-minus");
        Label qtyLbl = new Label(String.valueOf(item.getQuantity()));
        qtyLbl.getStyleClass().add("qty-label");
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("qty-button-plus");

        minusBtn.setOnAction(e -> changeQuantity(item, item.getQuantity() - 1));
        plusBtn.setOnAction(e -> changeQuantity(item, item.getQuantity() + 1));

        Button rem = new Button("🗑");
        rem.getStyleClass().add("button-danger-ghost");
        rem.setOnAction(e -> {
            try {
                cartController.removeFromCart(item.getCartItemId());
                refreshCartScene();
            } catch (Exception ex) {
                showCartError("Failed to remove item: " + ex.getMessage());
            }
        });
        act.getChildren().addAll(minusBtn, qtyLbl, plusBtn, rem);
        right.getChildren().addAll(sub, act);

        card.getChildren().addAll(selectBox, bookIcon, info, right);
        return card;
    }

    private HBox buildSelectionBar(List<CartItem> items) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        CheckBox selectAll = new CheckBox("Select all");
        selectAll.getStyleClass().add("form-label");
        selectAll.setCursor(javafx.scene.Cursor.HAND);
        selectAll.setSelected(countSelectedItems(items) == items.size());
        selectAll.setOnAction(e -> {
            if (selectAll.isSelected()) {
                selectedCartItemIds.clear();
                for (CartItem item : items) {
                    selectedCartItemIds.add(item.getCartItemId());
                }
            } else {
                selectedCartItemIds.clear();
            }
            refreshCartScene();
        });

        Label hint = new Label("Choose the books you want to buy now.");
        hint.getStyleClass().add("muted-copy");
        row.getChildren().addAll(selectAll, hint);
        return row;
    }

    private VBox buildEmptyState() {
        VBox e = new VBox(20);
        e.setAlignment(Pos.CENTER);
        e.setPadding(new Insets(100, 30, 100, 30));
        e.getStyleClass().add("surface-card");
        
        StackPane iconBox = makeIconBox("#9B4B3A", "M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z");
        iconBox.setScaleX(1.8);
        iconBox.setScaleY(1.8);

        Label t = new Label("Your cart is empty");
        t.getStyleClass().add("empty-title");
        t.setPadding(new Insets(20, 0, 0, 0));
        
        Label sub = new Label("Add some books to your cart and they will show up here.");
        sub.getStyleClass().add("muted-copy");

        Button b = new Button("Start Browsing Catalog");
        b.getStyleClass().add("button-customer-primary");
        b.setOnAction(ev -> onBackClick.run());
        
        e.getChildren().addAll(iconBox, t, sub, b);
        return e;
    }

    private StackPane makeIconBox(String color, String svg) {
        StackPane b = new StackPane(); b.setPrefSize(54, 54);
        b.setStyle("-fx-background-color: " + color + "1a; -fx-background-radius: 10px;");
        SVGPath i = new SVGPath(); i.setContent(svg);
        i.setFill(Color.web(color));
        b.getChildren().add(i); return b;
    }

    private void changeQuantity(CartItem item, int newQuantity) {
        int bounded = Math.max(1, Math.min(99, newQuantity));
        if (bounded == item.getQuantity()) {
            return;
        }
        try {
            cartController.updateCartItemQuantity(item.getCartItemId(), bounded);
            refreshCartScene();
        } catch (Exception ex) {
            showCartError("Failed to update quantity: " + ex.getMessage());
        }
    }

    private void refreshCartScene() {
        if (isRefreshingScene) {
            return;
        }
        isRefreshingScene = true;
        try {
            stage.setScene(createCartScene());
        } catch (Exception ex) {
            System.err.println("Cart scene refresh failed: " + ex.getMessage());
        } finally {
            isRefreshingScene = false;
        }
    }

    private void syncSelectedItems(List<CartItem> items) {
        Set<Integer> currentIds = new HashSet<>();
        for (CartItem item : items) {
            currentIds.add(item.getCartItemId());
        }

        if (!selectionInitialized) {
            selectedCartItemIds.addAll(currentIds);
            selectionInitialized = true;
            return;
        }

        selectedCartItemIds.retainAll(currentIds);
    }

    private int countSelectedItems(List<CartItem> items) {
        int count = 0;
        for (CartItem item : items) {
            if (selectedCartItemIds.contains(item.getCartItemId())) {
                count++;
            }
        }
        return count;
    }

    private double calculateSelectedTotal(List<CartItem> items) {
        double total = 0;
        for (CartItem item : items) {
            if (selectedCartItemIds.contains(item.getCartItemId())) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private Map<Integer, String> getBookNamesById() {
        Map<Integer, String> names = new HashMap<>();
        try {
            if (bookController == null) {
                bookController = new BookController();
            }
            for (Book book : bookController.getAllBooks()) {
                if (book != null && book.getBookName() != null && !book.getBookName().isBlank()) {
                    names.put(book.getBookId(), book.getBookName());
                }
            }
        } catch (Exception ex) {
            System.err.println("Cart catalog lookup failed: " + ex.getMessage());
        }
        return names;
    }

    private void showCartError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Cart Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
