package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.AuthService;
import tn.esprit.utils.AppNavigator;
import tn.esprit.utils.FormValidator;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button togglePasswordButton;
    @FXML private javafx.scene.control.Label feedbackLabel;

    private final AuthService authService = new AuthService();
    private boolean passwordVisible;

    @FXML
    public void initialize() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        FormValidator.attachClearOnInput(feedbackLabel, emailField, passwordField, visiblePasswordField);
    }

    public void handleLogin() {
        clearFeedback();

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showInlineError("Email and password are required.");
            return;
        }

        if (!FormValidator.isValidEmail(email)) {
            showInlineError("Enter a valid email address.");
            return;
        }

        User user = authService.authenticate(email, password);
        if (user == null) {
            showInlineError("Invalid email or password.");
            return;
        }

        String fxmlFile = switch (user.getRole()) {
            case "admin" -> "/fxml/admin_dashboard.fxml";
            case "doctor" -> "/fxml/doctor_dashboard.fxml";
            case "user" -> "/fxml/user_dashboard.fxml";
            default -> "";
        };

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            var scene = AppNavigator.createScene(loader.load(), getClass());

            if ("admin".equals(user.getRole())) {
                AdminDashboardController controller = loader.getController();
                controller.setLoggedInUser(user);
            } else if ("doctor".equals(user.getRole())) {
                DoctorDashboardController controller = loader.getController();
                controller.setLoggedInUser(user);
            } else if ("user".equals(user.getRole())) {
                UserDashboardController controller = loader.getController();
                controller.setLoggedInUser(user);
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            AppNavigator.applyStage(stage, scene, "PinkShield Dashboard");
        } catch (IOException e) {
            showAlert("Error", "Failed to load dashboard.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            var scene = AppNavigator.createScene(loader.load(), getClass());

            Stage stage = (Stage) emailField.getScene().getWindow();
            AppNavigator.applyStage(stage, scene, "PinkShield Register");
        } catch (IOException e) {
            showAlert("Error", "Failed to load register page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void handleTogglePassword() {
        passwordVisible = !passwordVisible;
        visiblePasswordField.setManaged(passwordVisible);
        visiblePasswordField.setVisible(passwordVisible);
        passwordField.setManaged(!passwordVisible);
        passwordField.setVisible(!passwordVisible);
        togglePasswordButton.setText(passwordVisible ? "Hide" : "Show");
    }

    private void showInlineError(String message) {
        FormValidator.markInvalid(emailField);
        FormValidator.markInvalid(passwordVisible ? visiblePasswordField : passwordField);
        FormValidator.setMessage(feedbackLabel, message, true);
    }

    private void clearFeedback() {
        FormValidator.clearStates(emailField, passwordField, visiblePasswordField);
        FormValidator.setMessage(feedbackLabel, "", true);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
