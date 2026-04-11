package view.components;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import controller.AdminController;
import controller.BookController;
import dao.CategoryDAO;
import dao.CustomerDAO;
import dao.OrderDAO;
import dao.UserDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Book;
import model.Category;
import model.Order;
import model.User;
import util.OrderStatusUtil;

public class AdminDashboardView {
    private AdminController adminController;
    private Runnable onInventoryClick;
    private Runnable onLogoutClick;
    private Runnable onReportsClick;
    private Runnable onUsersClick;
    private Runnable onOrdersClick;

    public AdminDashboardView(Stage stage, AdminController adminController,
            Runnable onInventoryClick, Runnable onOrdersClick, Runnable onLogoutClick) {
        this.adminController = adminController;
        this.onInventoryClick = onInventoryClick;
        this.onOrdersClick = onOrdersClick;
        this.onLogoutClick = onLogoutClick;
        this.onReportsClick = null;
        this.onUsersClick = null;
    }

    public void setReportsClick(Runnable r) { this.onReportsClick = r; }
    public void setUsersClick(Runnable r) { this.onUsersClick = r; }

    public Scene createAdminDashboardScene() throws Exception {
        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-admin");

        HBox nav = NavigationBar.create(true, "dashboard",
            null, null, null, null,
            null,
            onInventoryClick,
            onReportsClick,
            onUsersClick,
            onLogoutClick, 0);
        root.getChildren().add(nav);

        VBox body = new VBox(24);
        body.setPadding(new Insets(32, 24, 32, 24));
        body.getStyleClass().add("page-content");

        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.getStyleClass().add("page-title");
        Button ordersManagerBtn = new Button("Open Orders Manager");
        ordersManagerBtn.getStyleClass().add("button-customer-primary");
        ordersManagerBtn.setDisable(onOrdersClick == null);
        ordersManagerBtn.setOnAction(e -> {
            if (onOrdersClick != null) {
                onOrdersClick.run();
            }
        });
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        HBox headerRow = new HBox(12, titleLabel, headerSpacer, ordersManagerBtn);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        int totalCustomers = 0;
        int totalOrders = 0;
        double totalRevenue = 0;
        try {
            totalCustomers = adminController.getTotalCustomers();
            totalOrders = adminController.getTotalOrders();
            totalRevenue = adminController.getTotalRevenue();
        } catch (Exception ignored) {
            // Keep zero values when backend data is not available.
        }
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        String revIcon = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14h-2v-1h-2v-4h4V9h-2V8h-2v1H9v4h4v2h2v1z";
        String ordIcon = "M16 6V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H2v13c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6h-6zm-6-2h4v2h-4V4zm10 15H4V8h16v11z";
        String usrIcon = "M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z";
        String avgIcon = "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z";

        HBox kpiRow = new HBox(16,
            makeKpiCard("Total Revenue", "$" + String.format("%,.2f", totalRevenue), "#5A6F8C", "12.5%", "#7A9E6B", revIcon),
            makeKpiCard("Total Orders", String.valueOf(totalOrders), "#2C6E6E", "8.2%", "#7A9E6B", ordIcon),
            makeKpiCard("Total Customers", String.valueOf(totalCustomers), "#A8978A", "15.3%", "#7A9E6B", usrIcon),
            makeKpiCard("Avg Order Value", "$" + String.format("%.2f", avgOrderValue), "#E6B422", "3.1%", "#B14A3A", avgIcon)
        );
        kpiRow.getChildren().forEach(card -> HBox.setHgrow(card, Priority.ALWAYS));

        HBox chartsRow = new HBox(20);
        VBox salesCard = makeLineChartCard();
        VBox categoryCard = makeCategoryCard();
        HBox.setHgrow(salesCard, Priority.ALWAYS);
        chartsRow.getChildren().addAll(salesCard, categoryCard);

        VBox recentOrders = makeRecentOrdersTable();

        body.getChildren().addAll(headerRow, kpiRow, chartsRow, recentOrders);

        // Entrance animation
        body.setOpacity(0);
        body.setTranslateY(15);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(600), body);
        ft.setToValue(1.0);
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(600), body);
        tt.setToY(0);
        new javafx.animation.ParallelTransition(ft, tt).play();

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox makeKpiCard(String title, String value, String valueColor, String trendValue, String trendColor, String svgData) {
        VBox card = new VBox(12);
        card.getStyleClass().add("kpi-card");
        
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("kpi-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        SVGPath icon = new SVGPath();
        icon.setContent(svgData);
        icon.setFill(javafx.scene.paint.Color.web(valueColor));
        icon.setScaleX(0.85);
        icon.setScaleY(0.85);
        
        StackPane iconBg = new StackPane(icon);
        iconBg.getStyleClass().add("kpi-icon-box");
        iconBg.setStyle("-fx-background-color: " + valueColor + "15;");
        
        topRow.getChildren().addAll(titleLabel, spacer, iconBg);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("kpi-value");
        valueLabel.setStyle("-fx-text-fill: " + valueColor + ";");

        HBox trendRow = new HBox(6);
        trendRow.getStyleClass().add("kpi-trend-row");
        
        boolean isUp = trendColor.equals("#7A9E6B");
        SVGPath trendIcon = new SVGPath();
        trendIcon.setContent(isUp ? "M7 14l5-5 5 5z" : "M7 10l5 5 5-5z"); 
        trendIcon.setFill(javafx.scene.paint.Color.web(trendColor));
        
        Label trend = new Label(trendValue);
        trend.getStyleClass().add("kpi-trend-label");
        trend.setStyle("-fx-text-fill: " + trendColor + ";");
        Label fromLast = new Label("vs last month");
        fromLast.getStyleClass().add("muted-copy");
        trendRow.getChildren().addAll(trendIcon, trend, fromLast);

        card.getChildren().addAll(topRow, valueLabel, trendRow);
        
        // Premium Hover Animations
        card.setOnMouseEntered(e -> {
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), card);
            tt.setToY(-5);
            tt.play();
            card.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.THREE_PASS_BOX, 
                javafx.scene.paint.Color.web(valueColor, 0.3), 30, 0, 0, 10));
        });
        card.setOnMouseExited(e -> {
            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(200), card);
            tt.setToY(0);
            tt.play();
            card.setEffect(new javafx.scene.effect.DropShadow(
                javafx.scene.effect.BlurType.THREE_PASS_BOX, 
                javafx.scene.paint.Color.web("#211913", 0.08), 18, 0, 0, 6));
        });
        
        return card;
    }

    private VBox makeLineChartCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("surface-card");

        Label title = new Label("Sales Trend");
        title.getStyleClass().add("section-heading");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setAnimated(false);
        chart.setStyle("-fx-background-color: transparent;");
        xAxis.setTickLabelFill(javafx.scene.paint.Color.web("#5D4A3A"));
        yAxis.setTickLabelFill(javafx.scene.paint.Color.web("#5D4A3A"));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            Map<String, Double> revenueData = withRecentMonths(adminController.getRevenueData());
            for (Map.Entry<String, Double> entry : revenueData.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } catch (Exception ignored) {
            // Keep empty chart if data is unavailable.
        }

        chart.getData().add(series);
        applyLineSeriesPalette(chart, "#5A6F8C");
        card.getChildren().addAll(title, chart);
        return card;
    }

    private VBox makeCategoryCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("surface-card");
        card.setPrefWidth(360);

        Label title = new Label("Categories");
        title.getStyleClass().add("section-heading");

        PieChart pie = new PieChart();
        pie.setLegendVisible(false);
        pie.setPrefHeight(300);
        pie.setAnimated(false);
        pie.setStyle("-fx-background-color: transparent;");

        VBox legend = new VBox(8);
        legend.setPadding(new Insets(4, 0, 0, 0));

        String[] colors = {"#5A6F8C", "#2C6E6E", "#A8978A", "#E6B422", "#9B4B3A"};
        int i = 0;
        for (Map.Entry<String, Integer> entry : getCategoryDistribution().entrySet()) {
            PieChart.Data data = new PieChart.Data(entry.getKey(), entry.getValue());
            final String color = colors[i % colors.length];
            data.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-pie-color: " + color + ";");
                }
            });
            pie.getData().add(data);
            legend.getChildren().add(legendItem(colors[i % colors.length], entry.getKey(), entry.getValue()));
            i++;
        }

        card.getChildren().addAll(title, pie, legend);
        return card;
    }

    private VBox makeRecentOrdersTable() {
        VBox card = new VBox(14);
        card.getStyleClass().add("surface-card");

        Label title = new Label("Recent Orders");
        title.getStyleClass().add("section-heading");

        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(240);
        table.getStyleClass().add("admin-report-table");

        TableColumn<Order, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("ORD-" + String.format("%03d", d.getValue().getOrderId())));

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(resolveCustomerName(d.getValue().getUserId())));

        TableColumn<Order, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty("$" + String.format("%.2f", d.getValue().getTotalAmount())));
        amountCol.setStyle("-fx-text-fill: #2C6E6E;");

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(statusLabel(d.getValue().getOrderStatusId())));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Order rowOrder = getTableRow() == null ? null : getTableRow().getItem();
                int statusId = rowOrder == null ? -1 : rowOrder.getOrderStatusId();
                String color = OrderStatusUtil.color(statusId);
                Label badge = new Label(status);
                badge.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + ";"
                    + "-fx-background-radius: 18px; -fx-padding: 4 10; -fx-font-size: 11px;");
                setGraphic(badge);
            }
        });

        table.getColumns().add(idCol);
        table.getColumns().add(customerCol);
        table.getColumns().add(amountCol);
        table.getColumns().add(statusCol);

        ObservableList<Order> orders = FXCollections.observableArrayList();
        try {
            List<Order> all = new OrderDAO().readAll();
            int size = Math.min(4, all.size());
            for (int i = 0; i < size; i++) {
                orders.add(all.get(i));
            }
        } catch (Exception ignored) {
            // Keep empty table if orders cannot be loaded.
        }
        table.setItems(orders);

        card.getChildren().addAll(title, table);
        return card;
    }

    private HBox legendItem(String color, String text, int count) {
        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 100; -fx-min-width: 10; -fx-min-height: 10;");

        Label label = new Label(text);
        label.getStyleClass().add("muted-copy");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("muted-copy");

        HBox row = new HBox(8, dot, label, spacer, countLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try {
            Map<Integer, String> categoryNames = new HashMap<>();
            CategoryDAO categoryDAO = new CategoryDAO();
            for (Category category : categoryDAO.readAll()) {
                categoryNames.put(category.getCategoryId(), category.getCategoryName());
            }

            BookController bookController = new BookController();
            for (Book book : bookController.getAllBooks()) {
                String category = categoryNames.getOrDefault(book.getCategoryId(), "Other");
                counts.put(category, counts.getOrDefault(category, 0) + 1);
            }
        } catch (Exception ignored) {
            counts.put("Other", 1);
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());
        Map<String, Integer> ordered = new LinkedHashMap<>();
        int limit = Math.min(5, sorted.size());
        for (int i = 0; i < limit; i++) {
            ordered.put(sorted.get(i).getKey(), sorted.get(i).getValue());
        }
        return ordered;
    }

    private String resolveCustomerName(int customerOrUserId) {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            Integer userId = customerDAO.readUserIdByCustomerId(customerOrUserId);
            User user = userId == null ? new UserDAO().read(customerOrUserId) : new UserDAO().read(userId);
            if (user != null) {
                String fullName = (user.getFirstName() == null ? "" : user.getFirstName()) + " "
                    + (user.getLastName() == null ? "" : user.getLastName());
                fullName = fullName.trim();
                if (!fullName.isEmpty()) {
                    return fullName;
                }
            }
        } catch (Exception ignored) {
            // Fallback below.
        }
        return "Customer #" + customerOrUserId;
    }

    private String statusLabel(int statusId) {
        return OrderStatusUtil.label(statusId);
    }

    private Map<String, Double> withRecentMonths(Map<String, Double> raw) {
        LinkedHashMap<String, Double> out = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            String key = now.minusMonths(i).format(fmt);
            out.put(key, raw.getOrDefault(key, 0.0));
        }
        return out;
    }

    private void applyLineSeriesPalette(LineChart<String, Number> chart, String color) {
        Platform.runLater(() -> {
            var line = chart.lookup(".default-color0.chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            }
            for (var symbol : chart.lookupAll(".default-color0.chart-line-symbol")) {
                symbol.setStyle("-fx-background-color: " + color + ", white;");
            }
        });
    }

}
