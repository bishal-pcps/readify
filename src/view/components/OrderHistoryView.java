package view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.AuthController;
import controller.BookController;
import controller.CartController;
import controller.OrderController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import model.LineItem;
import model.Order;
import util.OrderStatusUtil;

/**
 * Order History view — matches OrderHistory.tsx design.
 * Card-per-order layout with item breakdown and contextual action buttons.
 */
public class OrderHistoryView {
    private OrderController orderController;
    private CartController cartController;
    private Runnable onBackClick;
    private Runnable onCartClick;
    private Runnable onLoyaltyClick;
    private Runnable onLogoutClick;
    private BookController bookController;
    private final Map<Integer, String> bookNameCache = new HashMap<>();

    public OrderHistoryView(Stage stage, OrderController orderController,
            AuthController authController, Runnable onBackClick) {
        this.orderController = orderController;
        this.onBackClick = onBackClick;
        this.cartController = null;
        this.onCartClick = null;
        this.onLoyaltyClick = null;
        this.onLogoutClick = () -> {
            authController.logout();
            onBackClick.run();
        };
        try {
            this.bookController = new BookController();
        } catch (Exception ignored) {
            this.bookController = null;
        }
    }

    public void setCartController(CartController cc, Runnable onCartClick, Runnable onLoyaltyClick, Runnable onLogoutClick) {
        this.cartController = cc;
        this.onCartClick = onCartClick;
        this.onLoyaltyClick = onLoyaltyClick;
        if (onLogoutClick != null) {
            this.onLogoutClick = onLogoutClick;
        }
    }

