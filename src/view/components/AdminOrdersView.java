package view.components;

import java.util.List;

import controller.OrderController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Order;
import util.OrderStatusUtil;

/**
 * Modernized Admin Orders Management View.
 * KPI Cards for Status tracking, Table with status badges and action buttons.
 */
public class AdminOrdersView {
    private OrderController orderController;
    private Runnable onInventoryClick;
    private Runnable onReportsClick;
    private Runnable onUsersClick;
    private Runnable onBackClick;
    private Runnable onLogoutClick;
    private controller.BookController bookController;
    private java.util.Map<Integer, String> orderItemsCache;
    private ObservableList<Order> orderData;

    private Label totalOrdersLabel, pendingLabel, shippedLabel, cancelledLabel;

    public AdminOrdersView(Stage stage, OrderController orderController,
            Runnable onInventoryClick, Runnable onReportsClick,
            Runnable onUsersClick, Runnable onBackClick, Runnable onLogoutClick) {
        this.orderController = orderController;
        this.onInventoryClick = onInventoryClick;
        this.onReportsClick = onReportsClick;
        this.onUsersClick = onUsersClick;
        this.onBackClick = onBackClick;
        this.onLogoutClick = onLogoutClick;
        this.orderItemsCache = new java.util.HashMap<>();
        try {
            this.bookController = new controller.BookController();
        } catch (Exception ignored) {
            this.bookController = null;
        }
    }

    public Scene createOrdersScene() throws Exception {
        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-admin");

        // Nav
        HBox nav = NavigationBar.create(true, "dashboard",
            null, null, null, null,
            onBackClick, onInventoryClick, onReportsClick, onUsersClick, onLogoutClick, 0);

        // Body
        VBox body = new VBox(28);
        body.setPadding(new Insets(32, 48, 40, 48));
        body.getStyleClass().add("page-content");

        // Header
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#2C6E6E", "M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z M3 6h18 M16 10a4 4 0 0 1-8 0");
        Label titleLabel = new Label("Orders Management");
        titleLabel.getStyleClass().add("page-title-sm");
        headerBox.getChildren().addAll(iconBox, titleLabel);

        // Stats
        HBox statsBox = new HBox(20);
        String ordSvg = "M16 6V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H2v13c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6h-6zm-6-2h4v2h-4V4zm10 15H4V8h16v11z";
        String pendSvg = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 14H11V7h2v9z";
        String shipSvg = "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z";
        String cancSvg = "M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10 10-4.47 10-10S17.53 2 12 2zm5 13.59L15.59 17 12 13.41 8.41 17 7 15.59 10.59 12 7 8.41 8.41 7 12 10.59 15.59 7 17 8.41 13.41 12 17 15.59z";

        VBox s1 = makeStatCard("Total Orders", "0", "#5A6F8C", ordSvg);
        totalOrdersLabel = (Label) s1.lookup(".value-label");
        VBox s2 = makeStatCard("Pending", "0", "#E6B422", pendSvg);
        pendingLabel = (Label) s2.lookup(".value-label");
        VBox s3 = makeStatCard("Shipped", "0", "#2C6E6E", shipSvg);
        shippedLabel = (Label) s3.lookup(".value-label");
        VBox s4 = makeStatCard("Cancelled", "0", "#B14A3A", cancSvg);
        cancelledLabel = (Label) s4.lookup(".value-label");

        HBox.setHgrow(s1, Priority.ALWAYS); HBox.setHgrow(s2, Priority.ALWAYS);
        HBox.setHgrow(s3, Priority.ALWAYS); HBox.setHgrow(s4, Priority.ALWAYS);
        statsBox.getChildren().addAll(s1, s2, s3, s4);

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Order ID or Customer ID...");
        searchField.setPrefHeight(46);
        searchField.getStyleClass().add("search-field");

        HBox selectedActions = new HBox(12);
        selectedActions.setAlignment(Pos.CENTER_LEFT);
        selectedActions.setPadding(new Insets(16, 20, 16, 20));
        selectedActions.getStyleClass().add("surface-card-compact");
        selectedActions.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(42, 30, 20, 0.05), 10, 0, 0, 4);");

        Label actionsTitle = new Label("Bulk Actions:");
        actionsTitle.getStyleClass().add("form-label");
        actionsTitle.setStyle("-fx-text-fill: #5A6F8C; -fx-font-size: 14px;");

        Button viewSelectedBtn = new Button("View Order");
        viewSelectedBtn.getStyleClass().add("button-secondary");

