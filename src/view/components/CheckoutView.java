package view.components;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Book;
import model.CartItem;
import util.OrderPricing;

/**
 * 3-step checkout wizard matching Checkout.tsx design.
 * Step 1: Shipping Info, Step 2: Payment Info, Step 3: Review Order
 */
public class CheckoutView {
    private static final Pattern NAME_INPUT_PATTERN = Pattern.compile("[a-zA-Z\\s'\\-]*");
    private static final Pattern EMAIL_INPUT_PATTERN = Pattern.compile("[a-zA-Z0-9@._+\\-]*");
    private static final Pattern ADDRESS_INPUT_PATTERN = Pattern.compile("[a-zA-Z0-9\\s#.,'\\-/]*");
    private static final Pattern CITY_STATE_INPUT_PATTERN = Pattern.compile("[a-zA-Z\\s'\\-]*");
    private static final Pattern ZIP_INPUT_PATTERN = Pattern.compile("[0-9\\-\\s]*");
    private static final Pattern CARD_NUMBER_INPUT_PATTERN = Pattern.compile("[0-9\\s]*");
    private static final Pattern EXPIRY_INPUT_PATTERN = Pattern.compile("[0-9/]*");
    private static final Pattern CVV_INPUT_PATTERN = Pattern.compile("[0-9]*");

    private static final Pattern EMAIL_VALIDATION_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern ZIP_VALIDATION_PATTERN = Pattern.compile("^[0-9]{4,10}(?:[-\\s][0-9]{2,6})?$");
    private static final Pattern CARD_NUMBER_VALIDATION_PATTERN = Pattern.compile("^[0-9]{13,19}$");
    private static final Pattern EXPIRY_VALIDATION_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");
    private static final Pattern CVV_VALIDATION_PATTERN = Pattern.compile("^[0-9]{3,4}$");

    private Stage stage;
    private OrderController orderController;
    private CartController cartController;
    private Runnable onOrderComplete;
    private Runnable onBackClick;
    private Runnable onLogoutClick;
    private BookController bookController;
    private final Map<Integer, String> bookNameCache = new HashMap<>();

    // Stored values across steps
    private String savedFirstName = "", savedLastName = "", savedEmail = "";
    private String savedAddress = "", savedCity = "", savedState = "", savedZip = "";
    private String savedCardName = "", savedCardLast4 = "";
    private double totalAmount = 0;
    private int itemCount = 0;
    private List<CartItem> checkoutItems = new ArrayList<>();

    public CheckoutView(Stage stage, OrderController orderController, CartController cartController,
            AuthController authController, Runnable onOrderComplete, Runnable onBackClick, Runnable onLogoutClick) {
        this.stage = stage;
        this.orderController = orderController;
        this.cartController = cartController;
        this.onOrderComplete = onOrderComplete;
        this.onBackClick = onBackClick;
        this.onLogoutClick = onLogoutClick;
        try {
            this.bookController = new BookController();
        } catch (Exception ex) {
            this.bookController = null;
            System.err.println("Checkout book controller init failed: " + ex.getMessage());
        }
    }

    public Scene createCheckoutScene() throws Exception {
        return buildScene(1);
    }