    public Scene createOrderHistoryScene() {
        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-customer");

        // Nav
        HBox nav = NavigationBar.create(false, "history",
            onBackClick, onCartClick, null, onLoyaltyClick,
            null, null, null, null,
            onLogoutClick, 0);

        // Content
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 48, 40, 48));
        content.getStyleClass().add("page-content");

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#9CAF88", "M20 7H4a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2z M1 3h22v4H1z");
        Label title = new Label("Order History");
        title.getStyleClass().add("page-title-sm");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button browseBtn = new Button("Browse Books");
        browseBtn.getStyleClass().add("button-customer-primary");
        browseBtn.setOnAction(e -> onBackClick.run());
        header.getChildren().addAll(iconBox, title, spacer, browseBtn);

        // Load orders
        VBox ordersBox = new VBox(20);
        try {
            int userId = AuthController.getCurrentCustomer().getUserId();
            List<Order> orders = orderController.getCustomerOrders(userId);
            if (orders.isEmpty()) {
                ordersBox.getChildren().add(buildEmptyState());
            } else {
                for (Order order : orders) {
                    ordersBox.getChildren().add(buildOrderCard(order));
                }
            }
        } catch (Exception e) {
            Label err = new Label("Error loading orders: " + e.getMessage());
            err.getStyleClass().add("error-text");
            ordersBox.getChildren().add(err);
        }

        content.getChildren().addAll(header, ordersBox);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #FDF8F0; -fx-background: #FDF8F0;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox buildOrderCard(Order order) {
        VBox card = new VBox(0);
        card.getStyleClass().add("surface-card");
        card.setPadding(Insets.EMPTY);

        card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(33, 25, 19, 0.05), 14, 0, 0, 4); " +
                      "-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: rgba(90, 70, 56, 0.1); -fx-border-radius: 12px;");
        card.setOnMouseEntered(e -> card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(154, 175, 136, 0.25), 24, 0, 0, 8); " +
                      "-fx-translate-y: -2px; -fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: rgba(154, 175, 136, 0.4); -fx-border-radius: 12px;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(33, 25, 19, 0.05), 14, 0, 0, 4); " +
                      "-fx-translate-y: 0px; -fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: rgba(90, 70, 56, 0.1); -fx-border-radius: 12px;"));

        // ── Order header ──
        String statusText = getStatusText(order.getOrderStatusId());
        String statusColor = getStatusColor(order.getOrderStatusId());
        String statusEmoji = getStatusEmoji(order.getOrderStatusId());

        HBox cardHeader = new HBox();
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(20, 24, 16, 24));
        cardHeader.setStyle("-fx-border-color: transparent transparent rgba(93,74,58,0.1) transparent; -fx-border-width: 1;");

        VBox orderInfo = new VBox(4);
        HBox.setHgrow(orderInfo, Priority.ALWAYS);
        HBox idRow = new HBox(12);
        idRow.setAlignment(Pos.CENTER_LEFT);
        Label idLabel = new Label("Order #ORD-" + order.getOrderId());
        idLabel.getStyleClass().add("book-title");
        Label statusBadge = new Label(statusEmoji + " " + statusText);
        statusBadge.setStyle("-fx-background-color: " + statusColor + "15; -fx-text-fill: " + statusColor + ";" +
            "-fx-background-radius: 20px; -fx-padding: 5 14; -fx-font-size: 13px; -fx-font-weight: 800;");
        idRow.getChildren().addAll(idLabel, statusBadge);
        Label dateLabel = new Label("Placed on " + (order.getOrderDate() != null ? order.getOrderDate().toString() : "—"));
        dateLabel.getStyleClass().add("muted-copy");
        orderInfo.getChildren().addAll(idRow, dateLabel);

        VBox totalBox = new VBox(4);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label totalLabel = new Label("Order Total");
        totalLabel.getStyleClass().add("muted-copy");
        Label amountLabel = new Label("$" + String.format("%.2f", order.getTotalAmount()));
        amountLabel.getStyleClass().add("price-highlight");
        amountLabel.setStyle("-fx-text-fill: #9CAF88;");
        totalBox.getChildren().addAll(totalLabel, amountLabel);

        cardHeader.getChildren().addAll(orderInfo, totalBox);

        // ── Items ──
        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(16, 24, 16, 24));
        List<LineItem> orderItems = java.util.Collections.emptyList();
        try {
            orderItems = orderController.getOrderDetails(order.getOrderId());
            if (!orderItems.isEmpty()) {
                for (LineItem item : orderItems) {
                    HBox itemRow = new HBox();
                    itemRow.setPadding(new Insets(10, 14, 10, 14));
                    itemRow.setAlignment(Pos.CENTER_LEFT);
                    itemRow.getStyleClass().add("surface-card-compact");
                    VBox itemInfo = new VBox(3);
                    HBox.setHgrow(itemInfo, Priority.ALWAYS);
                    Label bookName = new Label(resolveBookName(item.getBookId()));
                    bookName.getStyleClass().add("section-heading");
                    bookName.setStyle("-fx-font-size: 14px;");
                    Label author = new Label("Book ID: " + item.getBookId());
                    author.getStyleClass().add("muted-copy");
                    itemInfo.getChildren().addAll(bookName, author);
                    VBox priceBox = new VBox(2);
                    priceBox.setAlignment(Pos.CENTER_RIGHT);
                    Label qtyLbl = new Label("Qty: " + item.getQuantity());
                    qtyLbl.getStyleClass().add("muted-copy");
                    Label priceLbl = new Label("$" + String.format("%.2f", item.getPrice() * item.getQuantity()));
                    priceLbl.getStyleClass().add("price-highlight");
                    priceLbl.setStyle("-fx-font-size: 15px; -fx-text-fill: #9CAF88;");
                    priceBox.getChildren().addAll(qtyLbl, priceLbl);
                    itemRow.getChildren().addAll(itemInfo, priceBox);
                    itemsBox.getChildren().add(itemRow);
                }
            } else {
                Label noItems = new Label("Item details unavailable");
                noItems.getStyleClass().add("muted-copy");
                itemsBox.getChildren().add(noItems);
            }
        } catch (Exception e) {
            Label errLbl = new Label("Could not load items");
            errLbl.getStyleClass().add("error-text");
            itemsBox.getChildren().add(errLbl);
        }
        List<LineItem> finalOrderItems = orderItems;

        // ── Action buttons ──
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(14, 24, 18, 24));
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-border-color: rgba(93,74,58,0.1) transparent transparent transparent; -fx-border-width: 1;");

        if (order.getOrderStatusId() == 2) { // shipped
            Button trackBtn = actionBtn("Track Order", "#5A6F8C");
            trackBtn.setOnAction(e -> showTrackDialog(order));
            actions.getChildren().add(trackBtn);
        }
        Button detailsBtn = actionBtn("View Details", "#A8978A");
        detailsBtn.setOnAction(e -> showDetailsDialog(order));
        Button orderAgainBtn = actionBtn("Order Again", "#E67E5A");
        orderAgainBtn.setOnAction(e -> {
            if (cartController == null || finalOrderItems.isEmpty()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Order Again");
                a.setHeaderText(null);
                a.setContentText("Could not re-add this order's items to cart.");
                a.showAndWait();
                return;
            }

            try {
                int userId = AuthController.getCurrentCustomer().getUserId();
                for (LineItem item : finalOrderItems) {
                    cartController.addToCart(userId, item.getBookId(), item.getQuantity(), item.getPrice());
                }
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Order Again");
                a.setHeaderText(null);
                a.setContentText("Items have been added to your cart!");
                a.showAndWait();
                if (onCartClick != null) {
                    onCartClick.run();
                }
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Order Again");
                a.setHeaderText(null);
                a.setContentText("Failed to add items to cart: " + ex.getMessage());
                a.showAndWait();
            }
        });
        actions.getChildren().addAll(detailsBtn, orderAgainBtn);

        card.getChildren().addAll(cardHeader, itemsBox, actions);
        return card;
    }

    private VBox buildEmptyState() {
        VBox empty = new VBox(16);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(60));
        empty.getStyleClass().add("surface-card");
        Label icon = new Label("📦");
        icon.getStyleClass().add("empty-icon");
        Label title = new Label("No orders yet");
        title.getStyleClass().add("empty-title");
        Label sub = new Label("Start exploring our collection to place your first order");
        sub.getStyleClass().add("empty-copy");
        Button browseBtn = new Button("Browse Books");
        browseBtn.getStyleClass().add("button-customer-primary");
        browseBtn.setOnAction(e -> onBackClick.run());
        empty.getChildren().addAll(icon, title, sub, browseBtn);
        return empty;
    }

    private Button actionBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + color + "15; -fx-text-fill: " + color + "; -fx-font-weight: 800;" +
            "-fx-background-radius: 8px; -fx-padding: 8 16; -fx-cursor: hand; -fx-font-size: 13px; -fx-border-color: " + color + "33; -fx-border-radius: 8px;");
        return b;
    }

    private void showTrackDialog(Order order) {
        javafx.scene.control.Dialog<Void> d = new javafx.scene.control.Dialog<>();
        d.setTitle("Track Shipment");
        d.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        VBox content = new VBox(15); content.setPadding(new Insets(30, 40, 30, 40));
        content.setAlignment(Pos.CENTER);
        Label icon = new Label("🚚"); icon.setStyle("-fx-font-size: 48px;");
        Label title = new Label("On its way!"); title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #5A6F8C;");
        Label msg = new Label("Your package for Order #ORD-" + order.getOrderId() + "\nis arriving in 2-3 business days.");
        msg.setStyle("-fx-text-fill: #8a7b72; -fx-font-size: 14px; -fx-text-alignment: center;");
        content.getChildren().addAll(icon, title, msg);
        d.getDialogPane().setContent(content);
        d.showAndWait();
    }

    private void showDetailsDialog(Order order) {
        javafx.scene.control.Dialog<Void> d = new javafx.scene.control.Dialog<>();
        d.setTitle("Order #ORD-" + order.getOrderId());
        d.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        VBox content = new VBox(15); content.setPadding(new Insets(30, 40, 30, 40));
        Label h1 = new Label("Order Summary"); h1.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #9CAF88;");
        Label date = new Label("Placement Date: " + (order.getOrderDate() != null ? order.getOrderDate() : "—"));
        Label addr = new Label("Shipping Address:\n" + (order.getShippingAddress() != null ? order.getShippingAddress() : "—"));
        Label tot = new Label("Total Paid: $" + String.format("%.2f", order.getTotalAmount()));
        tot.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #2C6E6E;");
        date.setStyle("-fx-text-fill: #8a7b72; -fx-font-size: 14px;"); 
        addr.setStyle("-fx-text-fill: #8a7b72; -fx-font-size: 14px;");
        content.getChildren().addAll(h1, date, addr, tot);
        d.getDialogPane().setContent(content);
        d.showAndWait();
    }

    // Status helpers
    private String getStatusText(int statusId) {
        return OrderStatusUtil.label(statusId);
    }
    private String getStatusColor(int statusId) {
        return OrderStatusUtil.color(statusId);
    }
    private String getStatusEmoji(int statusId) {
        return OrderStatusUtil.emoji(statusId);
    }

    private StackPane makeIconBox(String color, String svgData) {
        StackPane box = new StackPane(); box.setPrefSize(48, 48);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath(); icon.setContent(svgData);
        icon.setStroke(Color.WHITE); icon.setStrokeWidth(2); icon.setFill(Color.TRANSPARENT);
        box.getChildren().add(icon); return box;
    }

    private String resolveBookName(int bookId) {
        if (bookNameCache.containsKey(bookId)) {
            return bookNameCache.get(bookId);
        }

        String label = "Book #" + bookId;
        if (bookController != null) {
            try {
                model.Book book = bookController.getBook(bookId);
                if (book != null && book.getBookName() != null && !book.getBookName().isBlank()) {
                    label = book.getBookName();
                }
            } catch (Exception ignored) {
                // Keep fallback label when lookup fails.
            }
        }

        bookNameCache.put(bookId, label);
        return label;
    }
}
