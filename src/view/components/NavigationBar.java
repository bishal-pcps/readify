package view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

public class NavigationBar {

    /**
     * Unified Navigation Bar for Admins and Customers.
     */
    public static HBox create(boolean isAdmin, String currentPage,
                             Runnable onBrowse, Runnable onCart, Runnable onOrderHistory, Runnable onLoyalty,
                             Runnable onDashboard, Runnable onInventory, Runnable onReports, Runnable onUsers,
                             Runnable onLogout, int cartCount) {

        HBox nav = new HBox();
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(16, 48, 16, 48));
        nav.setPrefHeight(80);
        nav.getStyleClass().add("nav-shell");

        // Brand
        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setCursor(javafx.scene.Cursor.HAND);
        Circle logo = new Circle(18);
        logo.getStyleClass().add("nav-logo-dot");
        Label brandName = new Label("Readify");
        brandName.getStyleClass().add("nav-brand-name");
        brand.getChildren().addAll(logo, brandName);
        brand.setOnMouseClicked(e -> {
            if (isAdmin) {
                if (onDashboard != null) {
                    onDashboard.run();
                }
            } else if (onBrowse != null) {
                onBrowse.run();
            }
        });

        Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Nav Links
        HBox links = new HBox(28);
        links.setAlignment(Pos.CENTER_LEFT);

        if (isAdmin) {
            links.getChildren().add(navLink("Dashboard", "dashboard".equalsIgnoreCase(currentPage), onDashboard));
            links.getChildren().add(navLink("Inventory", "inventory".equalsIgnoreCase(currentPage), onInventory));
            links.getChildren().add(navLink("Reports", "reports".equalsIgnoreCase(currentPage), onReports));
            links.getChildren().add(navLink("Users", "users".equalsIgnoreCase(currentPage), onUsers));
        } else {
            if (onBrowse != null || "browse".equalsIgnoreCase(currentPage)) {
                links.getChildren().add(navLink("Browse", "browse".equalsIgnoreCase(currentPage), onBrowse));
            }
            if (onOrderHistory != null || "history".equalsIgnoreCase(currentPage)) {
                links.getChildren().add(navLink("History", "history".equalsIgnoreCase(currentPage), onOrderHistory));
            }
            if (onLoyalty != null || "loyalty".equalsIgnoreCase(currentPage)) {
                links.getChildren().add(navLink("Loyalty", "loyalty".equalsIgnoreCase(currentPage), onLoyalty));
            }
        }

        Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);

        // Right Section
        HBox actions = new HBox(20);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (!isAdmin) {
            Button cartBtn = new Button("Cart (" + cartCount + ")");
            cartBtn.getStyleClass().add("button-customer-primary");
            cartBtn.setDisable(onCart == null);
            cartBtn.setOnAction(e -> {
                if (onCart != null) {
                    onCart.run();
                }
            });
            actions.getChildren().add(cartBtn);
        }

        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("button-secondary");
        logoutBtn.setDisable(onLogout == null);
        logoutBtn.setOnAction(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });
        actions.getChildren().add(logoutBtn);

        nav.getChildren().addAll(brand, spacer1, links, spacer2, actions);
        return nav;
    }

    private static Button navLink(String text, boolean active, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-link");
        if (active) {
            btn.getStyleClass().add("nav-link-active");
        }

        if (!active && action == null) {
            btn.setDisable(true);
            btn.getStyleClass().add("nav-link-disabled");
        }

        btn.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });

        return btn;
    }
}
