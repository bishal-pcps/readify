package view.components;

import controller.AuthController;
import java.util.regex.Pattern;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UnifiedAuthView {
    private static final Pattern NAME_INPUT_PATTERN = Pattern.compile("[a-zA-Z\\s'\\-]*");
    private static final Pattern EMAIL_INPUT_PATTERN = Pattern.compile("[a-zA-Z0-9@._+\\-]*");
    private static final Pattern EMAIL_VALIDATION_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private AuthController authController;
    private Runnable onAuthSuccess;

    private StackPane formContainer;
    private VBox loginForm;
    private ScrollPane registerForm;

    // --- Login field error labels ---
    private Label emailError, passError, loginGeneralError;

    // --- Register field error labels ---
    private Label regGeneralError;

    public UnifiedAuthView(Stage stage, AuthController authController, Runnable onAuthSuccess) {
        this.authController = authController;
        this.onAuthSuccess = onAuthSuccess;
    }

    public Scene createAuthScene() {
        HBox root = new HBox();
        root.getStyleClass().add("premium-bg");

        // --- Left Panel (Hero/Brand) ---
        VBox heroPanel = buildHeroPanel();
        HBox.setHgrow(heroPanel, Priority.ALWAYS);

        // --- Right Panel (Auth Forms) ---
        VBox authPanel = new VBox();
        authPanel.setAlignment(Pos.CENTER);
        authPanel.setPadding(new Insets(40, 60, 40, 60));
        authPanel.setPrefWidth(550);
        authPanel.getStyleClass().add("auth-side-panel");

        VBox contentBox = new VBox(32);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(420);

        VBox header = buildHeader();

        formContainer = new StackPane();
        loginForm = createLoginForm();
        registerForm = createRegisterForm();
        formContainer.getChildren().add(loginForm);

        HBox footer = buildFooter();

        contentBox.getChildren().addAll(header, formContainer, footer);
        authPanel.getChildren().add(contentBox);

        root.getChildren().addAll(heroPanel, authPanel);

        Scene scene = new Scene(root, 1200, 850);
        scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
        return scene;
    }

    private VBox buildHeroPanel() {
        VBox hero = new VBox(24);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(60));
        hero.getStyleClass().add("premium-bg");
        hero.setStyle("-fx-border-color: rgba(93,74,58,0.05); -fx-border-width: 0 1 0 0;");

        StackPane artBox = new StackPane();
        artBox.setPrefSize(300, 300);
        artBox.getStyleClass().add("hero-circle-art");

        SVGPath mainIcon = new SVGPath();
        mainIcon.setContent("M12 6.00019C10.2 6.00019 8.2 7.20019 7 8.50019V17.0002C8.2 15.7002 10.2 14.5002 12 14.5002M12 6.00019C13.8 6.00019 15.8 7.20019 17 8.50019V17.0002C15.8 15.7002 13.8 14.5002 12 14.5002M12 6.00019V14.5002M12 14.5002C10.2 14.5002 8.2 15.7002 7 17.0002M12 14.5002C13.8 14.5002 15.8 15.7002 17 17.0002M2 9.00019C2 7.34334 3.34315 6.00019 5 6.00019H19C20.6569 6.00019 22 7.34334 22 9.00019V18.1048C22 19.3402 20.8711 20.26 19.6601 20.0093C18.1506 19.6967 15.9388 19.0002 12 19.0002C8.06124 19.0002 5.84937 19.6967 4.33989 20.0093C3.12894 20.26 2 19.3402 2 18.1048V9.00019Z");
        mainIcon.setScaleX(4.5);
        mainIcon.setScaleY(4.5);
        mainIcon.setStroke(Color.web("#9B4B3A"));
        mainIcon.setStrokeWidth(1.2);
        mainIcon.setFill(Color.TRANSPARENT);
        artBox.getChildren().add(mainIcon);

        Label quote = new Label("\"A room without books is like a body without a soul.\"");
        quote.getStyleClass().add("vintage-header");
        quote.setStyle("-fx-font-size: 28px; -fx-text-alignment: center;");
        quote.setWrapText(true);
        quote.setMaxWidth(400);

        Label author = new Label("- Marcus Tullius Cicero");
        author.getStyleClass().add("vintage-subtitle");
        author.setStyle("-fx-font-style: italic;");

        hero.getChildren().addAll(artBox, quote, author);
        return hero;
    }

    private VBox buildHeader() {
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Readify");
        title.getStyleClass().add("vintage-header");

        Label subtitle = new Label("Where your next adventure begins.");
        subtitle.getStyleClass().add("vintage-subtitle");

        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createLoginForm() {
        VBox card = new VBox(20);
        card.getStyleClass().add("premium-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label signinLabel = new Label("Sign In");
        signinLabel.getStyleClass().add("section-heading");
        signinLabel.setStyle("-fx-font-size: 22px;");

        loginGeneralError = makeGeneralErrorLabel();

        // Fields
        emailError = makeErrorLabel();
        TextField emailField = makeInputField("Email Address", "M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z M22 6l-10 7L2 6");
        emailField.setTextFormatter(createValidatedFormatter(120, EMAIL_INPUT_PATTERN));
        emailField.textProperty().addListener((o, ov, nv) -> clearError(emailField, emailError));

        passError = makeErrorLabel();
        PasswordField passField = makePassField("Password", "M19 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2z M7 11V7a5 5 0 0 1 10 0v4");
        passField.setTextFormatter(createMaxLengthFormatter(72));
        passField.textProperty().addListener((o, ov, nv) -> clearError(passField, passError));

        // Submit
        Button submitBtn = new Button("Continue to Library");
        submitBtn.getStyleClass().add("btn-premium-auth");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (validateLoginFields(emailField, passField)) {
                handleUnifiedLogin(emailField.getText().trim(), passField.getText());
            }
        });

        HBox switchRow = new HBox(5);
        switchRow.setAlignment(Pos.CENTER);
        Label noAcc = new Label("New to Readify?");
        Hyperlink link = new Hyperlink("Create an account");
        link.setStyle("-fx-text-fill: #9B4B3A; -fx-font-weight: bold;");
        link.setOnAction(e -> switchForm(true));
        switchRow.getChildren().addAll(noAcc, link);

        card.getChildren().addAll(signinLabel, loginGeneralError,
                fieldGroup("Email Address", emailField, emailError),
                fieldGroup("Password", passField, passError),
                submitBtn, switchRow);
        return card;
    }

    private ScrollPane createRegisterForm() {
        VBox card = new VBox(18);
        card.getStyleClass().add("premium-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label regLabel = new Label("Join the Community");
        regLabel.getStyleClass().add("section-heading");
        regLabel.setStyle("-fx-font-size: 22px;");

        regGeneralError = makeGeneralErrorLabel();

        // Error labels per field
        Label fNameError = makeErrorLabel();
        Label lNameError = makeErrorLabel();
        Label emailErrLabel = makeErrorLabel();
        Label passErrLabel  = makeErrorLabel();
        Label confirmErrLabel = makeErrorLabel();

        TextField firstNameField = makeInputField("First Name", null);
        TextField lastNameField  = makeInputField("Last Name", null);
        TextField regEmailField  = makeInputField("Email Address", null);
        PasswordField passwordField = makePassField("Create Password (min 6 chars)", null);
        PasswordField confirmField  = makePassField("Confirm Password", null);

        firstNameField.setTextFormatter(createValidatedFormatter(40, NAME_INPUT_PATTERN));
        lastNameField.setTextFormatter(createValidatedFormatter(40, NAME_INPUT_PATTERN));
        regEmailField.setTextFormatter(createValidatedFormatter(120, EMAIL_INPUT_PATTERN));
        passwordField.setTextFormatter(createMaxLengthFormatter(72));
        confirmField.setTextFormatter(createMaxLengthFormatter(72));

        // Clear general error on any typing
        firstNameField.textProperty().addListener((o, ov, nv) -> { fNameError.setVisible(false); fNameError.setManaged(false); hideRegisterGeneralError(); });
        lastNameField.textProperty().addListener((o, ov, nv) -> { lNameError.setVisible(false); lNameError.setManaged(false); hideRegisterGeneralError(); });
        regEmailField.textProperty().addListener((o, ov, nv) -> { emailErrLabel.setVisible(false); emailErrLabel.setManaged(false); hideRegisterGeneralError(); });
        passwordField.textProperty().addListener((o, ov, nv) -> { passErrLabel.setVisible(false); passErrLabel.setManaged(false); hideRegisterGeneralError(); });
        confirmField.textProperty().addListener((o, ov, nv)  -> { confirmErrLabel.setVisible(false); confirmErrLabel.setManaged(false); hideRegisterGeneralError(); });

        Button submitBtn = new Button("Create Account");
        submitBtn.getStyleClass().add("btn-premium-auth");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            if (validateRegisterFields(firstNameField, lastNameField, regEmailField, passwordField, confirmField,
                    fNameError, lNameError, emailErrLabel, passErrLabel, confirmErrLabel)) {
                handleRegister(firstNameField.getText().trim(), lastNameField.getText().trim(), regEmailField.getText().trim(),
                        "", "", "", "", "", "", passwordField.getText(), confirmField.getText());
            }
        });

        // Name row: each field takes equal width
        HBox nameRow = new HBox(15);
        VBox fnBox = fieldGroup("First Name", firstNameField, fNameError);
        VBox lnBox = fieldGroup("Last Name", lastNameField, lNameError);
        HBox.setHgrow(fnBox, Priority.ALWAYS);
        HBox.setHgrow(lnBox, Priority.ALWAYS);
        nameRow.getChildren().addAll(fnBox, lnBox);

        HBox switchRow = new HBox(5);
        switchRow.setAlignment(Pos.CENTER);
        Label hasAcc = new Label("Already a member?");
        Hyperlink link = new Hyperlink("Sign In");
        link.setStyle("-fx-text-fill: #9B4B3A; -fx-font-weight: bold;");
        link.setOnAction(e -> switchForm(false));
        switchRow.getChildren().addAll(hasAcc, link);

        card.getChildren().addAll(regLabel, regGeneralError,
                nameRow,
                fieldGroup("Email", regEmailField, emailErrLabel),
                fieldGroup("Password", passwordField, passErrLabel),
                fieldGroup("Confirm Password", confirmField, confirmErrLabel),
                submitBtn, switchRow);

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private VBox fieldGroup(String labelStr, Control field, Label error) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelStr);
        lbl.getStyleClass().add("form-label");
        if (error != null) {
            box.getChildren().addAll(lbl, field, error);
        } else {
            box.getChildren().addAll(lbl, field);
        }
        return box;
    }

    private TextField makeInputField(String prompt, String svgIcon) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field-premium");
        tf.setPrefHeight(50);
        return tf;
    }

    private PasswordField makePassField(String prompt, String svgIcon) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.getStyleClass().add("input-field-premium");
        pf.setPrefHeight(50);
        return pf;
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

    private TextFormatter<String> createMaxLengthFormatter(int maxLength) {
        return createValidatedFormatter(maxLength, null);
    }

    private Label makeErrorLabel() {
        Label lbl = new Label();
        lbl.getStyleClass().add("error-text");
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    private Label makeGeneralErrorLabel() {
        Label lbl = new Label();
        lbl.getStyleClass().add("error-banner");
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setVisible(false);
        lbl.setManaged(false);
        return lbl;
    }

    private HBox buildFooter() {
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));
        String[] items = {"Terms", "Privacy", "Help Center"};
        for (String s : items) {
            Hyperlink h = new Hyperlink(s);
            h.getStyleClass().add("muted-copy");
            footer.getChildren().add(h);
        }
        return footer;
    }

    private void switchForm(boolean toRegister) {
        FadeTransition out = new FadeTransition(Duration.millis(200), formContainer);
        out.setFromValue(1.0);
        out.setToValue(0.0);
        out.setOnFinished(e -> {
            formContainer.getChildren().clear();
            formContainer.getChildren().add(toRegister ? registerForm : loginForm);
            FadeTransition in = new FadeTransition(Duration.millis(250), formContainer);
            in.setFromValue(0.0);
            in.setToValue(1.0);
            in.play();
        });
        out.play();
    }

    private boolean validateLoginFields(TextField email, PasswordField pass) {
        boolean valid = true;
        String emailValue = email.getText() == null ? "" : email.getText().trim();
        String passwordValue = pass.getText() == null ? "" : pass.getText();

        if (emailValue.isEmpty()) {
            emailError.setText("Email is required");
            emailError.setVisible(true);
            emailError.setManaged(true);
            valid = false;
        } else if (!EMAIL_VALIDATION_PATTERN.matcher(emailValue).matches()) {
            emailError.setText("Enter a valid email address");
            emailError.setVisible(true);
            emailError.setManaged(true);
            valid = false;
        }

        if (passwordValue.isEmpty()) {
            passError.setText("Password is required");
            passError.setVisible(true);
            passError.setManaged(true);
            valid = false;
        }

        return valid;
    }

    private boolean validateRegisterFields(TextField f, TextField l, TextField e, PasswordField p, PasswordField c,
            Label fErr, Label lErr, Label eErr, Label pErr, Label cErr) {
        boolean valid = true;
        String firstName = f.getText() == null ? "" : f.getText().trim();
        String lastName = l.getText() == null ? "" : l.getText().trim();
        String email = e.getText() == null ? "" : e.getText().trim();
        String password = p.getText() == null ? "" : p.getText();
        String confirm = c.getText() == null ? "" : c.getText();

        if (firstName.isEmpty()) {
            show(fErr, "First name is required");
            valid = false;
        } else if (firstName.length() < 2) {
            show(fErr, "First name must be at least 2 characters");
            valid = false;
        }

        if (lastName.isEmpty()) {
            show(lErr, "Last name is required");
            valid = false;
        } else if (lastName.length() < 2) {
            show(lErr, "Last name must be at least 2 characters");
            valid = false;
        }

        if (email.isEmpty()) {
            show(eErr, "Email is required");
            valid = false;
        } else if (!EMAIL_VALIDATION_PATTERN.matcher(email).matches()) {
            show(eErr, "Enter a valid email address");
            valid = false;
        }

        if (password.length() < 6) {
            show(pErr, "Password must be at least 6 characters");
            valid = false;
        }
        if (confirm.isEmpty()) {
            show(cErr, "Please confirm your password");
            valid = false;
        } else if (!password.equals(confirm)) {
            show(cErr, "Passwords do not match");
            valid = false;
        }

        return valid;
    }

    private void show(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void clearError(Control c, Label l) {
        if (l != null) { l.setVisible(false); l.setManaged(false); }
        loginGeneralError.setVisible(false);
        loginGeneralError.setManaged(false);
    }

    private void hideRegisterGeneralError() {
        regGeneralError.setVisible(false);
        regGeneralError.setManaged(false);
    }

    private void handleUnifiedLogin(String email, String password) {
        try {
            boolean loggedIn = authController.adminLogin(email, password) || authController.customerLogin(email, password);
            if (loggedIn) {
				onAuthSuccess.run();
			} else { loginGeneralError.setText("✕ Invalid email or password"); loginGeneralError.setVisible(true); loginGeneralError.setManaged(true); }
        } catch (Exception ex) {
            loginGeneralError.setText("✕ " + ex.getMessage());
            loginGeneralError.setVisible(true);
            loginGeneralError.setManaged(true);
        }
    }

    private void handleRegister(String f, String l, String e, String ph, String a, String c, String s, String z, String co, String p, String cp) {
        try {
            if (authController.customerRegister(f, l, e, ph, a, c, s, z, co, p)) {
                switchForm(false);
            } else {
                regGeneralError.setText("✕ Email already exists");
                regGeneralError.setVisible(true);
                regGeneralError.setManaged(true);
            }
        } catch (Exception ex) {
            regGeneralError.setText("✕ " + ex.getMessage());
            regGeneralError.setVisible(true);
            regGeneralError.setManaged(true);
        }
    }
}