        Button shipSelectedBtn = new Button("Mark Shipped");
        shipSelectedBtn.getStyleClass().add("button-soft");
        shipSelectedBtn.setStyle("-fx-background-color: rgba(122, 158, 107, 0.15); -fx-text-fill: #5c7e4e; -fx-font-weight: 800;");

        Button completeSelectedBtn = new Button("Complete");
        completeSelectedBtn.getStyleClass().add("button-soft");
        completeSelectedBtn.setStyle("-fx-background-color: rgba(44, 110, 110, 0.15); -fx-text-fill: #2C6E6E; -fx-font-weight: 800;");

        Button deleteSelectedBtn = new Button("Delete");
        deleteSelectedBtn.getStyleClass().add("button-soft");
        deleteSelectedBtn.setStyle("-fx-background-color: rgba(177, 74, 58, 0.15); -fx-text-fill: #B14A3A; -fx-font-weight: 800;");

        selectedActions.getChildren().addAll(actionsTitle, viewSelectedBtn, shipSelectedBtn, completeSelectedBtn, deleteSelectedBtn);

        Label actionsHint = new Label("Tip: Select a row and complete its workflow above, or double-click any row to open receipt.");
        actionsHint.getStyleClass().add("muted-copy");

        // Table
        VBox tableCard = new VBox();
        tableCard.getStyleClass().add("surface-card");
        TableView<Order> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(400);

        TableColumn<Order, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(p -> new SimpleStringProperty("#ORD-" + p.getValue().getOrderId()));
        idCol.setPrefWidth(120);

