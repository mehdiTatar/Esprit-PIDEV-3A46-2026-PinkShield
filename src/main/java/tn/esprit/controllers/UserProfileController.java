package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

public class UserProfileController {
    @FXML private Label titleLabel;
    @FXML private Label avatarLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;

    private UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        // This will be called after FXML is loaded
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            titleLabel.setText("Edit Profile");

            // Set avatar
            String initial = currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()
                ? currentUser.getEmail().substring(0, 1).toUpperCase()
                : "?";
            avatarLabel.setText(initial);

            // Populate fields
            if (currentUser.getFirstName() != null) {
                firstNameField.setText(currentUser.getFirstName());
            }
            if (currentUser.getLastName() != null) {
                lastNameField.setText(currentUser.getLastName());
            }
            if (currentUser.getEmail() != null) {
                emailField.setText(currentUser.getEmail());
            }
            if (currentUser.getPhone() != null) {
                phoneField.setText(currentUser.getPhone());
            }
            if (currentUser.getAddress() != null) {
                addressField.setText(currentUser.getAddress());
            }
        }
    }

    public void handleSave() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showAlert("Error", "Email is required", Alert.AlertType.ERROR);
            return;
        }

        // Update user object
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);

        // Update password if provided
        if (!password.isEmpty()) {
            if (password.length() < 8) {
                showAlert("Error", "Password must be at least 8 characters", Alert.AlertType.ERROR);
                return;
            }
            currentUser.setPassword(password);
        }

        // Determine which table to update based on role
        String table = "user"; // default
        if ("admin".equals(currentUser.getRole())) {
            table = "admin";
        } else if ("doctor".equals(currentUser.getRole())) {
            table = "doctor";
        }

        // Save to database
        if (userService.updateUser(currentUser, table)) {
            showAlert("Success", "Profile updated successfully", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Error", "Failed to update profile", Alert.AlertType.ERROR);
        }
    }

    public void handleCancel() {
        // Reload data to discard changes
        loadUserData();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


