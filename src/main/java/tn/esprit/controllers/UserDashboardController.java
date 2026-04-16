package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.AppointmentService;
import tn.esprit.services.UserService;
import tn.esprit.utils.AppNavigator;
import tn.esprit.utils.FormValidator;

import java.io.IOException;

public class UserDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label dashboardRoleLabel;
    @FXML private Label totalAppointmentsLabel;
    @FXML private Label upcomingAppointmentsLabel;
    @FXML private Label patientNameSummaryLabel;
    @FXML private Label patientEmailSummaryLabel;
    @FXML private Label patientPhoneSummaryLabel;
    @FXML private Label patientAddressSummaryLabel;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    @FXML private StackPane mainContent;
    @FXML private VBox dashboardView;
    @FXML private VBox profileEditView;
    @FXML private Button navDashboard;
    @FXML private Button navAppointments;
    @FXML private Button navProfileEdit;
    @FXML private Button navBlog;
    @FXML private Button navProducts;
    @FXML private Button navDailyCheckIn;

    private final UserService userService = new UserService();
    private final AppointmentService appointmentService = new AppointmentService();
    private User loggedInUser;

    @FXML
    public void initialize() {
        FormValidator.attachClearOnInput(feedbackLabel, fullNameField, emailField, phoneField, addressField);
    }

    public void setLoggedInUser(User user) {
        loggedInUser = user;
        if (user == null) {
            return;
        }

        welcomeLabel.setText("Welcome back, " + user.getFullName());
        dashboardRoleLabel.setText("Patient dashboard");
        populateProfile();
        populateDashboardSummary();
        updateAppointmentCards();
    }

    @FXML
    public void showDashboard() {
        updateNavStyles(navDashboard);
        populateDashboardSummary();
        updateAppointmentCards();
        mainContent.getChildren().setAll(dashboardView);
    }

    @FXML
    public void showProfileEdit() {
        updateNavStyles(navProfileEdit);
        populateProfile();
        FormValidator.setMessage(feedbackLabel, "", true);
        mainContent.getChildren().setAll(profileEditView);
    }

    @FXML
    public void showBlog() {
        updateNavStyles(navBlog);
        loadView("/fxml/blog_list.fxml");
    }

    @FXML
    public void showAppointments() {
        updateNavStyles(navAppointments);
        loadView("/fxml/appointment_list.fxml");
    }

    @FXML
    public void showProducts() {
        updateNavStyles(navProducts);
        loadView("/fxml/product_list.fxml");
    }

    @FXML
    public void showDailyCheckIn() {
        updateNavStyles(navDailyCheckIn);
        loadView("/fxml/daily_tracking.fxml");
    }

    @FXML
    public void handleEditProfile() {
        populateProfile();
        FormValidator.setMessage(feedbackLabel, "", true);
    }

    @FXML
    public void handleSaveProfile() {
        if (loggedInUser == null) {
            return;
        }

        FormValidator.clearStates(fullNameField, emailField, phoneField, addressField);

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (fullName.length() < 3) {
            FormValidator.markInvalid(fullNameField);
            FormValidator.setMessage(feedbackLabel, "Full name must contain at least 3 characters.", true);
            return;
        }
        if (!FormValidator.isValidEmail(email)) {
            FormValidator.markInvalid(emailField);
            FormValidator.setMessage(feedbackLabel, "Enter a valid email address.", true);
            return;
        }
        if (!FormValidator.isValidPhone(phone)) {
            FormValidator.markInvalid(phoneField);
            FormValidator.setMessage(feedbackLabel, "Phone number must contain 8 to 20 digits.", true);
            return;
        }
        if (address.isEmpty()) {
            FormValidator.markInvalid(addressField);
            FormValidator.setMessage(feedbackLabel, "Address is required.", true);
            return;
        }

        loggedInUser.setFullName(fullName);
        loggedInUser.setEmail(email);
        loggedInUser.setPhone(phone);
        loggedInUser.setAddress(address);

        String validationMessage = userService.validateUser(loggedInUser, loggedInUser.getPassword(), true);
        if (validationMessage != null) {
            if (validationMessage.toLowerCase().contains("email")) {
                FormValidator.markInvalid(emailField);
            }
            FormValidator.setMessage(feedbackLabel, validationMessage, true);
            return;
        }

        if (userService.updateUser(loggedInUser)) {
            welcomeLabel.setText("Welcome back, " + loggedInUser.getFullName());
            populateDashboardSummary();
            FormValidator.setMessage(feedbackLabel, "Profile updated successfully.", false);
        } else {
            FormValidator.setMessage(feedbackLabel, "Failed to update profile.", true);
        }
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = AppNavigator.createScene(loader.load(), getClass());

            Stage stage = (Stage) mainContent.getScene().getWindow();
            AppNavigator.applyStage(stage, scene, "PinkShield Login");
        } catch (IOException e) {
            showAlert("Error", "Failed to load login page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void populateProfile() {
        fullNameField.setText(loggedInUser.getFullName());
        emailField.setText(loggedInUser.getEmail());
        phoneField.setText(loggedInUser.getPhone() == null ? "" : loggedInUser.getPhone());
        addressField.setText(loggedInUser.getAddress() == null ? "" : loggedInUser.getAddress());
    }

    private void populateDashboardSummary() {
        if (loggedInUser == null) {
            return;
        }
        patientNameSummaryLabel.setText("Name: " + loggedInUser.getFullName());
        patientEmailSummaryLabel.setText("Email: " + loggedInUser.getEmail());
        patientPhoneSummaryLabel.setText("Phone: " + (loggedInUser.getPhone() == null || loggedInUser.getPhone().isBlank()
                ? "Not provided"
                : loggedInUser.getPhone()));
        patientAddressSummaryLabel.setText("Address: " + (loggedInUser.getAddress() == null || loggedInUser.getAddress().isBlank()
                ? "Not provided"
                : loggedInUser.getAddress()));
    }

    private void updateAppointmentCards() {
        totalAppointmentsLabel.setText(String.valueOf(appointmentService.countAppointmentsByPatient(loggedInUser.getId())));
        upcomingAppointmentsLabel.setText(String.valueOf(appointmentService.countUpcomingAppointmentsByPatient(loggedInUser.getId())));
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BlogListController) {
                ((BlogListController) controller).setCurrentUser(loggedInUser);
            } else if (controller instanceof BlogDetailController) {
                ((BlogDetailController) controller).setCurrentUser(loggedInUser);
            } else if (controller instanceof AppointmentListController) {
                ((AppointmentListController) controller).setCurrentUser(loggedInUser);
            } else if (controller instanceof ProductListController) {
                ((ProductListController) controller).setCurrentUser(loggedInUser);
            } else if (controller instanceof DailyTrackingController) {
                ((DailyTrackingController) controller).setCurrentUser(loggedInUser);
            }

            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            showAlert("Error", "Could not load view: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateNavStyles(Button activeBtn) {
        navDashboard.getStyleClass().remove("active");
        navAppointments.getStyleClass().remove("active");
        navProfileEdit.getStyleClass().remove("active");
        navBlog.getStyleClass().remove("active");
        navProducts.getStyleClass().remove("active");
        navDailyCheckIn.getStyleClass().remove("active");
        activeBtn.getStyleClass().add("active");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
