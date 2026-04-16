package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.AppNavigator;
import tn.esprit.utils.FormValidator;

import java.io.IOException;
import java.util.List;

public class RegisterController {
    private static final String PATIENT_ROLE = UserService.ROLE_USER;
    private static final String DOCTOR_ROLE = UserService.ROLE_DOCTOR;

    @FXML private StackPane registerRoot;
    @FXML private ToggleGroup roleGroup;
    @FXML private ToggleButton patientToggle;
    @FXML private ToggleButton doctorToggle;
    @FXML private VBox patientFields;
    @FXML private VBox doctorFields;
    @FXML private TextField patientFirstNameField;
    @FXML private Label patientFirstNameErrorLabel;
    @FXML private TextField patientLastNameField;
    @FXML private Label patientLastNameErrorLabel;
    @FXML private TextField phoneField;
    @FXML private Label phoneErrorLabel;
    @FXML private TextField addressField;
    @FXML private Label addressErrorLabel;
    @FXML private TextField firstNameField;
    @FXML private Label firstNameErrorLabel;
    @FXML private TextField lastNameField;
    @FXML private Label lastNameErrorLabel;
    @FXML private ComboBox<String> specialityField;
    @FXML private Label specialityErrorLabel;
    @FXML private TextField emailField;
    @FXML private Label emailErrorLabel;
    @FXML private PasswordField passwordField;
    @FXML private Label passwordErrorLabel;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Button registerBtn;
    @FXML private Label roleBadgeLabel;
    @FXML private Label roleHeadingLabel;
    @FXML private Label roleDescriptionLabel;
    @FXML private Label benefit1Label;
    @FXML private Label benefit2Label;
    @FXML private Label benefit3Label;
    @FXML private Label benefit4Label;
    @FXML private Label roleSummaryTitleLabel;
    @FXML private Label roleSummaryTextLabel;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        specialityField.getItems().setAll(
                "Cardiology",
                "Dermatology",
                "General Medicine",
                "Gynecology",
                "Neurology",
                "Orthopedics",
                "Pediatrics",
                "Psychiatry"
        );
        specialityField.setEditable(true);

        patientToggle.setUserData(PATIENT_ROLE);
        doctorToggle.setUserData(DOCTOR_ROLE);

        if (!registerRoot.getStyleClass().contains("register-screen")) {
            registerRoot.getStyleClass().add("register-screen");
        }

        attachFieldListeners();

        roleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                } else {
                    patientToggle.setSelected(true);
                }
                return;
            }

            clearValidationState();
            applyRoleTheme((String) newToggle.getUserData());
        });

        if (roleGroup.getSelectedToggle() == null) {
            patientToggle.setSelected(true);
        }

        applyRoleTheme(getSelectedRole());
    }

    @FXML
    public void handleRegister() {
        clearValidationState();

        User user = buildUserFromForm();
        if (user == null) {
            return;
        }

        String validationMessage = userService.validateUser(user, confirmPasswordField.getText(), false);
        if (validationMessage != null) {
            applyServiceValidationMessage(validationMessage);
            return;
        }

        boolean success = userService.createUser(user);
        if (!success) {
            FormValidator.setMessage(feedbackLabel, "Registration failed. Please verify the database connection.", true);
            return;
        }

        showAlert("Success", "Registration successful. Please sign in.", Alert.AlertType.INFORMATION);
        goToLogin();
    }

    @FXML
    public void handleBackToLogin() {
        goToLogin();
    }

    private void attachFieldListeners() {
        attachTextFieldListener(emailField, emailErrorLabel);
        attachTextFieldListener(patientFirstNameField, patientFirstNameErrorLabel);
        attachTextFieldListener(patientLastNameField, patientLastNameErrorLabel);
        attachTextFieldListener(phoneField, phoneErrorLabel);
        attachTextFieldListener(addressField, addressErrorLabel);
        attachTextFieldListener(firstNameField, firstNameErrorLabel);
        attachTextFieldListener(lastNameField, lastNameErrorLabel);
        attachTextFieldListener(passwordField, passwordErrorLabel);
        attachTextFieldListener(confirmPasswordField, confirmPasswordErrorLabel);

        passwordField.textProperty().addListener((obs, oldValue, newValue) -> clearFieldError(confirmPasswordField, confirmPasswordErrorLabel));

        specialityField.valueProperty().addListener((obs, oldValue, newValue) -> clearFieldError(specialityField, specialityErrorLabel));
        specialityField.getEditor().textProperty().addListener((obs, oldValue, newValue) -> clearFieldError(specialityField, specialityErrorLabel));
    }

    private void attachTextFieldListener(TextField field, Label errorLabel) {
        field.textProperty().addListener((obs, oldValue, newValue) -> clearFieldError(field, errorLabel));
    }

    private User buildUserFromForm() {
        String role = getSelectedRole();
        boolean valid = true;

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty()) {
            setFieldError(emailField, emailErrorLabel, "Email is required.");
            valid = false;
        } else if (!FormValidator.isValidEmail(email)) {
            setFieldError(emailField, emailErrorLabel, "Enter a valid email address.");
            valid = false;
        }

        if (password.isBlank()) {
            setFieldError(passwordField, passwordErrorLabel, "Password is required.");
            valid = false;
        } else if (password.length() < 8) {
            setFieldError(passwordField, passwordErrorLabel, "Password must contain at least 8 characters.");
            valid = false;
        }

        if (confirmPassword.isBlank()) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLabel, "Password confirmation is required.");
            valid = false;
        } else if (!confirmPassword.equals(password)) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLabel, "Password confirmation does not match.");
            valid = false;
        }

        User user = new User();
        user.setRole(role);
        user.setEmail(email);
        user.setPassword(password);

        if (DOCTOR_ROLE.equals(role)) {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String speciality = getSelectedSpeciality();

            if (!FormValidator.isValidName(firstName)) {
                setFieldError(firstNameField, firstNameErrorLabel, "First name is required and must contain only letters.");
                valid = false;
            }
            if (!FormValidator.isValidName(lastName)) {
                setFieldError(lastNameField, lastNameErrorLabel, "Last name is required and must contain only letters.");
                valid = false;
            }
            if (speciality.isBlank()) {
                setFieldError(specialityField, specialityErrorLabel, "Doctor speciality is required.");
                valid = false;
            }

            if (!valid) {
                return null;
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setFullName((firstName + " " + lastName).trim());
            user.setSpeciality(speciality);
            return user;
        }

        String patientFirstName = patientFirstNameField.getText().trim();
        String patientLastName = patientLastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();

        if (!FormValidator.isValidName(patientFirstName)) {
            setFieldError(patientFirstNameField, patientFirstNameErrorLabel, "First name is required and must contain only letters.");
            valid = false;
        }
        if (!FormValidator.isValidName(patientLastName)) {
            setFieldError(patientLastNameField, patientLastNameErrorLabel, "Last name is required and must contain only letters.");
            valid = false;
        }
        if (phone.isEmpty()) {
            setFieldError(phoneField, phoneErrorLabel, "Phone number is required.");
            valid = false;
        } else if (!FormValidator.isValidPhone(phone)) {
            setFieldError(phoneField, phoneErrorLabel, "Phone number must contain 8 to 20 digits.");
            valid = false;
        }
        if (address.isEmpty()) {
            setFieldError(addressField, addressErrorLabel, "Address is required.");
            valid = false;
        }

        if (!valid) {
            return null;
        }

        user.setFullName((patientFirstName + " " + patientLastName).trim());
        user.setPhone(phone);
        user.setAddress(address);
        return user;
    }

    private void applyServiceValidationMessage(String message) {
        markFieldForMessage(message);
        FormValidator.setMessage(feedbackLabel, "", true);
    }

    private String getSelectedRole() {
        return doctorToggle.isSelected() ? DOCTOR_ROLE : PATIENT_ROLE;
    }

    private String getSelectedSpeciality() {
        String value = specialityField.getValue();
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        String editorText = specialityField.getEditor().getText();
        return editorText == null ? "" : editorText.trim();
    }

    private void applyRoleTheme(String role) {
        boolean doctor = DOCTOR_ROLE.equals(role);

        patientFields.setVisible(!doctor);
        patientFields.setManaged(!doctor);
        doctorFields.setVisible(doctor);
        doctorFields.setManaged(doctor);

        registerRoot.getStyleClass().removeAll(List.of("patient-theme", "doctor-theme"));
        registerRoot.getStyleClass().add(doctor ? "doctor-theme" : "patient-theme");

        if (doctor) {
            roleBadgeLabel.setText("D");
            roleHeadingLabel.setText("Join as a Doctor");
            roleDescriptionLabel.setText("Connect with patients, manage appointments, and deliver structured care through your PinkShield workspace.");
            benefit1Label.setText("Secure and compliant practitioner onboarding");
            benefit2Label.setText("Smart appointment scheduling and follow-up");
            benefit3Label.setText("Direct patient communication flow");
            benefit4Label.setText("Operational insights for your practice");
            roleSummaryTitleLabel.setText("Doctor account");
            roleSummaryTextLabel.setText("Create a clinician profile with your name and speciality so patients can discover and book you.");
            registerBtn.setText("Create Doctor Account");
        } else {
            roleBadgeLabel.setText("P");
            roleHeadingLabel.setText("Join as a Patient");
            roleDescriptionLabel.setText("Take control of your health, connect with doctors, and manage your care in one protected place.");
            benefit1Label.setText("Privacy and data protection");
            benefit2Label.setText("Direct access to doctors");
            benefit3Label.setText("Centralized health records");
            benefit4Label.setText("Health reminders and alerts");
            roleSummaryTitleLabel.setText("Patient account");
            roleSummaryTextLabel.setText("Register to book appointments, track your health information, and keep your profile ready for care.");
            registerBtn.setText("Register as Patient");
        }
    }

    private void clearValidationState() {
        FormValidator.clearStates(
                emailField,
                patientFirstNameField,
                patientLastNameField,
                phoneField,
                addressField,
                firstNameField,
                lastNameField,
                specialityField,
                passwordField,
                confirmPasswordField
        );

        clearErrorLabel(emailErrorLabel);
        clearErrorLabel(patientFirstNameErrorLabel);
        clearErrorLabel(patientLastNameErrorLabel);
        clearErrorLabel(phoneErrorLabel);
        clearErrorLabel(addressErrorLabel);
        clearErrorLabel(firstNameErrorLabel);
        clearErrorLabel(lastNameErrorLabel);
        clearErrorLabel(specialityErrorLabel);
        clearErrorLabel(passwordErrorLabel);
        clearErrorLabel(confirmPasswordErrorLabel);

        FormValidator.setMessage(feedbackLabel, "", true);
    }

    private void setFieldError(Control control, Label errorLabel, String message) {
        FormValidator.markInvalid(control);
        errorLabel.setText(message);
        errorLabel.setManaged(true);
        errorLabel.setVisible(true);
    }

    private void clearFieldError(Control control, Label errorLabel) {
        FormValidator.clearFieldState(control);
        clearErrorLabel(errorLabel);
        FormValidator.setMessage(feedbackLabel, "", true);
    }

    private void clearErrorLabel(Label errorLabel) {
        errorLabel.setText("");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);
    }

    private void markFieldForMessage(String message) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("email")) {
            setFieldError(emailField, emailErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("password confirmation")) {
            setFieldError(confirmPasswordField, confirmPasswordErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("password")) {
            setFieldError(passwordField, passwordErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("phone")) {
            setFieldError(phoneField, phoneErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("address")) {
            setFieldError(addressField, addressErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("speciality")) {
            setFieldError(specialityField, specialityErrorLabel, message);
            return;
        }
        if (lowerMessage.contains("first name")) {
            if (DOCTOR_ROLE.equals(getSelectedRole())) {
                setFieldError(firstNameField, firstNameErrorLabel, message);
            } else {
                setFieldError(patientFirstNameField, patientFirstNameErrorLabel, message);
            }
            return;
        }
        if (lowerMessage.contains("last name")) {
            if (DOCTOR_ROLE.equals(getSelectedRole())) {
                setFieldError(lastNameField, lastNameErrorLabel, message);
            } else {
                setFieldError(patientLastNameField, patientLastNameErrorLabel, message);
            }
            return;
        }

        FormValidator.setMessage(feedbackLabel, message, true);
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            var scene = AppNavigator.createScene(loader.load(), getClass());

            Stage stage = (Stage) emailField.getScene().getWindow();
            AppNavigator.applyStage(stage, scene, "PinkShield Login");
        } catch (IOException e) {
            showAlert("Error", "Failed to load login page.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
