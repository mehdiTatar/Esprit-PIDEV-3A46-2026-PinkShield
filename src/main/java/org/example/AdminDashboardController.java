package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminDashboardController {

    private static final String DARK_MODE_CLASS = "dark-mode";

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private ToggleButton nightModeToggle;

    private boolean darkModeEnabled;

    @FXML
    public void initialize() {
        NavigationManager.getInstance().registerContentArea(contentArea);
        NavigationManager.getInstance().setDarkMode(darkModeEnabled);
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
        applyThemeClass(rootPane);
    }

    @FXML
    public void handleToggleNightMode() {
        darkModeEnabled = nightModeToggle != null && nightModeToggle.isSelected();
        if (nightModeToggle != null) {
            nightModeToggle.setText(darkModeEnabled ? "Day Mode" : "Night Mode");
        }
        NavigationManager.getInstance().setDarkMode(darkModeEnabled);
        applyThemeClass(rootPane);
        if (!contentArea.getChildren().isEmpty()) {
            applyThemeClass(contentArea.getChildren().getFirst());
        }
    }

    @FXML
    public void handleAppointments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminAppointment.fxml"));
            Parent appointmentView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(appointmentView);
            applyThemeClass(appointmentView);
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
            showEmptyPlaceholder("Appointments");
        }
    }

    @FXML
    public void handleParapharmacie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/parapharmacie.fxml"));
            Parent parapharmacieView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(parapharmacieView);
            applyThemeClass(parapharmacieView);
        } catch (IOException e) {
            System.err.println("Error loading parapharmacie: " + e.getMessage());
            showEmptyPlaceholder("Parapharmacie");
        }
    }

    @FXML
    public void handleRiskAnalyser() {
        showEmptyPlaceholder("Risk Analyser");
    }

    @FXML
    public void handleDashboard() {
        if (contentArea != null) {
            contentArea.getChildren().clear();
        }
    }

    @FXML
    public void handleLogout() {
        if (!confirmLogout()) {
            return;
        }

        UserSession.getInstance().cleanUserSession();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Auth.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("PinkShield - Sign In");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login page: " + e.getMessage());
        }
    }

    private boolean confirmLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to log out?");
        confirmAlert.setContentText("You will be returned to the sign in page.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void applyThemeClass(Node node) {
        if (node == null) {
            return;
        }

        if (darkModeEnabled) {
            if (!node.getStyleClass().contains(DARK_MODE_CLASS)) {
                node.getStyleClass().add(DARK_MODE_CLASS);
            }
        } else {
            node.getStyleClass().remove(DARK_MODE_CLASS);
        }
    }

    private void showEmptyPlaceholder(String sectionName) {
        if (contentArea == null) {
            return;
        }
        System.err.println("Section unavailable: " + sectionName);
        contentArea.getChildren().clear();
    }
}

