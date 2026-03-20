package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Map;
import controller.AdminController;
import controller.AuthController;

public class AdminView {
    private AdminController adminController;
    private AuthController authController;
    
    public AdminView() {
        // Controller initialized in start() method when we have the user
    }
    
    public void start(Stage stage, AuthController authController) {
        this.authController = authController;
        this.adminController = new AdminController(authController.getCurrentUser());
        
        stage.setTitle("Readify - Admin Dashboard");
        
        // Main layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");
        
        // Top navigation
        HBox topNav = createTopNavigation(stage);
        root.setTop(topNav);
        
        // Main content
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Header
        Label dashboardTitle = new Label("Dashboard Overview");
        dashboardTitle.getStyleClass().add("h2");
        mainContent.getChildren().add(dashboardTitle);
        
        // Stats cards
        HBox statsRow = createStatsCards();
        mainContent.getChildren().add(statsRow);
        
        // Charts row
        HBox chartsRow = createCharts();
        mainContent.getChildren().add(chartsRow);
        
        root.setCenter(mainContent);
        
        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    private HBox createTopNavigation(Stage stage) {
        HBox navBar = new HBox(20);
        navBar.getStyleClass().add("nav-bar");
        navBar.setAlignment(Pos.CENTER_LEFT);
        
        Label logo = new Label("Readify Admin");
        logo.setStyle("-fx-text-fill: #8d4034; -fx-font-size: 18; -fx-font-weight: bold; -fx-font-family: 'Georgia';");
        
        HBox navLinks = new HBox(10);
        String[] links = {"Dashboard", "Inventory", "Reports", "Users", "Store"};
        for (String link : links) {
            Button btn = new Button(link);
            btn.getStyleClass().addAll("button", "nav-button");
            
            // Link "Store" back to customer view
            if (link.equals("Store")) {
                btn.setOnAction(e -> {
                    new BrowseBooksView().start(stage, authController);
                });
            }
            
            navLinks.getChildren().add(btn);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().addAll("button", "nav-button");
        logoutBtn.setOnAction(e -> {
            authController.logout();
            new LoginView().start(stage);
        });
        
        navBar.getChildren().addAll(logo, navLinks, spacer, logoutBtn);
        
        return navBar;
    }
    
    private HBox createStatsCards() {
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        
        Map<String, Object> stats = adminController.getDashboardStats();
        
        statsRow.getChildren().addAll(
            createStatCard("Total Revenue", (String) stats.getOrDefault("totalRevenue", "$0.00"), "↑12.5% from last month", "#386641"),
            createStatCard("Total Orders", (String) stats.getOrDefault("totalOrders", "0"), "↑8.2% from last month", "#386641"),
            createStatCard("Total Customers", (String) stats.getOrDefault("totalCustomers", "0"), "↑15.3% from last month", "#386641"),
            createStatCard("Avg Order Value", (String) stats.getOrDefault("avgOrderValue", "$0.00"), "↓3.1% from last month", "#e74c3c")
        );
        
        return statsRow;
    }
    
    private VBox createStatCard(String title, String value, String trend, String trendColor) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: #4a3b32;");
        
        Label trendLabel = new Label(trend);
        trendLabel.setStyle("-fx-text-fill: " + trendColor + "; -fx-font-size: 12;");
        
        card.getChildren().addAll(titleLabel, valueLabel, trendLabel);
        
        return card;
    }
    
    private HBox createCharts() {
        HBox chartsRow = new HBox(20);
        chartsRow.setAlignment(Pos.CENTER);
        
        // Sales Trend Chart
        VBox salesChart = createSalesChart();
        salesChart.setPrefWidth(600);
        
        // Categories Chart (simplified - using a VBox with labels)
        VBox categoriesBox = createCategoriesBox();
        categoriesBox.setPrefWidth(500);
        
        chartsRow.getChildren().addAll(salesChart, categoriesBox);
        
        return chartsRow;
    }
    
    private VBox createSalesChart() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        container.setPadding(new Insets(20));
        
        Label title = new Label("Sales Trend");
        title.getStyleClass().add("h2");
        title.setStyle("-fx-font-size: 18px;");
        
        // Create a simple line chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Monthly Sales");
        lineChart.setPrefHeight(250);
        lineChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");
        series.getData().add(new XYChart.Data<>("Jan", 4500));
        series.getData().add(new XYChart.Data<>("Feb", 5200));
        series.getData().add(new XYChart.Data<>("Mar", 6100));
        series.getData().add(new XYChart.Data<>("Apr", 7500));
        series.getData().add(new XYChart.Data<>("May", 8200));
        
        lineChart.getData().add(series);
        
        // Style the chart
        lineChart.setStyle("-fx-background-color: transparent;");
        
        container.getChildren().addAll(title, lineChart);
        
        return container;
    }
    
    private VBox createCategoriesBox() {
        VBox container = new VBox(15);
        container.getStyleClass().add("card");
        container.setPadding(new Insets(20));
        
        Label title = new Label("Categories");
        title.getStyleClass().add("h2");
        title.setStyle("-fx-font-size: 18px;");
        
        // Simple category list
        String[] categories = {
            "Classic Literature: 35%",
            "Contemporary Fiction: 25%",
            "Mystery & Thriller: 20%",
            "Fantasy & Sci-Fi: 12%",
            "Others: 8%"
        };
        
        VBox categoryList = new VBox(10);
        for (String cat : categories) {
            Label catLabel = new Label("• " + cat);
            catLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #4a3b32;");
            
            ProgressBar bar = new ProgressBar();
            bar.setPrefWidth(400);
            bar.setStyle("-fx-accent: #e07a5f;"); // Use terracotta accent
            
            // Simulate progress
            double progress = 0.5;
            if (cat.contains("35%")) progress = 0.35;
            if (cat.contains("25%")) progress = 0.25;
            if (cat.contains("20%")) progress = 0.20;
            if (cat.contains("12%")) progress = 0.12;
            if (cat.contains("8%")) progress = 0.08;
            
            bar.setProgress(progress);
            
            VBox row = new VBox(5);
            row.getChildren().addAll(catLabel, bar);
            
            categoryList.getChildren().add(row);
        }
        
        container.getChildren().addAll(title, categoryList);
        
        return container;
    }
}