    private Scene buildScene(int step) {
        VBox root = new VBox(0);
        root.getStyleClass().add("page-shell-customer");

        // Nav
        HBox nav = NavigationBar.create(false, "checkout",
            null, onBackClick, null, null, null, null, null, null,
            onLogoutClick, 0);

        // Content
        VBox content = new VBox(32);
        content.setPadding(new Insets(40, 48, 48, 48));
        content.getStyleClass().add("page-content");

        // Title
        Label titleLabel = new Label("Secure Checkout");
        titleLabel.getStyleClass().add("page-title-sm");
        titleLabel.setStyle("-fx-text-fill: #9B4B3A;");

        // Progress steps
        HBox stepsBar = buildStepsBar(step);

        // Card
        VBox card = new VBox(24);
        card.getStyleClass().add("surface-card");
        card.setPadding(new Insets(36));
        card.setMaxWidth(680);
        card.setAlignment(Pos.TOP_CENTER);

        // Security badge
        HBox secBadge = new HBox(8);
        secBadge.setAlignment(Pos.CENTER);
        Label lockIcon = new Label("🔒");
        lockIcon.getStyleClass().add("muted-copy");
        Label secLabel = new Label("Secure SSL Encrypted Transaction");
        secLabel.getStyleClass().add("muted-copy");
        secLabel.setStyle("-fx-text-fill: #9B4B3A;");
        secBadge.getChildren().addAll(lockIcon, secLabel);

        // Load selected cart data
        try {
            int userId = AuthController.getCurrentCustomer().getUserId();
            List<CartItem> items = cartController.getCartItems(userId);
            List<Integer> selectedIds = cartController.getSelectedCartItemIds();

            List<CartItem> resolvedItems = new ArrayList<>();
            if (selectedIds == null || selectedIds.isEmpty()) {
                resolvedItems.addAll(items);
            } else {
                for (CartItem item : items) {
                    if (selectedIds.contains(item.getCartItemId())) {
                        resolvedItems.add(item);
                    }
                }
            }

            checkoutItems = resolvedItems;
            itemCount = checkoutItems.size();
            totalAmount = 0;
            for (CartItem item : checkoutItems) {
                totalAmount += item.getPrice() * item.getQuantity();
            }
        } catch (Exception ex) {
            checkoutItems = new ArrayList<>();
            itemCount = 0;
            totalAmount = 0;
            System.err.println("Checkout data load failed: " + ex.getMessage());
        }

        // Pre-fill name from profile
        if (savedFirstName.isEmpty()) {
            try {
                savedFirstName = AuthController.getCurrentCustomer().getFirstName();
                savedLastName  = AuthController.getCurrentCustomer().getLastName();
                savedEmail     = AuthController.getCurrentCustomer().getEmail();
                savedAddress   = AuthController.getCurrentCustomer().getAddressLine();
                savedCity      = AuthController.getCurrentCustomer().getCity();
                savedState     = AuthController.getCurrentCustomer().getState();
                savedZip       = AuthController.getCurrentCustomer().getZipCode();
            } catch (Exception ex) {
                System.err.println("Checkout profile prefill failed: " + ex.getMessage());
            }
        }

        card.getChildren().add(secBadge);

        if (step == 1) {
			buildStep1(card, step);
		} else if (step == 2) {
			buildStep2(card, step);
		} else {
			buildStep3(card, step);
		}

        VBox centeredCard = new VBox(card);
        centeredCard.setAlignment(Pos.TOP_CENTER);
        content.getChildren().addAll(titleLabel, stepsBar, centeredCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().addAll(nav, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private HBox buildStepsBar(int currentStep) {
        HBox bar = new HBox(0);
        bar.setAlignment(Pos.CENTER);
        String[] labels = {"Shipping", "Payment", "Review"};
        for (int i = 1; i <= 3; i++) {
            boolean done   = i < currentStep;
            boolean active = i == currentStep;
            Label circle = new Label(done ? "✓" : String.valueOf(i));
            circle.setStyle(
                "-fx-min-width: 42; -fx-min-height: 42; -fx-max-width: 42; -fx-max-height: 42;" +
                "-fx-background-radius: 21px; -fx-alignment: center; -fx-font-weight: 800; -fx-font-size: 15px;" +
                "-fx-background-color: " + (done || active ? "#9B4B3A" : "#FFFFFF") + ";" +
                "-fx-text-fill: " + (done || active ? "#FFFFFF" : "#8a7b72") + ";" +
                "-fx-border-color: " + (done || active ? "#9B4B3A" : "rgba(138, 123, 114, 0.3)") + "; -fx-border-width: 2px; -fx-border-radius: 21px;" +
                (active ? " -fx-effect: dropshadow(three-pass-box, rgba(155, 75, 58, 0.4), 12, 0, 0, 4);" : "")
            );
            Label lbl = new Label(labels[i - 1]);
            lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: " + (active ? "bold" : "normal") + "; -fx-text-fill: " + (active ? "#9B4B3A" : "#8a7b72") + "; -fx-padding: 5 0 0 8;");
            HBox step = new HBox(8, circle, lbl);
            step.setAlignment(Pos.CENTER);
            bar.getChildren().add(step);
            if (i < 3) {
                Region line = new Region();
                line.setPrefWidth(60); line.setMaxHeight(3); line.setMinHeight(3);
                line.setStyle("-fx-background-color: " + (currentStep > i ? "#9B4B3A" : "rgba(138, 123, 114, 0.2)") + "; -fx-background-radius: 2px;");
                HBox.setMargin(line, new Insets(0, 10, 0, 10));
                bar.getChildren().add(line);
            }
        }
        return bar;
    }

    private void buildStep1(VBox card, int step) {
        Label stepTitle = new Label("Shipping Information");
        stepTitle.getStyleClass().add("section-heading");
        stepTitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #9B4B3A;");

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(16);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(50);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(c1, c2);

        TextField firstNameField = styledField("First Name", savedFirstName);
        TextField lastNameField  = styledField("Last Name", savedLastName);
        TextField emailField     = styledField("Email Address", savedEmail);
        TextField addressField   = styledField("Street Address", savedAddress);
        TextField cityField      = styledField("City", savedCity);
        TextField stateField     = styledField("State", savedState);
        TextField zipField       = styledField("ZIP Code", savedZip);

        firstNameField.setTextFormatter(createValidatedFormatter(40, NAME_INPUT_PATTERN));
        lastNameField.setTextFormatter(createValidatedFormatter(40, NAME_INPUT_PATTERN));
        emailField.setTextFormatter(createValidatedFormatter(120, EMAIL_INPUT_PATTERN));
        addressField.setTextFormatter(createValidatedFormatter(160, ADDRESS_INPUT_PATTERN));
        cityField.setTextFormatter(createValidatedFormatter(60, CITY_STATE_INPUT_PATTERN));
        stateField.setTextFormatter(createValidatedFormatter(60, CITY_STATE_INPUT_PATTERN));
        zipField.setTextFormatter(createValidatedFormatter(12, ZIP_INPUT_PATTERN));

        grid.add(labeled("First Name", firstNameField), 0, 0);
        grid.add(labeled("Last Name", lastNameField), 1, 0);
        VBox emailBox = labeled("Email Address", emailField);
        GridPane.setColumnSpan(emailBox, 2);
        grid.add(emailBox, 0, 1);
        VBox addrBox = labeled("Street Address", addressField);
        GridPane.setColumnSpan(addrBox, 2);
        grid.add(addrBox, 0, 2);
        grid.add(labeled("City", cityField), 0, 3);
        grid.add(labeled("State", stateField), 1, 3);
        VBox zipBox = labeled("ZIP Code", zipField);
        GridPane.setColumnSpan(zipBox, 2);
        grid.add(zipBox, 0, 4);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button continueBtn = primaryBtn("Continue");
        continueBtn.setOnAction(e -> {
            String firstName = normalizeInput(firstNameField.getText());
            String lastName = normalizeInput(lastNameField.getText());
            String email = normalizeInput(emailField.getText());
            String address = normalizeInput(addressField.getText());
            String city = normalizeInput(cityField.getText());
            String state = normalizeInput(stateField.getText());
            String zip = normalizeInput(zipField.getText());

            String validationError = validateShippingInput(firstName, lastName, email, address, city, state, zip);
            if (validationError != null) {
                showError(validationError);
                return;
            }

            savedFirstName = firstName;
            savedLastName  = lastName;
            savedEmail     = email;
            savedAddress   = address;
            savedCity      = city;
            savedState     = state;
            savedZip       = zip;
            stage.setScene(buildScene(2));
        });
        buttons.getChildren().add(continueBtn);
        card.getChildren().addAll(stepTitle, grid, buttons);
    }

    private void buildStep2(VBox card, int step) {
        Label stepTitle = new Label("Payment Information");
        stepTitle.getStyleClass().add("section-heading");
        stepTitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #9B4B3A;");

        VBox form = new VBox(16);

        TextField cardNameField   = styledField("Name on Card", savedCardName);
        TextField cardNumField    = styledField("1234  5678  9012  3456", "");
        TextField expiryField     = styledField("MM/YY", "");
        TextField cvvField        = styledField("CVV", "");

        cardNameField.setTextFormatter(createValidatedFormatter(80, NAME_INPUT_PATTERN));
        cardNumField.setTextFormatter(createValidatedFormatter(23, CARD_NUMBER_INPUT_PATTERN));
        expiryField.setTextFormatter(createValidatedFormatter(5, EXPIRY_INPUT_PATTERN));
        cvvField.setTextFormatter(createValidatedFormatter(4, CVV_INPUT_PATTERN));

        form.getChildren().addAll(
            labeled("Cardholder Name", cardNameField),
            labeled("Card Number", cardNumField),
            new HBox(14) {{ setAlignment(Pos.CENTER_LEFT);
                getChildren().addAll(labeled("Expiry Date", expiryField), labeled("CVV", cvvField));
                HBox.setHgrow(getChildren().get(0), Priority.ALWAYS);
                HBox.setHgrow(getChildren().get(1), Priority.ALWAYS);
            }}
        );

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button backBtn = secondaryBtn("Back");
        backBtn.setOnAction(e -> stage.setScene(buildScene(1)));
        Button continueBtn = primaryBtn("Continue");
        continueBtn.setOnAction(e -> {
            String cardName = normalizeInput(cardNameField.getText());
            String cardNumberRaw = normalizeInput(cardNumField.getText());
            String cardNumber = cardNumberRaw.replaceAll("\\s+", "");
            String expiry = normalizeInput(expiryField.getText());
            String cvv = normalizeInput(cvvField.getText());

            String validationError = validatePaymentInput(cardName, cardNumber, expiry, cvv);
            if (validationError != null) {
                showError(validationError);
                return;
            }

            savedCardName  = cardName;
            savedCardLast4 = cardNumber.substring(cardNumber.length() - 4);
            stage.setScene(buildScene(3));
        });
        buttons.getChildren().addAll(backBtn, continueBtn);
        card.getChildren().addAll(stepTitle, form, buttons);
    }

    private void buildStep3(VBox card, int step) {
        Label stepTitle = new Label("Review Your Order");
        stepTitle.getStyleClass().add("section-heading");
        stepTitle.setStyle("-fx-font-size: 22px; -fx-text-fill: #9B4B3A;");

        // Order summary panel
        VBox summary = reviewPanel("Order Summary");
        HBox itemRow = rowLine(itemCount + " item(s)", "$" + String.format("%.2f", totalAmount));
        HBox shipRow = rowLine("Shipping", "$" + String.format("%.2f", OrderPricing.SHIPPING_FEE));
        Region divider = new Region(); divider.setMaxHeight(1); divider.setMinHeight(1);
        divider.setStyle("-fx-background-color: rgba(93,74,58,0.12);");
        HBox totalRow = rowLine("Total", "$" + String.format("%.2f", OrderPricing.orderTotal(totalAmount)));
        Label totalLbl = (Label) totalRow.getChildren().get(totalRow.getChildren().size()-1);
        totalLbl.getStyleClass().add("price-highlight");
        totalLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #9B4B3A;");
        summary.getChildren().addAll(itemRow, shipRow, divider, totalRow);

        VBox itemsPanel = reviewPanel("Items in Your Order");
        try {
            if (checkoutItems.isEmpty()) {
                Label empty = new Label("No selected items found for checkout.");
                empty.getStyleClass().add("muted-copy");
                itemsPanel.getChildren().add(empty);
            } else {
                for (CartItem ci : checkoutItems) {
                    String bookName = (ci.getBookName() != null && !ci.getBookName().isBlank())
                        ? ci.getBookName()
                        : resolveBookName(ci.getBookId());
                    String left = bookName + " (ID: " + ci.getBookId() + ")";
                    String right = "Qty " + ci.getQuantity() + " • $" + String.format("%.2f", ci.getPrice() * ci.getQuantity());
                    itemsPanel.getChildren().add(rowLine(left, right));
                }
            }
        } catch (Exception ex) {
            Label warn = new Label("Could not load item list: " + ex.getMessage());
            warn.getStyleClass().add("error-text");
            itemsPanel.getChildren().add(warn);
        }

        // Shipping address panel
        VBox addrPanel = reviewPanel("Shipping Address");
        Label adr = new Label(savedFirstName + " " + savedLastName + "\n" + savedAddress + "\n" + savedCity + ", " + savedState + " " + savedZip);
        adr.getStyleClass().add("muted-copy");
        addrPanel.getChildren().add(adr);

        // Payment panel
        VBox payPanel = reviewPanel("Payment Method");
        Label payLbl = new Label((savedCardName.isEmpty() ? "Card" : savedCardName) +
            " ending in " + (savedCardLast4.isEmpty() ? "****" : savedCardLast4));
        payLbl.getStyleClass().add("muted-copy");
        payPanel.getChildren().add(payLbl);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        Button backBtn = secondaryBtn("Back");
        backBtn.setOnAction(e -> stage.setScene(buildScene(2)));
        Button placeBtn = primaryBtn("Place Order");
        placeBtn.setOnAction(e -> placeOrder());
        buttons.getChildren().addAll(backBtn, placeBtn);
        card.getChildren().addAll(stepTitle, summary, itemsPanel, addrPanel, payPanel, buttons);
    }

    private void placeOrder() {
        try {
            if (checkoutItems.isEmpty()) {
                showError("No selected books to checkout.");
                return;
            }

            String shippingValidation = validateShippingInput(
                normalizeInput(savedFirstName),
                normalizeInput(savedLastName),
                normalizeInput(savedEmail),
                normalizeInput(savedAddress),
                normalizeInput(savedCity),
                normalizeInput(savedState),
                normalizeInput(savedZip));
            if (shippingValidation != null) {
                showError("Invalid shipping details: " + shippingValidation);
                return;
            }

            int userId = AuthController.getCurrentCustomer().getUserId();
            int cartId = cartController.getCartIdForUser(userId);
            List<Integer> selectedCartItemIds = new ArrayList<>();
            for (CartItem item : checkoutItems) {
                selectedCartItemIds.add(item.getCartItemId());
            }

            int orderId = orderController.placeOrder(
                userId,
                cartId,
                savedAddress + ", " + savedCity + ", " + savedState + " " + savedZip,
                selectedCartItemIds
            );

            cartController.clearSelectedCartItemIds();
            showSuccess("Order #" + orderId + " placed successfully! 🎉");
            onOrderComplete.run();
        } catch (Exception e) {
            showError("Failed to place order: " + e.getMessage());
        }
    }

    // ─── UI helpers ───
    private VBox reviewPanel(String title) {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("surface-card-compact");
        panel.setStyle("-fx-background-color: #fcfbfa; -fx-border-color: rgba(90,70,56,0.1); -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 18; " +
                       "-fx-effect: dropshadow(three-pass-box, rgba(40,30,20,0.03), 10, 0, 0, 4);");
        Label h = new Label(title);
        h.getStyleClass().add("form-label");
        h.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #5A6F8C;");
        panel.getChildren().add(h);
        return panel;
    }

    private HBox rowLine(String left, String right) {
        HBox r = new HBox(); r.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(left);
        l.getStyleClass().add("muted-copy");
        l.setStyle("-fx-font-weight: 600; -fx-text-fill: #6b5c50;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(right);
        v.getStyleClass().add("muted-copy");
        v.setStyle("-fx-opacity: 0.95; -fx-font-weight: bold; -fx-text-fill: #3b2e2a;");
        r.getChildren().addAll(l, sp, v);
        return r;
    }

    private TextField styledField(String prompt, String value) {
        TextField f = new TextField(value);
        f.setPromptText(prompt);
        f.setPrefHeight(46);
        f.setMaxWidth(Double.MAX_VALUE);
        f.getStyleClass().add("input-modern");
        f.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 8 14; -fx-font-size: 14px; -fx-border-color: rgba(90,70,56,0.15);");
        return f;
    }

    private VBox labeled(String label, TextField field) {
        VBox box = new VBox(6);
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        box.getChildren().addAll(l, field);
        return box;
    }

    private TextFormatter<String> createValidatedFormatter(int maxLength, Pattern allowedPattern) {
        return new TextFormatter<>(change -> {
            String next = change.getControlNewText();
            if (next.length() > maxLength) {
                return null;
            }
            if (allowedPattern != null && !allowedPattern.matcher(next).matches()) {
                return null;
            }
            return change;
        });
    }

    private String normalizeInput(String value) {
        return value == null ? "" : value.trim();
    }

    private String validateShippingInput(String firstName, String lastName, String email,
            String address, String city, String state, String zip) {
        if (firstName.isBlank()) {
            return "First name is required.";
        }
        if (firstName.length() < 2) {
            return "First name must be at least 2 characters.";
        }
        if (lastName.isBlank()) {
            return "Last name is required.";
        }
        if (email.isBlank()) {
            return "Email address is required.";
        }
        if (!EMAIL_VALIDATION_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        if (address.isBlank()) {
            return "Street address is required.";
        }
        if (city.isBlank()) {
            return "City is required.";
        }
        if (state.isBlank()) {
            return "State is required.";
        }
        if (zip.isBlank()) {
            return "ZIP Code is required.";
        }
        if (!ZIP_VALIDATION_PATTERN.matcher(zip).matches()) {
            return "Please enter a valid ZIP Code.";
        }
        return null;
    }

    private String validatePaymentInput(String cardName, String cardNumber, String expiry, String cvv) {
        if (cardName.isBlank()) {
            return "Cardholder name is required.";
        }
        if (cardName.length() < 2) {
            return "Cardholder name must be at least 2 characters.";
        }
        if (!CARD_NUMBER_VALIDATION_PATTERN.matcher(cardNumber).matches()) {
            return "Card number must be 13 to 19 digits.";
        }
        if (!EXPIRY_VALIDATION_PATTERN.matcher(expiry).matches()) {
            return "Expiry date must be in MM/YY format.";
        }
        if (!isValidExpiry(expiry)) {
            return "Card expiry date is in the past.";
        }
        if (!CVV_VALIDATION_PATTERN.matcher(cvv).matches()) {
            return "CVV must be 3 or 4 digits.";
        }
        return null;
    }

    private boolean isValidExpiry(String expiry) {
        try {
            YearMonth expiryMonth = YearMonth.parse(expiry, DateTimeFormatter.ofPattern("MM/yy"));
            return !expiryMonth.isBefore(YearMonth.now());
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    private Button primaryBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(44);
        b.setPrefWidth(160);
        b.getStyleClass().add("button-customer-primary");
        return b;
    }

    private Button secondaryBtn(String text) {
        Button b = new Button(text);
        b.setPrefHeight(44);
        b.setPrefWidth(100);
        b.getStyleClass().add("button-secondary");
        return b;
    }

    private void showSuccess(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Order Placed"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private String resolveBookName(int bookId) {
        if (bookNameCache.containsKey(bookId)) {
            return bookNameCache.get(bookId);
        }

        String label = "Book #" + bookId;
        if (bookController != null) {
            try {
                Book book = bookController.getBook(bookId);
                if (book != null && book.getBookName() != null && !book.getBookName().isBlank()) {
                    label = book.getBookName();
                }
            } catch (Exception ex) {
                System.err.println("Checkout book name resolve failed for ID " + bookId + ": " + ex.getMessage());
            }
        }

        bookNameCache.put(bookId, label);
        return label;
    }
}
