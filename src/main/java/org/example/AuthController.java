package org.example;

import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.animation.Interpolator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AuthController {

    private static final Color PATIENT_COLOR = Color.web("#e84393");
    private static final Color DOCTOR_COLOR = Color.web("#0984e3");
    private static final Duration THEME_ANIMATION_DURATION = Duration.millis(500);
    private static final int MAX_RECENT_EMAILS = 8;
    private static final Path RECENT_EMAILS_FILE = Path.of(System.getProperty("user.home", "."), ".pinkshield-recent-emails.txt");

    @FXML private StackPane authRoot;
    @FXML private StackPane logoContainer;
    @FXML private SVGPath shieldPath;
    @FXML private SVGPath pulsePath;

    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    @FXML private TextField txtLoginEmail;
    @FXML private PasswordField txtLoginPassword;
    @FXML private TextField txtLoginPasswordVisible;

    @FXML private TextField txtRegisterFullName;
    @FXML private TextField txtRegisterEmail;
    @FXML private PasswordField txtRegisterPassword;
    @FXML private PasswordField txtRegisterConfirmPassword;
    @FXML private TextField txtRegisterLicenseId;
    @FXML private ComboBox<String> cmbRegisterSpecialty;

    @FXML private VBox doctorFieldsBox;
    @FXML private ToggleButton btnPatientMode;
    @FXML private ToggleButton btnDoctorMode;
    @FXML private Button btnSignIn;

    private final ToggleGroup roleGroup = new ToggleGroup();

    @FXML private Label loginErrorLabel;
    @FXML private Label registerErrorLabel;

    @FXML private Button btnTogglePassword;

    private final ServiceUser userService = new ServiceUser();
    private boolean showPassword;
    private final DoubleProperty themeProgress = new SimpleDoubleProperty(0);
    private Set<Node> inputRows;
    private Set<Node> fieldIcons;
    private ParallelTransition logoHeartbeat;
    private final ContextMenu emailSuggestionsMenu = new ContextMenu();
    private final List<String> recentLoginEmails = new ArrayList<>();

    private static final java.util.List<String> SPECIALTIES = java.util.Arrays.asList(
            "Cardiology",
            "Neurology",
            "Orthopedics",
            "Dermatology",
            "Pediatrics",
            "Internal Medicine",
            "Emergency Medicine",
            "Gynecology",
            "Psychiatry",
            "General Surgery"
    );

    @FXML
    public void initialize() {
        btnPatientMode.setToggleGroup(roleGroup);
        btnDoctorMode.setToggleGroup(roleGroup);
        btnPatientMode.setSelected(true);
        cmbRegisterSpecialty.setItems(FXCollections.observableArrayList(SPECIALTIES));
        registerBox.setVisible(false);
        registerBox.setManaged(false);
        txtLoginPasswordVisible.setVisible(false);
        txtLoginPasswordVisible.setManaged(false);
        loadRecentLoginEmails();
        setupLoginEmailSuggestions();
        applyRoleMode();

        themeProgress.addListener((obs, oldValue, newValue) ->
                applyThemeColors(PATIENT_COLOR.interpolate(DOCTOR_COLOR, newValue.doubleValue())));

        Platform.runLater(() -> {
            inputRows = authRoot.lookupAll(".auth-input-row");
            fieldIcons = authRoot.lookupAll(".auth-field-icon");
            bindLogoScale();
            applyThemeColors(PATIENT_COLOR);
            startLogoHeartbeatAnimation();
        });
    }

    @FXML
    private void switchToRegister() {
        switchForm();
    }

    @FXML
    private void switchToLogin() {
        switchForm();
    }

    private void switchForm() {
        boolean registerVisible = registerBox.isManaged() && registerBox.isVisible();
        if (registerVisible) {
            switchForm(loginBox, registerBox);
        } else {
            switchForm(registerBox, loginBox);
        }
    }

    private void switchForm(VBox toShow, VBox toHide) {
        clearMessages();

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), toHide);
        slideOut.setFromX(0);
        slideOut.setToX(-30);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toHide);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ParallelTransition out = new ParallelTransition(slideOut, fadeOut);
        out.setOnFinished(event -> {
            toHide.setVisible(false);
            toHide.setManaged(false);
            toHide.setOpacity(1);
            toHide.setTranslateX(0);

            toShow.setManaged(true);
            toShow.setVisible(true);
            toShow.setOpacity(0);
            toShow.setTranslateX(30);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(260), toShow);
            slideIn.setFromX(30);
            slideIn.setToX(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(260), toShow);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            new ParallelTransition(slideIn, fadeIn).play();
        });

        out.play();
    }

    @FXML
    private void handleSignIn() {
        clearMessages();

        String email = txtLoginEmail.getText() == null ? "" : txtLoginEmail.getText().trim();
        String password = getLoginPassword();
        String selectedSection = getSelectedRole();

        if (email.isEmpty() || password.isEmpty() || selectedSection.isEmpty()) {
            showError(loginErrorLabel, "Email, password, and section are required.");
            return;
        }

        AuthUser user = userService.authenticateUser(email, password);
        if (user == null) {
            showError(loginErrorLabel, "Invalid credentials.");
            return;
        }

        String expectedRole = "DOCTOR".equals(selectedSection) ? "ADMIN" : "PATIENT";
        if (!expectedRole.equalsIgnoreCase(user.getRole())) {
            showError(loginErrorLabel, "This account does not belong to the selected section.");
            return;
        }

        UserSession.getInstance().setCurrentUser(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );

        rememberLoginEmail(email);

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            openScreen("/AdminDashboard.fxml", "PinkShield - Admin Dashboard");
        } else {
            openScreen("/Dashboard.fxml", "PinkShield - Dashboard");
        }
    }

    @FXML
    private void handleSkipPatientLogin() {
        clearMessages();
        UserSession.getInstance().cleanUserSession();
        openScreen("/Dashboard.fxml", "PinkShield - Dashboard");
    }

    @FXML
    private void handleSkipDoctorLogin() {
        clearMessages();
        UserSession.getInstance().cleanUserSession();
        openScreen("/AdminDashboard.fxml", "PinkShield - Admin Dashboard");
    }

    @FXML
    private void handleSignUp() {
        clearMessages();

        String fullName = safeText(txtRegisterFullName);
        String email = safeText(txtRegisterEmail);
        String password = safeText(txtRegisterPassword);
        String confirm = safeText(txtRegisterConfirmPassword);
        String selectedSection = getSelectedRole();
        String licenseId = safeText(txtRegisterLicenseId);
        String specialty = cmbRegisterSpecialty.getValue() == null ? "" : cmbRegisterSpecialty.getValue().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || selectedSection.isEmpty()) {
            showError(registerErrorLabel, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showError(registerErrorLabel, "Passwords do not match.");
            return;
        }

        String role = "DOCTOR".equals(selectedSection) ? "ADMIN" : "PATIENT";
        boolean created;
        if ("ADMIN".equals(role)) {
            if (specialty.isEmpty() || licenseId.isEmpty()) {
                showError(registerErrorLabel, "Specialty and Medical License ID are required for doctors.");
                return;
            }
            if (!licenseId.matches("\\d+")) {
                showError(registerErrorLabel, "Medical License ID must be numeric.");
                return;
            }
            created = userService.registerAdmin(fullName, email, specialty, licenseId, password);
        } else {
            created = userService.registerWithRole(fullName, email, password, role);
        }

        if (!created) {
            showError(registerErrorLabel, "This email is already registered.");
            return;
        }

        clearRegisterFields();
        switchToLogin();
        showError(loginErrorLabel, "Account created. Sign in with your section.");
        loginErrorLabel.getStyleClass().add("auth-info-label");
    }

    @FXML
    private void toggleLoginPasswordVisibility() {
        showPassword = !showPassword;

        if (showPassword) {
            txtLoginPasswordVisible.setText(txtLoginPassword.getText());
            txtLoginPasswordVisible.setVisible(true);
            txtLoginPasswordVisible.setManaged(true);
            txtLoginPassword.setVisible(false);
            txtLoginPassword.setManaged(false);
            btnTogglePassword.setText("🙈");
        } else {
            txtLoginPassword.setText(txtLoginPasswordVisible.getText());
            txtLoginPassword.setVisible(true);
            txtLoginPassword.setManaged(true);
            txtLoginPasswordVisible.setVisible(false);
            txtLoginPasswordVisible.setManaged(false);
            btnTogglePassword.setText("👁");
        }
    }

    private void openScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) loginBox.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showError(loginErrorLabel, "Unable to open dashboard.");
        }
    }

    private String getLoginPassword() {
        return showPassword ? safeText(txtLoginPasswordVisible) : safeText(txtLoginPassword);
    }

    private String safeText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private void clearRegisterFields() {
        txtRegisterFullName.clear();
        txtRegisterEmail.clear();
        txtRegisterPassword.clear();
        txtRegisterConfirmPassword.clear();
        txtRegisterLicenseId.clear();
        cmbRegisterSpecialty.getSelectionModel().clearSelection();
    }

    @FXML
    private void handlePatientMode() {
        applyRoleMode();
        animateThemeTransition(false);
    }

    @FXML
    private void handleDoctorMode() {
        applyRoleMode();
        animateThemeTransition(true);
    }

    private void applyRoleMode() {
        boolean doctorSelected = btnDoctorMode != null && btnDoctorMode.isSelected();
        if (doctorFieldsBox != null) {
            doctorFieldsBox.setVisible(doctorSelected);
            doctorFieldsBox.setManaged(doctorSelected);
        }
    }

    private void animateThemeTransition(boolean doctorMode) {
        double target = doctorMode ? 1.0 : 0.0;
        Timeline timeline = new Timeline(
                new KeyFrame(THEME_ANIMATION_DURATION, new KeyValue(themeProgress, target, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    private void applyThemeColors(Color accent) {
        if (accent == null) {
            return;
        }

        String accentHex = toHex(accent);
        String softAccentHex = toHex(accent.interpolate(Color.WHITE, 0.62));
        String subtleAccentHex = toHex(accent.interpolate(Color.WHITE, 0.80));

        if (shieldPath != null) {
            shieldPath.setStroke(accent);
            shieldPath.setFill(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.12));
        }
        if (pulsePath != null) {
            pulsePath.setStroke(accent);
            pulsePath.setFill(Color.TRANSPARENT);
        }

        if (btnSignIn != null) {
            btnSignIn.setStyle("-fx-background-color: " + accentHex + "; -fx-text-fill: #ffffff;");
        }

        if (inputRows != null) {
            for (Node row : inputRows) {
                row.setStyle("-fx-padding: 4 0 0 0; -fx-alignment: center-left; -fx-border-color: transparent transparent " + softAccentHex + " transparent; -fx-border-width: 0 0 1 0;");
            }
        }

        if (fieldIcons != null) {
            for (Node icon : fieldIcons) {
                icon.setStyle("-fx-icon-color: " + accentHex + ";");
            }
        }

        setAccentBorder(txtLoginEmail, subtleAccentHex);
        setAccentBorder(txtLoginPassword, subtleAccentHex);
        setAccentBorder(txtLoginPasswordVisible, subtleAccentHex);
        setAccentBorder(txtRegisterFullName, subtleAccentHex);
        setAccentBorder(txtRegisterEmail, subtleAccentHex);
        setAccentBorder(txtRegisterPassword, subtleAccentHex);
        setAccentBorder(txtRegisterConfirmPassword, subtleAccentHex);
        setAccentBorder(txtRegisterLicenseId, subtleAccentHex);
        if (cmbRegisterSpecialty != null) {
            cmbRegisterSpecialty.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent " + subtleAccentHex + " transparent; -fx-border-width: 0 0 1.3 0; -fx-border-radius: 0; -fx-background-radius: 0; -fx-padding: 6 0 6 0;");
        }

        applyRoleToggleAccent(accentHex);
    }

    private void applyRoleToggleAccent(String accentHex) {
        if (btnPatientMode == null || btnDoctorMode == null) {
            return;
        }

        String passive = "-fx-background-color: #eef0f6; -fx-text-fill: #2d3436; -fx-border-color: #d7dbe6;";
        String active = "-fx-background-color: " + accentHex + "; -fx-text-fill: #ffffff; -fx-border-color: " + accentHex + ";";

        btnPatientMode.setStyle((btnPatientMode.isSelected() ? active : passive) + " -fx-background-radius: 999; -fx-border-radius: 999; -fx-padding: 8 16;");
        btnDoctorMode.setStyle((btnDoctorMode.isSelected() ? active : passive) + " -fx-background-radius: 999; -fx-border-radius: 999; -fx-padding: 8 16;");
    }

    private void setAccentBorder(TextInputControl field, String borderHex) {
        if (field == null) {
            return;
        }
        field.setStyle("-fx-background-color: transparent; -fx-border-color: transparent transparent " + borderHex + " transparent; -fx-border-width: 0 0 1.3 0; -fx-border-radius: 0; -fx-background-radius: 0; -fx-padding: 10 6 10 6;");
    }

    private void bindLogoScale() {
        if (logoContainer == null || shieldPath == null || pulsePath == null) {
            return;
        }

        Runnable scaler = () -> {
            Bounds bounds = logoContainer.getLayoutBounds();
            double size = Math.min(bounds.getWidth(), bounds.getHeight());
            double scale = Math.max(1.6, size / 120.0);
            shieldPath.setScaleX(scale);
            shieldPath.setScaleY(scale);
            pulsePath.setScaleX(scale);
            pulsePath.setScaleY(scale);
        };

        logoContainer.widthProperty().addListener((obs, oldValue, newValue) -> scaler.run());
        logoContainer.heightProperty().addListener((obs, oldValue, newValue) -> scaler.run());
        scaler.run();
    }

    private void startLogoHeartbeatAnimation() {
        if (logoContainer == null) {
            return;
        }
        if (logoHeartbeat != null) {
            logoHeartbeat.stop();
        }

        ScaleTransition scalePulse = new ScaleTransition(Duration.millis(950), logoContainer);
        scalePulse.setFromX(1.0);
        scalePulse.setFromY(1.0);
        scalePulse.setToX(1.05);
        scalePulse.setToY(1.05);
        scalePulse.setAutoReverse(true);
        scalePulse.setCycleCount(Timeline.INDEFINITE);

        FadeTransition fadePulse = new FadeTransition(Duration.millis(950), logoContainer);
        fadePulse.setFromValue(1.0);
        fadePulse.setToValue(0.88);
        fadePulse.setAutoReverse(true);
        fadePulse.setCycleCount(Timeline.INDEFINITE);

        logoHeartbeat = new ParallelTransition(scalePulse, fadePulse);
        logoHeartbeat.setCycleCount(Timeline.INDEFINITE);
        logoHeartbeat.play();
    }

    private String toHex(Color color) {
        int red = (int) Math.round(color.getRed() * 255);
        int green = (int) Math.round(color.getGreen() * 255);
        int blue = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02x%02x%02x", red, green, blue);
    }

    private String getSelectedRole() {
        return btnDoctorMode != null && btnDoctorMode.isSelected() ? "DOCTOR" : "PATIENT";
    }

    private void setupLoginEmailSuggestions() {
        if (txtLoginEmail == null) {
            return;
        }

        txtLoginEmail.textProperty().addListener((obs, oldText, newText) -> showEmailSuggestions(newText));
        txtLoginEmail.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                showEmailSuggestions(txtLoginEmail.getText());
            } else {
                emailSuggestionsMenu.hide();
            }
        });
    }

    private void showEmailSuggestions(String query) {
        if (txtLoginEmail == null) {
            return;
        }

        String normalized = query == null ? "" : query.trim().toLowerCase();
        List<String> matches = new ArrayList<>();

        for (String email : recentLoginEmails) {
            if (normalized.isEmpty() || email.toLowerCase().contains(normalized)) {
                matches.add(email);
            }
        }

        if (matches.isEmpty() || !txtLoginEmail.isFocused()) {
            emailSuggestionsMenu.hide();
            return;
        }

        emailSuggestionsMenu.getItems().clear();
        for (String email : matches) {
            MenuItem item = new MenuItem(email);
            item.setOnAction(event -> {
                txtLoginEmail.setText(email);
                txtLoginEmail.positionCaret(email.length());
                emailSuggestionsMenu.hide();
            });
            emailSuggestionsMenu.getItems().add(item);
        }

        if (!emailSuggestionsMenu.isShowing()) {
            emailSuggestionsMenu.show(txtLoginEmail, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    private void loadRecentLoginEmails() {
        recentLoginEmails.clear();
        try {
            if (!Files.exists(RECENT_EMAILS_FILE)) {
                return;
            }

            LinkedHashSet<String> dedup = new LinkedHashSet<>();
            for (String line : Files.readAllLines(RECENT_EMAILS_FILE, StandardCharsets.UTF_8)) {
                String email = line == null ? "" : line.trim();
                if (!email.isEmpty()) {
                    dedup.add(email);
                }
                if (dedup.size() >= MAX_RECENT_EMAILS) {
                    break;
                }
            }
            recentLoginEmails.addAll(dedup);
        } catch (IOException ignored) {
            // Keep login flow robust even if history file is unavailable.
        }
    }

    private void rememberLoginEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return;
        }

        recentLoginEmails.removeIf(existing -> existing.equalsIgnoreCase(normalized));
        recentLoginEmails.add(0, normalized);
        while (recentLoginEmails.size() > MAX_RECENT_EMAILS) {
            recentLoginEmails.remove(recentLoginEmails.size() - 1);
        }

        try {
            Files.write(RECENT_EMAILS_FILE, recentLoginEmails, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // History persistence is optional and should not block successful sign-in.
        }
    }

    private void clearMessages() {
        clearMessage(loginErrorLabel);
        clearMessage(registerErrorLabel);
    }

    private void clearMessage(Label label) {
        label.setText("");
        label.setOpacity(0);
        label.getStyleClass().remove("auth-info-label");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        FadeTransition fade = new FadeTransition(Duration.millis(220), label);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
}


