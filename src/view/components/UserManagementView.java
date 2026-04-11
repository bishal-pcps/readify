package view.components;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import controller.AuthController;
import controller.OrderController;
import dao.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Order;
import model.User;

public class UserManagementView {
    private static final Pattern SEARCH_INPUT_PATTERN = Pattern.compile("[a-zA-Z0-9@._+\\-\\s]*");

    private Runnable onInventoryClick;
    private Runnable onReportsClick;
    private Runnable onBackClick;
    private Runnable onLogoutClick;

    public UserManagementView(Stage stage, AuthController authController,
            Runnable onInventoryClick, Runnable onReportsClick,
            Runnable onBackClick, Runnable onLogoutClick) {
        this.onInventoryClick = onInventoryClick;
        this.onReportsClick = onReportsClick;
        this.onBackClick = onBackClick;
        this.onLogoutClick = onLogoutClick;
    }

    public Scene createUserManagementScene() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        HBox nav = NavigationBar.create(true, "users",
            null, null, null, null, onBackClick, onInventoryClick, onReportsClick, null, onLogoutClick, 0);

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 24, 32, 24));
        content.setStyle("-fx-background-color: #F0F2F5;");

        // Header
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#A8978A", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 7a4 4 0 1 0 0-8 4 4 0 0 0 0 8 M22 21v-2a4 4 0 0 0-3-3.87 M16 3.13a4 4 0 0 1 0 7.75");
        Label title = new Label("User Management");
        title.setStyle("-fx-font-size: 34px; -fx-text-fill: #5D4A3A;");
        header.getChildren().addAll(iconBox, title);

        ObservableList<User> allUsers = FXCollections.observableArrayList();
        Map<Integer, ActivitySummary> activityByUser = new HashMap<>();

        Label totalUsersValue = makeStatValueLabel("0", "#A8978A");
        Label customerUsersValue = makeStatValueLabel("0", "#7A9E6B");
        Label suspendedUsersValue = makeStatValueLabel("N/A", "#B14A3A");
        Label staffUsersValue = makeStatValueLabel("0", "#5A6F8C");

        HBox statsRow = new HBox(16);
        VBox s1 = makeStatCard("Total Users", totalUsersValue);
        VBox s2 = makeStatCard("Customers", customerUsersValue);
        VBox s3 = makeStatCard("Suspended", suspendedUsersValue);
        VBox s4 = makeStatCard("Staff Members", staffUsersValue);
        HBox.setHgrow(s1,Priority.ALWAYS); HBox.setHgrow(s2,Priority.ALWAYS);
        HBox.setHgrow(s3,Priority.ALWAYS); HBox.setHgrow(s4,Priority.ALWAYS);
        statsRow.getChildren().addAll(s1, s2, s3, s4);

        allUsers.addListener((ListChangeListener<User>) change ->
            refreshStats(allUsers, totalUsersValue, customerUsersValue, staffUsersValue));
        refreshStats(allUsers, totalUsersValue, customerUsersValue, staffUsersValue);

        // Search bar
        HBox searchWrap = new HBox(10);
        searchWrap.setPrefHeight(46);
        searchWrap.setAlignment(Pos.CENTER_LEFT);
        searchWrap.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10px;" +
            "-fx-border-color: rgba(93,74,58,0.12); -fx-border-radius: 10px; -fx-border-width: 1px; -fx-padding: 0 14;");
        Label searchIcon = new Label("Search");
        searchIcon.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.5; -fx-font-size: 12px;");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");
        searchField.setStyle("-fx-background-color: transparent; -fx-padding: 10 0; -fx-border-width: 0; -fx-font-size: 14px; -fx-text-fill: #5D4A3A;");
        searchField.setTextFormatter(createValidatedFormatter(80, SEARCH_INPUT_PATTERN));
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchWrap.getChildren().addAll(searchIcon, searchField);

        FilteredList<User> filtered = new FilteredList<>(allUsers, u -> true);
        searchField.textProperty().addListener((obs, ov, nv) -> {
            String q = normalizeInput(nv).toLowerCase();
            filtered.setPredicate(u -> q.isEmpty()
                || getFullName(u).toLowerCase().contains(q)
                || normalizeInput(u.getEmail()).toLowerCase().contains(q)
                || normalizeInput(u.getPhoneNumber()).toLowerCase().contains(q));
        });

        // Table
        VBox tableCard = new VBox(0);
        tableCard.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 14px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 16, 0, 0, 4);");

        TableView<User> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-background-color: transparent;");
        table.getStyleClass().add("admin-users-table");
        table.setItems(filtered);

        TableColumn<User, String> nameCol = new TableColumn<>("User");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(getFullName(d.getValue())));
        nameCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                User user = getUserAt(this);
                if (user == null) {
                    setGraphic(null);
                    return;
                }
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                Label avatar = new Label(name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase());
                avatar.setStyle("-fx-background-color: #A8978A; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20px; -fx-min-width: 40; -fx-min-height: 40;" +
                    "-fx-alignment: center; -fx-padding: 0 12;");
                VBox textBox = new VBox(2);
                Label nameLbl = new Label(name.isBlank() ? "Unknown User" : name);
                nameLbl.setStyle("-fx-text-fill: #5D4A3A; -fx-font-size: 14px;");
                if (user.getLoyaltyPoints() > 3000) {
                    Label gold = new Label("Gold Member");
                    gold.setStyle("-fx-text-fill: #D4AF37; -fx-font-size: 11px;");
                    textBox.getChildren().addAll(nameLbl, gold);
                } else {
                    textBox.getChildren().add(nameLbl);
                }
                row.getChildren().addAll(avatar, textBox);
                setGraphic(row);
            }
        });

        TableColumn<User, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(d -> new SimpleStringProperty("contact"));
        contactCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getUserAt(this);
                if (u == null) {
                    setGraphic(null);
                    return;
                }
                VBox box = new VBox(2);
                Label email = new Label(u.getEmail() == null ? "N/A" : u.getEmail());
                email.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.75; -fx-font-size: 12px;");
                Label phone = new Label(u.getPhoneNumber() == null || u.getPhoneNumber().isBlank() ? "No phone" : u.getPhoneNumber());
                phone.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.55; -fx-font-size: 12px;");
                box.getChildren().addAll(email, phone);
                setGraphic(box);
            }
        });

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getRoleId() == 1 ? "Admin" : "Customer"));
        roleCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setGraphic(null); return; }
                boolean isAdmin = "Admin".equals(role);
                Label badge = new Label(role);
                badge.setStyle("-fx-background-color: " + (isAdmin ? "#B14A3A20" : "#A8978A20") + ";" +
                    "-fx-text-fill: " + (isAdmin ? "#B14A3A" : "#A8978A") + ";" +
                    "-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 12px; -fx-font-weight: bold;" +
                    "-fx-border-color: " + (isAdmin ? "#B14A3A40" : "#A8978A40") + "; -fx-border-radius: 20px;");
                setGraphic(badge);
            }
        });

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty("Not Tracked"));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle("-fx-background-color: #A8978A20; -fx-text-fill: #A8978A;" +
                    "-fx-background-radius: 20px; -fx-padding: 4 12; -fx-font-size: 12px;" +
                    "-fx-border-color: #A8978A40; -fx-border-radius: 20px;");
                setGraphic(badge);
            }
        });

        TableColumn<User, String> joinDateCol = new TableColumn<>("Join Date");
        joinDateCol.setCellValueFactory(d -> {
            if (d.getValue().getCreatedAt() == null) {
                return new SimpleStringProperty("N/A");
            }
            return new SimpleStringProperty(d.getValue().getCreatedAt().toLocalDateTime().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        });
        joinDateCol.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.75;");

        TableColumn<User, String> activityCol = new TableColumn<>("Activity");
        activityCol.setCellValueFactory(d -> new SimpleStringProperty("activity"));
        activityCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                User u = getUserAt(this);
                if (u == null) {
                    setGraphic(null);
                    return;
                }
                ActivitySummary summary = activityByUser.getOrDefault(u.getUserId(), new ActivitySummary(0, 0));
                VBox box = new VBox(2);
                Label orders = new Label(summary.orderCount + " orders");
                orders.setStyle("-fx-text-fill: #5D4A3A; -fx-font-size: 12px;");
                Label spend = new Label("$" + String.format("%.2f", summary.totalSpend));
                spend.setStyle("-fx-text-fill: #2C6E6E; -fx-font-size: 12px;");
                box.getChildren().addAll(orders, spend);
                setGraphic(box);
            }
        });

        TableColumn<User, Void> actCol = new TableColumn<>("Actions");
        actCol.setPrefWidth(180);
        actCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button suspendBtn = new Button("Suspend");
            {
                editBtn.setStyle("-fx-background-color: #A8978A20; -fx-text-fill: #A8978A;" +
                    "-fx-background-radius: 8px; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 12px;" +
                    "-fx-border-color: #A8978A40; -fx-border-radius: 8px;");
                editBtn.setOnAction(e -> {
                    User u = getUserAt(this);
                    if (u != null) {
                        editUserDialog(u, allUsers, activityByUser, table);
                    }
                });

                suspendBtn.setStyle("-fx-background-color: #B14A3A20; -fx-text-fill: #B14A3A;" +
                    "-fx-background-radius: 8px; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 12px;" +
                    "-fx-border-color: #B14A3A40; -fx-border-radius: 8px;");
                suspendBtn.setOnAction(e -> {
                    User u = getUserAt(this);
                    if (u == null) {
                        return;
                    }
                    if (u.getRoleId() == 1) {
                        new Alert(Alert.AlertType.INFORMATION, "Admin accounts cannot be suspended here.").showAndWait();
                    } else {
                        new Alert(Alert.AlertType.INFORMATION, "User suspension is not available in the current schema yet.").showAndWait();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                User u = getUserAt(this);
                if (u == null) {
                    setGraphic(null);
                    return;
                }
                HBox actions = new HBox(6, editBtn);
                if (u.getRoleId() != 1) {
                    actions.getChildren().add(suspendBtn);
                }
                setGraphic(actions);
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(contactCol);
        table.getColumns().add(roleCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(joinDateCol);
        table.getColumns().add(activityCol);
        table.getColumns().add(actCol);
        tableCard.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        Label emptyLabel = new Label("Loading users...");
        emptyLabel.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.7; -fx-padding: 20;");
        table.setPlaceholder(emptyLabel);

        content.getChildren().addAll(header, statsRow, searchWrap, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #F0F2F5; -fx-background: #F0F2F5;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());

        loadUsersAndActivityAsync(allUsers, activityByUser, table, emptyLabel,
            totalUsersValue, customerUsersValue, staffUsersValue);

        return scene;
    }

    private void loadUsersAndActivityAsync(ObservableList<User> allUsers,
            Map<Integer, ActivitySummary> activityByUser,
            TableView<User> table,
            Label placeholderLabel,
            Label totalUsersValue,
            Label customerUsersValue,
            Label staffUsersValue) {
        Task<UserLoadResult> loadTask = new Task<>() {
            @Override
            protected UserLoadResult call() throws Exception {
                UserDAO dao = new UserDAO();
                List<User> users = dao.readAll();
                Map<Integer, ActivitySummary> activity = buildActivityMap(users);
                return new UserLoadResult(users, activity);
            }
        };

        loadTask.setOnSucceeded(e -> {
            UserLoadResult result = loadTask.getValue();
            allUsers.setAll(result.users);
            activityByUser.clear();
            activityByUser.putAll(result.activityByUser);
            refreshStats(allUsers, totalUsersValue, customerUsersValue, staffUsersValue);
            placeholderLabel.setText("No users found matching your search");
            table.refresh();
        });

        loadTask.setOnFailed(e -> {
            Throwable error = loadTask.getException();
            String message = (error == null || error.getMessage() == null || error.getMessage().isBlank())
                ? "Unknown error"
                : error.getMessage();
            refreshStats(allUsers, totalUsersValue, customerUsersValue, staffUsersValue);
            placeholderLabel.setText("No users found matching your search");
            new Alert(Alert.AlertType.ERROR, "Failed to load users: " + message).showAndWait();
        });

        Thread worker = new Thread(loadTask, "user-management-loader");
        worker.setDaemon(true);
        worker.start();
    }

    private Map<Integer, ActivitySummary> buildActivityMap(List<User> users) throws Exception {
        Map<Integer, ActivitySummary> map = new HashMap<>();
        OrderController oc = new OrderController();
        for (User user : users) {
            map.put(user.getUserId(), buildActivitySummary(oc, user));
        }
        return map;
    }

    private ActivitySummary buildActivitySummary(OrderController oc, User user) {
        if (user.getRoleId() == 1) {
            return new ActivitySummary(0, 0);
        }

        int orderCount = 0;
        double spent = 0;
        try {
            List<Order> orders = oc.getCustomerOrders(user.getUserId());
            orderCount = orders.size();
            for (Order order : orders) {
                if (order.getOrderStatusId() != 3) {
                    spent += order.getTotalAmount();
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to load activity for user " + user.getUserId() + ": " + ex.getMessage());
        }
        return new ActivitySummary(orderCount, spent);
    }

    private ActivitySummary loadActivityForUser(User user) {
        try {
            OrderController oc = new OrderController();
            return buildActivitySummary(oc, user);
        } catch (Exception ex) {
            System.err.println("Failed to refresh activity for user " + user.getUserId() + ": " + ex.getMessage());
            return new ActivitySummary(0, 0);
        }
    }

    private void editUserDialog(User user, ObservableList<User> list,
            Map<Integer, ActivitySummary> activityByUser, TableView<User> table) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("User: " + getFullName(user));

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label nameLabel = new Label("Name: " + (getFullName(user).isBlank() ? "N/A" : getFullName(user)));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D4A3A;");

        String emailText = normalizeInput(user.getEmail());
        Label emailLabel = new Label("Email: " + (emailText.isBlank() ? "N/A" : emailText));
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D4A3A;");

        String phoneText = normalizeInput(user.getPhoneNumber());
        Label phoneLabel = new Label("Phone: " + (phoneText.isBlank() ? "N/A" : phoneText));
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #5D4A3A;");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Admin", "Customer");
        roleCombo.setValue(user.getRoleId() == 1 ? "Admin" : "Customer");

        content.getChildren().addAll(
            nameLabel,
            emailLabel,
            phoneLabel,
            new Separator(),
            new Label("Role:"), roleCombo
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            String role = roleCombo.getValue();

            String validationError = validateRoleUpdate(user, role, list);
            if (validationError != null) {
                event.consume();
                new Alert(Alert.AlertType.WARNING, validationError).showAndWait();
                return;
            }

            try {
                user.setRoleId("Admin".equals(role) ? 1 : 2);

                UserDAO dao = new UserDAO();
                dao.update(user);

                int idx = list.indexOf(user);
                if (idx >= 0) {
                    list.set(idx, user);
                }

                activityByUser.put(user.getUserId(), loadActivityForUser(user));
                table.refresh();
            } catch (Exception ex) {
                event.consume();
                new Alert(Alert.AlertType.ERROR, "Update failed: " + ex.getMessage()).showAndWait();
            }
        });

        dialog.showAndWait();
    }

    private String validateRoleUpdate(User targetUser, String selectedRole, List<User> users) {
        if (!"Admin".equals(selectedRole) && !"Customer".equals(selectedRole)) {
            return "Please select a valid role.";
        }

        boolean demotingAdmin = targetUser.getRoleId() == 1 && "Customer".equals(selectedRole);
        if (!demotingAdmin) {
            return null;
        }

        User currentUser = AuthController.getCurrentUser();
        if (currentUser != null && currentUser.getUserId() == targetUser.getUserId()) {
            return "You cannot change your own admin role.";
        }

        long adminCount = users.stream().filter(u -> u.getRoleId() == 1).count();
        if (adminCount <= 1) {
            return "At least one admin account must remain.";
        }

        return null;
    }

    private TextFormatter<String> createValidatedFormatter(int maxLength, Pattern allowedPattern) {
        return new TextFormatter<>(change -> {
            String nextValue = change.getControlNewText();
            if (nextValue.length() > maxLength) {
                return null;
            }
            if (allowedPattern != null && !allowedPattern.matcher(nextValue).matches()) {
                return null;
            }
            return change;
        });
    }

    private User getUserAt(TableCell<User, ?> cell) {
        if (cell == null || cell.getTableView() == null) {
            return null;
        }
        int index = cell.getIndex();
        if (index < 0 || index >= cell.getTableView().getItems().size()) {
            return null;
        }
        return cell.getTableView().getItems().get(index);
    }

    private String getFullName(User user) {
        String first = normalizeInput(user.getFirstName());
        String last = normalizeInput(user.getLastName());
        return (first + " " + last).trim();
    }

    private String normalizeInput(String value) {
        return value == null ? "" : value.trim();
    }

    private void refreshStats(List<User> users, Label totalUsersValue, Label customerUsersValue, Label staffUsersValue) {
        long totalUsers = users.size();
        long staffCount = users.stream().filter(u -> u.getRoleId() == 1).count();
        long customerCount = Math.max(0, totalUsers - staffCount);

        totalUsersValue.setText(String.valueOf(totalUsers));
        customerUsersValue.setText(String.valueOf(customerCount));
        staffUsersValue.setText(String.valueOf(staffCount));
    }

    private VBox makeStatCard(String label, Label valueLabel) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.07), 12, 0, 0, 3); -fx-padding: 20;");
        Label lbl = new Label(label); lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    private Label makeStatValueLabel(String value, String color) {
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        return val;
    }

    private StackPane makeIconBox(String color, String svgData) {
        StackPane box = new StackPane(); box.setPrefSize(48, 48);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath(); icon.setContent(svgData);
        icon.setStroke(Color.WHITE); icon.setStrokeWidth(2); icon.setFill(Color.TRANSPARENT);
        box.getChildren().add(icon); return box;
    }

    private static class ActivitySummary {
        private final int orderCount;
        private final double totalSpend;

        ActivitySummary(int orderCount, double totalSpend) {
            this.orderCount = orderCount;
            this.totalSpend = totalSpend;
        }
    }

    private static class UserLoadResult {
        private final List<User> users;
        private final Map<Integer, ActivitySummary> activityByUser;

        UserLoadResult(List<User> users, Map<Integer, ActivitySummary> activityByUser) {
            this.users = users;
            this.activityByUser = activityByUser;
        }
    }
}
