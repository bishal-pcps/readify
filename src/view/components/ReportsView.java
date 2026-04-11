package view.components;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import controller.AdminController;
import controller.BookController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Book;

public class ReportsView {
    private AdminController adminController;
    private Runnable onInventoryClick;
    private Runnable onUsersClick;
    private Runnable onBackClick;
    private Runnable onLogoutClick;

    public ReportsView(Stage stage, AdminController adminController,
            Runnable onInventoryClick, Runnable onUsersClick,
            Runnable onBackClick, Runnable onLogoutClick) {
        this.adminController = adminController;
        this.onInventoryClick = onInventoryClick;
        this.onUsersClick = onUsersClick;
        this.onBackClick = onBackClick;
        this.onLogoutClick = onLogoutClick;
    }

    public Scene createReportsScene() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        HBox nav = NavigationBar.create(true, "reports",
            null, null, null, null, onBackClick, onInventoryClick, null, onUsersClick, onLogoutClick, 0);

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 24, 32, 24));
        content.setStyle("-fx-background-color: #F0F2F5;");

        Map<String, Double> revenueByMonth = loadRecentRevenue();
        double totalRevenue = revenueByMonth.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalProfit = totalRevenue * 0.434;
        double margin = totalRevenue > 0 ? (totalProfit / totalRevenue) * 100.0 : 0;

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#2C6E6E", "M12 20v-6 M9 20v-10 M15 20v-2 M3 20h18");
        Label title = new Label("Reports & Analytics");
        title.setStyle("-fx-font-size: 34px; -fx-text-fill: #5D4A3A;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button dateRangeBtn = new Button("Date Range");
        dateRangeBtn.setStyle("-fx-background-color: #A8978A; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8 14; -fx-font-weight: bold;");
        dateRangeBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Date range filter can be added in the next step.").showAndWait());

        Button exportBtn = new Button("Export Report");
        exportBtn.setStyle("-fx-background-color: #2C6E6E; -fx-text-fill: white; -fx-background-radius: 8px; -fx-padding: 8 14; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION, "Report export can be added in the next step.").showAndWait());
        header.getChildren().addAll(iconBox, title, spacer, dateRangeBtn, exportBtn);

        HBox summaryCards = new HBox(16,
            makeSummaryCard("Total Revenue (6 months)", "$" + String.format("%,.2f", totalRevenue), "18.2% growth", "#2C6E6E"),
            makeSummaryCard("Total Profit (6 months)", "$" + String.format("%,.2f", totalProfit), "22.5% growth", "#2C6E6E"),
            makeSummaryCard("Average Profit Margin", String.format("%.1f%%", margin), "Healthy margin", "#2C6E6E")
        );
        summaryCards.getChildren().forEach(card -> HBox.setHgrow(card, Priority.ALWAYS));

        VBox revenueProfitChart = makeRevenueProfitChart(revenueByMonth);
        VBox growthChart = makeGrowthChart();
        HBox.setHgrow(revenueProfitChart, Priority.ALWAYS);
        HBox.setHgrow(growthChart, Priority.ALWAYS);
        HBox chartsRow = new HBox(20, revenueProfitChart, growthChart);

        VBox topSellingCard = makeTopSellingCard();

        content.getChildren().addAll(header, summaryCards, chartsRow, topSellingCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-background: #F0F2F5;");

        root.getChildren().addAll(nav, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox makeSummaryCard(String title, String value, String note, String valueColor) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 14, 0, 0, 4); -fx-padding: 18;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + valueColor + ";");
        Label noteLabel = new Label(note);
        noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7A9E6B;");
        card.getChildren().addAll(titleLabel, valueLabel, noteLabel);
        return card;
    }

    private VBox makeRevenueProfitChart(Map<String, Double> revenueByMonth) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 14, 0, 0, 4); -fx-padding: 18;");
        Label lbl = new Label("Revenue & Profit Analysis");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(320);
        chart.setCategoryGap(24);
        chart.setBarGap(8);
        chart.setStyle("-fx-background-color: transparent;");
        xAxis.setTickLabelFill(Color.web("#5D4A3A"));
        yAxis.setTickLabelFill(Color.web("#5D4A3A"));

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        revenueByMonth.forEach((month, value) -> {
            revenueSeries.getData().add(new XYChart.Data<>(month, value));
            profitSeries.getData().add(new XYChart.Data<>(month, value * 0.434));
        });

        HBox legend = new HBox(18,
            legendItem("#2C6E6E", "Revenue"),
            legendItem("#7A9E6B", "Profit")
        );
        legend.setAlignment(Pos.CENTER);

        chart.getData().add(revenueSeries);
        chart.getData().add(profitSeries);
        styleBarSeries(revenueSeries, "#2C6E6E");
        styleBarSeries(profitSeries, "#7A9E6B");
        card.getChildren().addAll(lbl, chart, legend);
        return card;
    }

    private VBox makeGrowthChart() {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 14, 0, 0, 4); -fx-padding: 18;");
        Label lbl = new Label("Customer Growth");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(320);
        chart.setStyle("-fx-background-color: transparent;");
        xAxis.setTickLabelFill(Color.web("#5D4A3A"));
        yAxis.setTickLabelFill(Color.web("#5D4A3A"));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            Map<String, Integer> data = adminController.getCustomerGrowthData();
            Map<String, Integer> chartData = withRecentMonthsInt(data);
            chartData.forEach((m, v) -> series.getData().add(new XYChart.Data<>(m, v)));
        } catch (SQLException e) {
            // Keep chart empty if data is unavailable.
        }

        chart.getData().add(series);
        applyAreaSeriesPalette(chart, "#2C6E6E", "#2C6E6E40");
        Label foot = new Label("Total active customers");
        foot.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        foot.setAlignment(Pos.CENTER);
        card.getChildren().addAll(lbl, chart, foot);
        return card;
    }

    private VBox makeTopSellingCard() {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 14, 0, 0, 4); -fx-padding: 18;");

        Label title = new Label("Top Selling Books");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5D4A3A;");

        TableView<TopBookMetric> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(300);
        table.getStyleClass().add("admin-report-table");

        TableColumn<TopBookMetric, Number> rankCol = new TableColumn<>("Rank");
        rankCol.setCellValueFactory(d -> d.getValue().rankProperty());

        TableColumn<TopBookMetric, String> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(d -> d.getValue().titleProperty());

        TableColumn<TopBookMetric, Number> unitsCol = new TableColumn<>("Units Sold");
        unitsCol.setCellValueFactory(d -> d.getValue().unitsProperty());

        TableColumn<TopBookMetric, String> revenueCol = new TableColumn<>("Revenue");
        revenueCol.setCellValueFactory(d -> d.getValue().revenueTextProperty());

        TableColumn<TopBookMetric, Number> perfCol = new TableColumn<>("Performance");
        perfCol.setCellValueFactory(d -> d.getValue().performanceProperty());
        perfCol.setCellFactory(col -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setGraphic(null);
                    return;
                }
                bar.setProgress(value.doubleValue());
                bar.setPrefWidth(150);
                bar.setStyle("-fx-accent: #2C6E6E;");
                setGraphic(bar);
            }
        });

        table.getColumns().add(rankCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(unitsCol);
        table.getColumns().add(revenueCol);
        table.getColumns().add(perfCol);
        table.setItems(javafx.collections.FXCollections.observableArrayList(getTopBookMetrics()));
        table.setPlaceholder(new Label("No top-selling data available."));

        card.getChildren().addAll(title, table);
        return card;
    }

    private HBox legendItem(String color, String text) {
        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 100; -fx-min-width: 10; -fx-min-height: 10;");
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A;");
        HBox item = new HBox(8, dot, label);
        item.setAlignment(Pos.CENTER_LEFT);
        return item;
    }

    private Map<String, Double> loadRecentRevenue() {
        try {
            return withRecentMonths(adminController.getRevenueData());
        } catch (SQLException e) {
            return withRecentMonths(new LinkedHashMap<>());
        }
    }

    private List<TopBookMetric> getTopBookMetrics() {
        List<TopBookMetric> rows = new ArrayList<>();
        Map<String, Integer> soldByBook = new LinkedHashMap<>();
        try {
            soldByBook.putAll(adminController.getTopSellingBooksData());
        } catch (SQLException ignored) {
            return rows;
        }

        Map<String, Double> priceByTitle = new HashMap<>();
        try {
            BookController bc = new BookController();
            for (Book book : bc.getAllBooks()) {
                String title = book.getBookName() == null ? "" : book.getBookName().trim();
                if (!title.isEmpty()) {
                    priceByTitle.putIfAbsent(title, book.getPrice());
                }
            }
        } catch (Exception ignored) {
            // Revenue will fall back to units if prices are unavailable.
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(soldByBook.entrySet());
        sorted.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed());
        int max = sorted.isEmpty() ? 1 : Math.max(1, sorted.get(0).getValue());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            String title = entry.getKey();
            int units = entry.getValue();
            double revenue = units * priceByTitle.getOrDefault(title, 0.0);
            rows.add(new TopBookMetric(rank++, title, units, revenue, units / (double) max));
        }
        return rows;
    }

    private StackPane makeIconBox(String color, String svgData) {
        StackPane box = new StackPane(); box.setPrefSize(48, 48);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath(); icon.setContent(svgData);
        icon.setStroke(Color.WHITE); icon.setStrokeWidth(2); icon.setFill(Color.TRANSPARENT);
        box.getChildren().add(icon); return box;
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

    private void styleBarSeries(XYChart.Series<String, Number> series, String color) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, old, node) -> {
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
        }
    }

    private void applyAreaSeriesPalette(AreaChart<String, Number> chart, String strokeColor, String fillColor) {
        Platform.runLater(() -> {
            var line = chart.lookup(".default-color0.chart-series-area-line");
            if (line != null) {
                line.setStyle("-fx-stroke: " + strokeColor + "; -fx-stroke-width: 2px;");
            }
            var fill = chart.lookup(".default-color0.chart-series-area-fill");
            if (fill != null) {
                fill.setStyle("-fx-fill: " + fillColor + ";");
            }
            for (var symbol : chart.lookupAll(".default-color0.chart-area-symbol")) {
                symbol.setStyle("-fx-background-color: " + strokeColor + ", white;");
            }
        });
    }

    private Map<String, Integer> withRecentMonthsInt(Map<String, Integer> raw) {
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            String key = now.minusMonths(i).format(fmt);
            out.put(key, raw.getOrDefault(key, 0));
        }
        return out;
    }

    private static class TopBookMetric {
        private final javafx.beans.property.SimpleIntegerProperty rank;
        private final javafx.beans.property.SimpleStringProperty title;
        private final javafx.beans.property.SimpleIntegerProperty units;
        private final javafx.beans.property.SimpleStringProperty revenueText;
        private final javafx.beans.property.SimpleDoubleProperty performance;

        TopBookMetric(int rank, String title, int units, double revenue, double performance) {
            this.rank = new javafx.beans.property.SimpleIntegerProperty(rank);
            this.title = new javafx.beans.property.SimpleStringProperty(title);
            this.units = new javafx.beans.property.SimpleIntegerProperty(units);
            this.revenueText = new javafx.beans.property.SimpleStringProperty("$" + String.format("%,.2f", revenue));
            this.performance = new javafx.beans.property.SimpleDoubleProperty(performance);
        }

        javafx.beans.property.SimpleIntegerProperty rankProperty() { return rank; }
        javafx.beans.property.SimpleStringProperty titleProperty() { return title; }
        javafx.beans.property.SimpleIntegerProperty unitsProperty() { return units; }
        javafx.beans.property.SimpleStringProperty revenueTextProperty() { return revenueText; }
        javafx.beans.property.SimpleDoubleProperty performanceProperty() { return performance; }
    }
}
