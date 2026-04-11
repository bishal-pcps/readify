package com.readify;

import controller.AdminController;
import controller.AuthController;
import controller.BookController;
import controller.CartController;
import controller.CustomerController;
import java.sql.SQLException;

import controller.OrderController;
import javafx.application.Application;
import javafx.stage.Stage;
import service.BookService;
import service.CartService;
import view.components.AdminDashboardView;
import view.components.AdminOrdersView;
import view.components.BrowseView;
import view.components.CartView;
import view.components.CheckoutView;

import view.components.InventoryView;
import view.components.LoyaltyView;
import view.components.OrderHistoryView;
import view.components.ReportsView;
import view.components.UnifiedAuthView;
import view.components.UserManagementView;

public class Main extends Application {
    private AuthController authController;
    private BookController bookController;
    private CartController cartController;
    private OrderController orderController;
    
    private AdminController adminController;
    private CustomerController customerController;

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize lightweight controller only; heavy controllers are lazily created per view.
            authController = new AuthController();

            // Stage Settings
            primaryStage.setTitle("Readify - Book Store");
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            showAuthView(primaryStage);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─── Authentication ───
    private void showAuthView(Stage stage) {
        try {
            UnifiedAuthView authView = new UnifiedAuthView(stage, authController, () -> {
                if (AuthController.isAdminLoggedIn()) {
                    showAdminDashboard(stage);
                } else {
                    showCustomerBrowse(stage);
                }
            });
            stage.setScene(authView.createAuthScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Customer Views ───
    private void showCustomerBrowse(Stage stage) {
        try {
            if (bookController == null) {
                bookController = new BookController();
            }
            if (cartController == null) {
                cartController = new CartController();
            }
            int userId = AuthController.getCurrentCustomer().getUserId();
            BrowseView view = new BrowseView(stage, bookController, cartController, userId,
                () -> showCustomerCart(stage));

            // Wire extra nav callbacks
            view.setNavCallbacks(
                () -> showCustomerOrderHistory(stage),
                () -> showLoyalty(stage),
                () -> { authController.logout(); showAuthView(stage); }
            );

            stage.setScene(view.createBrowseScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showCustomerCart(Stage stage) {
        try {
            if (cartController == null) {
                cartController = new CartController();
            }
            if (orderController == null) {
                orderController = new OrderController();
            }
            CartView view = new CartView(stage, cartController, orderController, authController,
                () -> showCustomerCheckout(stage),
                () -> showCustomerBrowse(stage));

            // Wire extra nav callbacks
            view.setNavCallbacks(
                () -> showCustomerOrderHistory(stage),
                () -> showLoyalty(stage),
                () -> { authController.logout(); showAuthView(stage); }
            );

            stage.setScene(view.createCartScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showCustomerCheckout(Stage stage) {
        try {
            if (cartController == null) {
                cartController = new CartController();
            }
            if (orderController == null) {
                orderController = new OrderController();
            }
            CheckoutView view = new CheckoutView(stage, orderController, cartController, authController,
                () -> showCustomerOrderHistory(stage),
                () -> showCustomerCart(stage),
                () -> { authController.logout(); showAuthView(stage); });
            stage.setScene(view.createCheckoutScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showCustomerOrderHistory(Stage stage) {
        try {
            if (orderController == null) {
                orderController = new OrderController();
            }
            if (cartController == null) {
                cartController = new CartController();
            }
            OrderHistoryView view = new OrderHistoryView(stage, orderController, authController,
                () -> showCustomerBrowse(stage));

            // Allow "Order Again" logic
            view.setCartController(
                cartController,
                () -> showCustomerCart(stage),
                () -> showLoyalty(stage),
                () -> { authController.logout(); showAuthView(stage); }
            );

            stage.setScene(view.createOrderHistoryScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showLoyalty(Stage stage) {
        try {
            if (customerController == null) {
                customerController = new CustomerController();
            }
            LoyaltyView view = new LoyaltyView(stage, authController, customerController,
                () -> showCustomerBrowse(stage),
                () -> { authController.logout(); showAuthView(stage); });
            view.setNavCallbacks(
                () -> showCustomerOrderHistory(stage),
                () -> showCustomerCart(stage)
            );
            stage.setScene(view.createLoyaltyScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Admin Views ───
    private void showAdminDashboard(Stage stage) {
        try {
            if (adminController == null) {
                adminController = new AdminController();
            }
            AdminDashboardView view = new AdminDashboardView(stage, adminController,
                () -> showInventory(stage),
                () -> showAdminOrders(stage),
                () -> { authController.logout(); showAuthView(stage); });

            view.setReportsClick(() -> showReports(stage));
            view.setUsersClick(() -> showUserManagement(stage));

            stage.setScene(view.createAdminDashboardScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAdminOrders(Stage stage) {
        try {
            if (orderController == null) {
                orderController = new OrderController();
            }
            AdminOrdersView view = new AdminOrdersView(stage, orderController,
                () -> showInventory(stage),
                () -> showReports(stage),
                () -> showUserManagement(stage),
                () -> showAdminDashboard(stage),
                () -> { authController.logout(); showAuthView(stage); });
            stage.setScene(view.createOrdersScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showInventory(Stage stage) {
        try {
            if (bookController == null) {
                bookController = new BookController();
            }
            InventoryView view = new InventoryView(stage, bookController,
                () -> showAdminDashboard(stage),
                () -> showReports(stage),
                () -> showUserManagement(stage),
                () -> showAdminOrders(stage),
                () -> { authController.logout(); showAuthView(stage); });
            stage.setScene(view.createInventoryScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showReports(Stage stage) {
        try {
            if (adminController == null) {
                adminController = new AdminController();
            }
            ReportsView view = new ReportsView(stage, adminController,
                () -> showInventory(stage),
                () -> showUserManagement(stage),
                () -> showAdminDashboard(stage),
                () -> { authController.logout(); showAuthView(stage); });
            stage.setScene(view.createReportsScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showUserManagement(Stage stage) {
        try {
            UserManagementView view = new UserManagementView(stage, authController,
                () -> showInventory(stage),
                () -> showReports(stage),
                () -> showAdminDashboard(stage),
                () -> { authController.logout(); showAuthView(stage); });
            stage.setScene(view.createUserManagementScene());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void runServiceVerification() {
        try {
            System.out.println("Initializing BookService...");
            new BookService();
            System.out.println("BookService initialized successfully.");

            System.out.println("Initializing CartService...");
            new CartService();
            System.out.println("CartService initialized successfully.");

            System.out.println("Refactor Verification Passed!");
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0 && "--verify-services".equalsIgnoreCase(args[0])) {
            runServiceVerification();
            return;
        }
        launch(args);
    }
}