        TableColumn<Order, Integer> userCol = new TableColumn<>("Customer ID");
        userCol.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getUserId()));
        userCol.setPrefWidth(120);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Total Items/Books Bought");
        itemsCol.setCellValueFactory(d -> new SimpleStringProperty(getOrderItemsSummary(d.getValue().getOrderId())));
        itemsCol.setPrefWidth(340);

        TableColumn<Order, String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(p -> new SimpleStringProperty("$" + String.format("%.2f", p.getValue().getTotalAmount())));
        amtCol.setStyle("-fx-text-fill: #2C6E6E; -fx-font-weight: bold;");
        amtCol.setPrefWidth(120);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(p -> new SimpleStringProperty(getStatusText(p.getValue().getOrderStatusId())));
        statusCol.setPrefWidth(140);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label badge = new Label(item);
                Order rowOrder = getTableRow() == null ? null : getTableRow().getItem();
                int statusId = rowOrder == null ? -1 : rowOrder.getOrderStatusId();
                String color = OrderStatusUtil.color(statusId);
                badge.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + ";" +
                    "-fx-background-radius: 20px; -fx-padding: 3 12; -fx-font-size: 12px; -fx-font-weight: bold;");
                setGraphic(badge);
            }
        });

        TableColumn<Order, Void> actCol = new TableColumn<>("Quick Actions");
        actCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
        actCol.setCellFactory(p -> createActionCell());
        actCol.setMinWidth(260);
        actCol.setPrefWidth(280);

        table.getColumns().add(idCol);
        table.getColumns().add(userCol);
        table.getColumns().add(actCol);
        table.getColumns().add(itemsCol);
        table.getColumns().add(amtCol);
        table.getColumns().add(statusCol);

        orderData = FXCollections.observableArrayList();
        loadData(orderData);

        FilteredList<Order> filtered = new FilteredList<>(orderData, p -> true);
        searchField.textProperty().addListener((obs, ov, nv) -> {
            filtered.setPredicate(o -> {
                if (nv == null || nv.isEmpty()) {
					return true;
				}
                String q = nv.toLowerCase();
                return String.valueOf(o.getOrderId()).contains(q) || String.valueOf(o.getUserId()).contains(q);
            });
        });

        table.setItems(filtered);
        table.setPlaceholder(new Label("No orders found. Create an order from customer checkout, then refresh this page."));

        viewSelectedBtn.setDisable(true);
        shipSelectedBtn.setDisable(true);
        completeSelectedBtn.setDisable(true);
        deleteSelectedBtn.setDisable(true);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, selected) -> {
            viewSelectedBtn.setDisable(selected == null);
            deleteSelectedBtn.setDisable(selected == null);
            shipSelectedBtn.setDisable(selected == null || !OrderStatusUtil.isPending(selected.getOrderStatusId()));
            completeSelectedBtn.setDisable(selected == null || selected.getOrderStatusId() != OrderStatusUtil.SHIPPED);
        });

        viewSelectedBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Select Order");
                info.setHeaderText(null);
                info.setContentText("Please select an order row first to view details.");
                info.showAndWait();
                return;
            }
            showOrderDetailsDialog(selected, table);
        });
        shipSelectedBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Select Order");
                info.setHeaderText(null);
                info.setContentText("Please select a pending order row first.");
                info.showAndWait();
                return;
            }
            handleShipOrder(selected, table);
        });
        completeSelectedBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Select Order");
                info.setHeaderText(null);
                info.setContentText("Please select a shipped order row first.");
                info.showAndWait();
                return;
            }
            handleCompleteOrder(selected, table);
        });
        deleteSelectedBtn.setOnAction(e -> {
            Order selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Select Order");
                info.setHeaderText(null);
                info.setContentText("Please select an order row first to delete.");
                info.showAndWait();
                return;
            }
            handleDeleteOrder(selected, table);
        });

        table.setRowFactory(tv -> {
            TableRow<Order> row = new TableRow<>();
            row.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                    showOrderDetailsDialog(row.getItem(), table);
                }
            });
            return row;
        });

        tableCard.getChildren().add(table);

        body.getChildren().addAll(headerBox, statsBox, searchField, selectedActions, actionsHint, tableCard);

        ScrollPane sp = new ScrollPane(body);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, sp);
        VBox.setVgrow(sp, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private void loadData(ObservableList<Order> list) {
        try {
            list.clear();
            orderItemsCache.clear();
            List<Order> orders = orderController.getAllOrders();
            list.addAll(orders);
            updateStats(orders);
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Load Orders");
            a.setHeaderText(null);
            a.setContentText("Failed to load orders: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private String getOrderItemsSummary(int orderId) {
        String cached = orderItemsCache.get(orderId);
        if (cached != null) {
            return cached;
        }

        String summary = "N/A";
        try {
            List<model.LineItem> items = orderController.getOrderDetails(orderId);
            StringBuilder sb = new StringBuilder();
            for (model.LineItem li : items) {
                String bookName = "Book #" + li.getBookId();
                if (bookController != null) {
                    model.Book book = bookController.getBook(li.getBookId());
                    if (book != null && book.getBookName() != null && !book.getBookName().isBlank()) {
                        bookName = book.getBookName();
                    }
                }
                sb.append(li.getQuantity()).append("x ").append(bookName).append(", ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2);
            }
            summary = sb.length() > 0 ? sb.toString() : "N/A";
        } catch (Exception ex) {
            System.err.println("Failed to resolve order items for order " + orderId + ": " + ex.getMessage());
            summary = "Unavailable";
        }

        orderItemsCache.put(orderId, summary);
        return summary;
    }

    private void updateStats(List<Order> orders) {
        int total = orders.size(), p = 0, s = 0, c = 0;
        for (Order o : orders) {
            if (o.getOrderStatusId() == OrderStatusUtil.PENDING) {
				p++;
            } else if (o.getOrderStatusId() == OrderStatusUtil.SHIPPED) {
				s++;
            } else if (o.getOrderStatusId() == OrderStatusUtil.CANCELLED) {
				c++; // using 3 for cancelled in this mapping
			}
        }
        totalOrdersLabel.setText(String.valueOf(total));
        pendingLabel.setText(String.valueOf(p));
        shippedLabel.setText(String.valueOf(s));
        cancelledLabel.setText(String.valueOf(c));
    }

    private TableCell<Order, Void> createActionCell() {
        return new TableCell<>() {
            private final Button shipBtn = new Button("Ship");
            private final Button completeBtn = new Button("Complete");
            private final Button deleteBtn = new Button("Delete");
            private final Button viewBtn = new Button("View");
            {
                shipBtn.setStyle(actionStyle("#7A9E6B"));
                completeBtn.setStyle(actionStyle("#2C6E6E"));
                deleteBtn.setStyle(actionStyle("#B14A3A"));
                viewBtn.setStyle(actionStyle("#5A6F8C"));

                shipBtn.setOnAction(e -> {
                    Order selected = getOrderAt(this);
                    if (selected != null) {
                        handleShipOrder(selected, getTableView());
                    }
                });

                completeBtn.setOnAction(e -> {
                    Order selected = getOrderAt(this);
                    if (selected != null) {
                        handleCompleteOrder(selected, getTableView());
                    }
                });

                deleteBtn.setOnAction(e -> {
                    Order selected = getOrderAt(this);
                    if (selected != null) {
                        handleDeleteOrder(selected, getTableView());
                    }
                });

                viewBtn.setOnAction(e -> {
                    Order selected = getOrderAt(this);
                    if (selected != null) {
                        showOrderDetailsDialog(selected, getTableView());
                    }
                });
            }
            @Override protected void updateItem(Void it, boolean emp) {
                super.updateItem(it, emp);
                if (emp) {
                    setGraphic(null);
                } else {
                    Order rowOrder = getOrderAt(this);
                    if (rowOrder == null) {
                        setGraphic(null);
                        return;
                    }
                    HBox actions = new HBox(8, viewBtn, deleteBtn);
                    if (OrderStatusUtil.isPending(rowOrder.getOrderStatusId())) {
                        actions.getChildren().add(shipBtn);
                    }
                    if (rowOrder.getOrderStatusId() == OrderStatusUtil.SHIPPED) {
                        actions.getChildren().add(completeBtn);
                    }
                    setGraphic(actions);
				}
            }
        };
    }

    private Order getOrderAt(TableCell<Order, Void> cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getTableRow() != null && cell.getTableRow().getItem() != null) {
            return cell.getTableRow().getItem();
        }

        if (cell.getTableView() == null) {
            return null;
        }

        int index = cell.getIndex();
        if (index < 0 || index >= cell.getTableView().getItems().size()) {
            return null;
        }
        return cell.getTableView().getItems().get(index);
    }

    private void handleShipOrder(Order order, TableView<Order> tableView) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Ship Order");
        confirm.setHeaderText("Ship Order #ORD-" + order.getOrderId() + "?");
        confirm.setContentText("This will ship the order and deduct stock from inventory.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            orderController.shipOrder(order.getOrderId());
            loadData(orderData);
            if (tableView != null) {
                tableView.refresh();
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Order Shipped");
            success.setHeaderText(null);
            success.setContentText("Order #ORD-" + order.getOrderId() + " has been shipped successfully.");
            success.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Ship Order");
            a.setHeaderText(null);
            a.setContentText("Failed to ship order: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private void handleCompleteOrder(Order order, TableView<Order> tableView) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Complete Order");
        confirm.setHeaderText("Complete Order #ORD-" + order.getOrderId() + "?");
        confirm.setContentText("This will mark the order as fully delivered and completed.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            orderController.updateOrderStatus(order.getOrderId(), OrderStatusUtil.COMPLETED);
            loadData(orderData);
            if (tableView != null) {
                tableView.refresh();
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Order Completed");
            success.setHeaderText(null);
            success.setContentText("Order #ORD-" + order.getOrderId() + " has been marked as completed successfully.");
            success.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Complete Order");
            a.setHeaderText(null);
            a.setContentText("Failed to complete order: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private void handleDeleteOrder(Order order, TableView<Order> tableView) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Order");
        confirm.setHeaderText("Delete Order #ORD-" + order.getOrderId() + "?");
        confirm.setContentText("This will permanently remove the order and its line items.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            orderController.deleteOrder(order.getOrderId());
            loadData(orderData);
            if (tableView != null) {
                tableView.refresh();
            }

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Order Deleted");
            success.setHeaderText(null);
            success.setContentText("Order #ORD-" + order.getOrderId() + " has been deleted.");
            success.showAndWait();
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Delete Order");
            a.setHeaderText(null);
            a.setContentText("Failed to delete order: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private void showOrderDetailsDialog(Order order, TableView<Order> tableView) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Order Details");
            String dateText = order.getOrderDate() == null
                ? "N/A"
                : order.getOrderDate().toString().substring(0, Math.min(16, order.getOrderDate().toString().length()));
            dialog.setHeaderText("Order #" + order.getOrderId() + " - " + dateText);

            VBox content = new VBox(20);
            content.setPadding(new Insets(20));
            content.getStyleClass().add("page-content");

            Label statusLbl = new Label("Status: " + getStatusText(order.getOrderStatusId()));
            statusLbl.getStyleClass().add("muted-copy");

            Label amtLbl = new Label("Total Amount: $" + String.format("%.2f", order.getTotalAmount()));
            amtLbl.getStyleClass().add("section-heading");
            amtLbl.setStyle("-fx-font-size: 14px;");

            Label addrLbl = new Label("Shipping Address:\n" + (order.getShippingAddress() == null ? "N/A" : order.getShippingAddress()));
            addrLbl.setWrapText(true);
            addrLbl.getStyleClass().add("muted-copy");

            TableView<model.LineItem> table = new TableView<>();
            table.setPrefHeight(200);

            TableColumn<model.LineItem, String> itemsCol = new TableColumn<>("Item");
            itemsCol.setCellValueFactory(d -> {
                try {
                    model.Book b = bookController != null ? bookController.getBook(d.getValue().getBookId()) : null;
                    return new SimpleStringProperty(b != null ? b.getBookName() : "Unknown");
                } catch (Exception ex) {
                    return new SimpleStringProperty("N/A");
                }
            });
            itemsCol.setPrefWidth(200);

            TableColumn<model.LineItem, Integer> qtyCol = new TableColumn<>("Qty");
            qtyCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getQuantity()));

            TableColumn<model.LineItem, String> pCol = new TableColumn<>("Subtotal");
            pCol.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().getPrice() * d.getValue().getQuantity())));

            table.getColumns().add(itemsCol);
            table.getColumns().add(qtyCol);
            table.getColumns().add(pCol);
            table.setItems(FXCollections.observableArrayList(orderController.getOrderDetails(order.getOrderId())));

            content.getChildren().addAll(statusLbl, amtLbl, addrLbl, table);
            dialog.getDialogPane().setContent(content);

            ButtonType shipType = new ButtonType("Ship Order", ButtonBar.ButtonData.OK_DONE);
            ButtonType completeType = new ButtonType("Complete Order", ButtonBar.ButtonData.OK_DONE);
            ButtonType deleteType = new ButtonType("Delete Order", ButtonBar.ButtonData.OTHER);
            
            if (OrderStatusUtil.isPending(order.getOrderStatusId())) {
                dialog.getDialogPane().getButtonTypes().add(shipType);
            } else if (order.getOrderStatusId() == OrderStatusUtil.SHIPPED) {
                dialog.getDialogPane().getButtonTypes().add(completeType);
            }
            dialog.getDialogPane().getButtonTypes().add(deleteType);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            ButtonType selected = dialog.showAndWait().orElse(ButtonType.CLOSE);
            if (selected == shipType) {
                handleShipOrder(order, tableView);
            } else if (selected == completeType) {
                handleCompleteOrder(order, tableView);
            } else if (selected == deleteType) {
                handleDeleteOrder(order, tableView);
            }
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Order Details");
            a.setHeaderText(null);
            a.setContentText("Failed to load order details: " + ex.getMessage());
            a.showAndWait();
        }
    }

    private String getStatusText(int id) {
        return OrderStatusUtil.label(id);
    }

    private String actionStyle(String color) {
         return "-fx-background-color: " + color + "1a; -fx-text-fill: " + color + ";" +
             "-fx-background-radius: 8px; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12px; -fx-font-weight: 900;" +
             "-fx-border-color: " + color + "33; -fx-border-radius: 8px;";
    }

    private VBox makeStatCard(String title, String value, String valueColor, String svgData) {
        VBox card = new VBox(12);
        card.getStyleClass().add("kpi-card");
        
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("muted-copy");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #6b5c50;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        SVGPath icon = new SVGPath();
        icon.setContent(svgData);
        icon.setFill(javafx.scene.paint.Color.web(valueColor));
        icon.setScaleX(0.85); icon.setScaleY(0.85);
        
        StackPane iconBg = new StackPane(icon);
        iconBg.setStyle("-fx-background-color: " + valueColor + "15; -fx-background-radius: 10px;");
        iconBg.setPrefSize(38, 38);
        iconBg.setMinSize(38, 38);
        
        topRow.getChildren().addAll(titleLabel, spacer, iconBg);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("value-label");
        valueLabel.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 34px; -fx-font-weight: 900;");

        card.getChildren().addAll(topRow, valueLabel);
        
        // Hover Action
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-effect: dropshadow(three-pass-box, " + valueColor + "35, 24, 0, 0, 10); -fx-translate-y: -3px; " +
                          "-fx-background-color: white; -fx-background-radius: 14px; -fx-border-color: " + valueColor + "20; -fx-border-radius: 14px;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(33, 25, 19, 0.08), 18, 0, 0, 6); -fx-translate-y: 0px; " +
                          "-fx-background-color: -readify-surface; -fx-background-radius: 14px; -fx-border-color: rgba(90, 70, 56, 0.1); -fx-border-radius: 14px;");
        });
        
        return card;
    }

    private StackPane makeIconBox(String color, String svg) {
        StackPane box = new StackPane(); box.setPrefSize(42, 42); 
        box.setStyle("-fx-background-color: " + color + "1a; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath(); icon.setContent(svg);
        icon.setFill(Color.web(color));
        box.getChildren().add(icon); return box;
    }

}
