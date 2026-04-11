package view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.BookController;
import dao.AuthorDAO;
import dao.CategoryDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.Author;
import model.Book;
import model.Category;

public class InventoryView {
    private BookController bookController;
    private Runnable onDashboardClick;
    private Runnable onReportsClick;
    private Runnable onUsersClick;
    private Runnable onLogoutClick;

    public InventoryView(Stage stage, BookController bookController,
            Runnable onDashboardClick, Runnable onReportsClick,
            Runnable onUsersClick, Runnable onOrdersClick, Runnable onLogoutClick) {
        this.bookController = bookController;
        this.onDashboardClick = onDashboardClick;
        this.onReportsClick = onReportsClick;
        this.onUsersClick = onUsersClick;
        this.onLogoutClick = onLogoutClick;
    }

    public Scene createInventoryScene() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #F0F2F5;");

        HBox nav = NavigationBar.create(true, "inventory",
            null, null, null, null,
            onDashboardClick, null, onReportsClick, onUsersClick,
            onLogoutClick, 0);

        Map<Integer, String> authorNames = loadAuthorNames();
        Map<Integer, String> categoryNames = loadCategoryNames();

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 24, 32, 24));
        content.setStyle("-fx-background-color: #F0F2F5;");

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane iconBox = makeIconBox("#5A6F8C", "M3 3h18v4H3z M5 9h14v10H5z M9 13h6v2H9z");
        Label title = new Label("Inventory Management");
        title.setStyle("-fx-font-size: 34px; -fx-text-fill: #5D4A3A;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button addNewBtn = new Button("Add New Book");
        addNewBtn.setStyle("-fx-background-color: #5A6F8C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8 14; -fx-cursor: hand;");
        header.getChildren().addAll(iconBox, title, spacer, addNewBtn);

        ObservableList<Book> booksData = FXCollections.observableArrayList();
        try {
            List<Book> books = bookController.getAllBooks();
            booksData.addAll(books);
        } catch (Exception e) {
            showError("Failed to load inventory: " + e.getMessage());
        }

        HBox statsRow = new HBox(14);
        Label totalVal = new Label();
        Label inStockVal = new Label();
        Label lowStockVal = new Label();
        Label outStockVal = new Label();
        VBox totalCard = makeStatCard("Total Items", totalVal, "#5A6F8C", null);
        VBox inStockCard = makeStatCard("In Stock", inStockVal, "#7A9E6B", null);
        VBox lowStockCard = makeStatCard("Low Stock", lowStockVal, "#E6B422", "#E6B42240");
        VBox outStockCard = makeStatCard("Out of Stock", outStockVal, "#B14A3A", "#B14A3A40");
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(inStockCard, Priority.ALWAYS);
        HBox.setHgrow(lowStockCard, Priority.ALWAYS);
        HBox.setHgrow(outStockCard, Priority.ALWAYS);
        statsRow.getChildren().addAll(totalCard, inStockCard, lowStockCard, outStockCard);
        updateStats(booksData, totalVal, inStockVal, lowStockVal, outStockVal);

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search by title, author, or ISBN...");
        searchField.setPrefHeight(44);
        searchField.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: rgba(93,74,58,0.15); -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-padding: 0 12;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        toolbar.getChildren().add(searchField);

        FilteredList<Book> filtered = new FilteredList<>(booksData, b -> true);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            String q = newValue == null ? "" : newValue.trim().toLowerCase();
            filtered.setPredicate(book -> {
                if (q.isEmpty()) {
                    return true;
                }
                String titleText = book.getBookName() == null ? "" : book.getBookName().toLowerCase();
                String isbnText = book.getIsbn() == null ? "" : book.getIsbn().toLowerCase();
                String authorText = authorNames.getOrDefault(book.getAuthorId(), "Unknown Author").toLowerCase();
                return titleText.contains(q) || isbnText.contains(q) || authorText.contains(q);
            });
        });

        TableView<Book> table = new TableView<>();
        table.setItems(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px;");
        table.getStyleClass().add("admin-inventory-table");
        table.setPrefHeight(520);

        TableColumn<Book, String> detailsCol = new TableColumn<>("Book Details");
        detailsCol.setCellValueFactory(d -> new SimpleStringProperty("details"));
        detailsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Book book = getTableView().getItems().get(getIndex());
                VBox box = new VBox(2);
                Label titleLabel = new Label(book.getBookName() == null ? "Untitled" : book.getBookName());
                titleLabel.setStyle("-fx-text-fill: #5D4A3A; -fx-font-size: 13px;");
                Label authorLabel = new Label("by " + authorNames.getOrDefault(book.getAuthorId(), "Unknown Author"));
                authorLabel.setStyle("-fx-text-fill: #5D4A3A; -fx-opacity: 0.6; -fx-font-size: 11px;");
                box.getChildren().addAll(titleLabel, authorLabel);
                setGraphic(box);
            }
        });

        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIsbn() == null ? "-" : d.getValue().getIsbn()));
        isbnCol.setStyle("-fx-font-family: 'Consolas'; -fx-text-fill: #5D4A3A; -fx-opacity: 0.75;");

        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(d -> new SimpleStringProperty(categoryNames.getOrDefault(d.getValue().getCategoryId(), "General")));
        categoryCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(category);
                badge.setStyle("-fx-background-color: #5A6F8C20; -fx-text-fill: #5A6F8C; -fx-border-color: #5A6F8C40;" +
                    "-fx-border-radius: 18px; -fx-background-radius: 18px; -fx-padding: 4 10; -fx-font-size: 11px;");
                setGraphic(badge);
            }
        });

        TableColumn<Book, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(d -> new SimpleStringProperty("$" + String.format("%.2f", d.getValue().getPrice())));
        priceCol.setStyle("-fx-text-fill: #2C6E6E; -fx-font-weight: bold;");

        TableColumn<Book, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStockLevel()));
        stockCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    return;
                }
                setText(stock.intValue() + " units");
                setStyle("-fx-text-fill: #5D4A3A;");
            }
        });

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(statusForStock(d.getValue().getStockLevel())));
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                String color = "In Stock".equals(status) ? "#7A9E6B" : ("Low Stock".equals(status) ? "#E6B422" : "#B14A3A");
                Label badge = new Label(status);
                badge.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + "; -fx-border-color: " + color + "40;" +
                    "-fx-border-radius: 18px; -fx-background-radius: 18px; -fx-padding: 4 10; -fx-font-size: 11px;");
                setGraphic(badge);
            }
        });

        TableColumn<Book, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button delBtn = new Button("Delete");
            private final HBox box = new HBox(6, editBtn, delBtn);
            {
                editBtn.setStyle("-fx-background-color: #5A6F8C20; -fx-text-fill: #5A6F8C; -fx-font-weight: bold; -fx-background-radius: 8px;" +
                    "-fx-border-color: #5A6F8C40; -fx-border-radius: 8px; -fx-padding: 4 10;");
                delBtn.setStyle("-fx-background-color: #B14A3A20; -fx-text-fill: #B14A3A; -fx-font-weight: bold; -fx-background-radius: 8px;" +
                    "-fx-border-color: #B14A3A40; -fx-border-radius: 8px; -fx-padding: 4 10;");

                editBtn.setOnAction(e -> editBook());
                delBtn.setOnAction(e -> deleteBook());
            }

            private void editBook() {
                Book book = getTableView().getItems().get(getIndex());
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Edit Book");
                dialog.setHeaderText("Update inventory values");

                TextField titleField = new TextField(book.getBookName() == null ? "" : book.getBookName());
                titleField.setPromptText("Title");
                TextField isbnField = new TextField(book.getIsbn() == null ? "" : book.getIsbn());
                isbnField.setPromptText("ISBN");
                TextField priceField = new TextField(String.valueOf(book.getPrice()));
                priceField.setPromptText("Price");
                TextField stockField = new TextField(String.valueOf(book.getStockLevel()));
                stockField.setPromptText("Stock");

                VBox form = new VBox(10,
                    new Label("Title"), titleField,
                    new Label("ISBN"), isbnField,
                    new Label("Price"), priceField,
                    new Label("Stock"), stockField
                );
                form.setPadding(new Insets(10));
                dialog.getDialogPane().setContent(form);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                dialog.showAndWait().ifPresent(result -> {
                    if (result != ButtonType.OK) {
                        return;
                    }
                    try {
                        book.setBookName(titleField.getText().trim());
                        book.setIsbn(isbnField.getText().trim());
                        book.setPrice(Double.parseDouble(priceField.getText().trim()));
                        book.setStockLevel(Math.max(0, Integer.parseInt(stockField.getText().trim())));
                        bookController.updateBook(book);
                        table.refresh();
                        updateStats(booksData, totalVal, inStockVal, lowStockVal, outStockVal);
                    } catch (Exception ex) {
                        showError("Failed to update book: " + ex.getMessage());
                    }
                });
            }

            private void deleteBook() {
                Book book = getTableView().getItems().get(getIndex());
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + (book.getBookName() == null ? "book" : book.getBookName()) + "\"?");
                confirm.setHeaderText(null);
                confirm.showAndWait().ifPresent(result -> {
                    if (result != ButtonType.OK) {
                        return;
                    }
                    try {
                        bookController.deleteBook(book.getBookId());
                        booksData.remove(book);
                        updateStats(booksData, totalVal, inStockVal, lowStockVal, outStockVal);
                    } catch (Exception ex) {
                        showError("Failed to delete book: " + ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().add(detailsCol);
        table.getColumns().add(isbnCol);
        table.getColumns().add(categoryCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(stockCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(actionsCol);
        table.setPlaceholder(new Label("No books found matching your search"));

        addNewBtn.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add New Book");
            dialog.setHeaderText("Enter basic inventory details");

            TextField titleField = new TextField();
            titleField.setPromptText("Title");
            TextField isbnField = new TextField();
            isbnField.setPromptText("ISBN");
            TextField priceField = new TextField();
            priceField.setPromptText("Price");
            TextField stockField = new TextField();
            stockField.setPromptText("Stock");

            VBox form = new VBox(10,
                new Label("Title"), titleField,
                new Label("ISBN"), isbnField,
                new Label("Price"), priceField,
                new Label("Stock"), stockField
            );
            form.setPadding(new Insets(10));
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(result -> {
                if (result != ButtonType.OK) {
                    return;
                }
                try {
                    Book newBook = new Book();
                    newBook.setBookName(titleField.getText().trim());
                    newBook.setIsbn(isbnField.getText().trim());
                    newBook.setPrice(Double.parseDouble(priceField.getText().trim()));
                    newBook.setStockLevel(Math.max(0, Integer.parseInt(stockField.getText().trim())));
                    int id = bookController.addBook(newBook);
                    newBook.setBookId(id);
                    booksData.add(0, newBook);
                    updateStats(booksData, totalVal, inStockVal, lowStockVal, outStockVal);
                } catch (Exception ex) {
                    showError("Failed to add book: " + ex.getMessage());
                }
            });
        });

        content.getChildren().addAll(header, statsRow, toolbar, table);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #F0F2F5; -fx-background: #F0F2F5;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox makeStatCard(String label, Label valueLabel, String color, String borderColor) {
        VBox card = new VBox(6);
        String borderStyle = borderColor == null ? "" : ("-fx-border-color: " + borderColor + "; -fx-border-width: 2px; ");
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-border-radius: 12px;" + borderStyle +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 12, 0, 0, 3); -fx-padding: 16;");
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: #5D4A3A; -fx-opacity: 0.7;");
        valueLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        card.getChildren().addAll(labelNode, valueLabel);
        return card;
    }

    private void updateStats(List<Book> books, Label totalVal, Label inStockVal, Label lowStockVal, Label outStockVal) {
        int total = books.size();
        int inStock = 0;
        int lowStock = 0;
        int outStock = 0;
        for (Book book : books) {
            if (book.getStockLevel() <= 0) {
                outStock++;
            } else if (book.getStockLevel() <= 10) {
                lowStock++;
            } else {
                inStock++;
            }
        }
        totalVal.setText(String.valueOf(total));
        inStockVal.setText(String.valueOf(inStock));
        lowStockVal.setText(String.valueOf(lowStock));
        outStockVal.setText(String.valueOf(outStock));
    }

    private String statusForStock(int stock) {
        if (stock <= 0) {
            return "Out of Stock";
        }
        if (stock <= 10) {
            return "Low Stock";
        }
        return "In Stock";
    }

    private Map<Integer, String> loadAuthorNames() {
        Map<Integer, String> names = new HashMap<>();
        try {
            AuthorDAO dao = new AuthorDAO();
            for (Author author : dao.readAll()) {
                names.put(author.getAuthorId(), author.getAuthorName());
            }
        } catch (Exception ignored) {
            // Keep fallback names.
        }
        return names;
    }

    private Map<Integer, String> loadCategoryNames() {
        Map<Integer, String> names = new HashMap<>();
        try {
            CategoryDAO dao = new CategoryDAO();
            for (Category category : dao.readAll()) {
                names.put(category.getCategoryId(), category.getCategoryName());
            }
        } catch (Exception ignored) {
            // Keep fallback names.
        }
        return names;
    }

    private StackPane makeIconBox(String color, String svgData) {
        StackPane box = new StackPane();
        box.setPrefSize(48, 48);
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");
        SVGPath icon = new SVGPath();
        icon.setContent(svgData);
        icon.setStroke(Color.WHITE);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        box.getChildren().add(icon);
        return box;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Inventory Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
