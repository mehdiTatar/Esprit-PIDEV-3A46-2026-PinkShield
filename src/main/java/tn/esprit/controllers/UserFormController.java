package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.utils.FormValidator;

public class UserFormController {
    @FXML private Label formTitleLabel;
    @FXML private Label feedbackLabel;
    @FXML private ComboBox<String> roleCombo;
    @FXML private VBox staffFieldsBox;
    @FXML private VBox patientFieldsBox;
    @FXML private VBox specialityBox;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField specialityField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private Button saveButton;

    private final UserService userService = new UserService();
    private boolean editMode;
    private boolean saved;
    private User currentUser;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(
                UserService.ROLE_ADMIN,
                UserService.ROLE_DOCTOR,
                UserService.ROLE_USER
        ));
        roleCombo.valueProperty().addListener((obs, oldValue, newValue) -> updateRoleFields());

        FormValidator.attachClearOnInput(feedbackLabel,
                firstNameField, lastNameField, fullNameField, emailField,
                passwordField, confirmPasswordField, specialityField, phoneField, addressField);
    }

    public void configureForCreate() {
        editMode = false;
        currentUser = null;
        saved = false;
        formTitleLabel.setText("Create a new account");
        saveButton.setText("Create account");
        clearFields();
        roleCombo.setDisable(false);
        roleCombo.setValue(UserService.ROLE_USER);
        updateRoleFields();
    }

    public void configureForEdit(User user) {
        editMode = true;
        currentUser = user;
        saved = false;
        formTitleLabel.setText("Edit account");
        saveButton.setText("Save changes");

        roleCombo.setValue(user.getRole());
        roleCombo.setDisable(true);
        firstNameField.setText(value(user.getFirstName()));
        lastNameField.setText(value(user.getLastName()));
        fullNameField.setText(value(user.getFullName()));
        emailField.setText(value(user.getEmail()));
        passwordField.setText(value(user.getPassword()));
        confirmPasswordField.setText(value(user.getPassword()));
        specialityField.setText(value(user.getSpeciality()));
        phoneField.setText(value(user.getPhone()));
        addressField.setText(value(user.getAddress()));
        updateRoleFields();
    }

    @FXML
    public void handleSave() {
        FormValidator.clearStates(
                roleCombo, firstNameField, lastNameField, fullNameField, emailField,
                passwordField, confirmPasswordField, specialityField, phoneField, addressField
        );

        String role = roleCombo.getValue();
        if (role == null || role.isBlank()) {
            FormValidator.markInvalid(roleCombo);
            FormValidator.setMessage(feedbackLabel, "Please select a role.", true);
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!FormValidator.isValidEmail(email)) {
            FormValidator.markInvalid(emailField);
            FormValidator.setMessage(feedbackLabel, "Enter a valid email address.", true);
            return;
        }

        if (password.length() < 8) {
            FormValidator.markInvalid(passwordField);
            FormValidator.setMessage(feedbackLabel, "Password must contain at least 8 characters.", true);
            return;
        }

        if (!password.equals(confirmPassword)) {
            FormValidator.markInvalid(confirmPasswordField);
            FormValidator.setMessage(feedbackLabel, "Password confirmation does not match.", true);
            return;
        }

        User userToSave = editMode ? copyUser(currentUser) : new User();
        userToSave.setRole(role);
        userToSave.setEmail(email);
        userToSave.setPassword(password);

        if (UserService.ROLE_USER.equals(role)) {
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (fullName.length() < 3) {
                FormValidator.markInvalid(fullNameField);
                FormValidator.setMessage(feedbackLabel, "Full name must contain at least 3 characters.", true);
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

            userToSave.setFullName(fullName);
            userToSave.setPhone(phone);
            userToSave.setAddress(address);
            userToSave.setFirstName(null);
            userToSave.setLastName(null);
            userToSave.setSpeciality(null);
        } else {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();

            if (firstName.isEmpty()) {
                FormValidator.markInvalid(firstNameField);
                FormValidator.setMessage(feedbackLabel, "First name is required.", true);
                return;
            }
            if (lastName.isEmpty()) {
                FormValidator.markInvalid(lastNameField);
                FormValidator.setMessage(feedbackLabel, "Last name is required.", true);
                return;
            }

            userToSave.setFirstName(firstName);
            userToSave.setLastName(lastName);
            userToSave.setFullName((firstName + " " + lastName).trim());
            userToSave.setPhone(phoneField.getText().trim());
            userToSave.setAddress(addressField.getText().trim());

            if (UserService.ROLE_DOCTOR.equals(role)) {
                String speciality = specialityField.getText().trim();
                if (speciality.isEmpty()) {
                    FormValidator.markInvalid(specialityField);
                    FormValidator.setMessage(feedbackLabel, "Doctor speciality is required.", true);
                    return;
                }
                userToSave.setSpeciality(speciality);
            } else {
                userToSave.setSpeciality(null);
            }
        }

        String validationMessage = userService.validateUser(userToSave, confirmPassword, editMode);
        if (validationMessage != null) {
            markFieldForMessage(validationMessage);
            FormValidator.setMessage(feedbackLabel, validationMessage, true);
            return;
        }

        boolean success = editMode ? userService.updateUser(userToSave) : userService.createUser(userToSave);
        if (!success) {
            FormValidator.setMessage(feedbackLabel, "The operation failed. Verify the data and database state.", true);
            return;
        }

        saved = true;
        closeWindow();
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    public boolean isSaved() {
        return saved;
    }

    private void updateRoleFields() {
        String role = roleCombo.getValue();
        boolean patient = UserService.ROLE_USER.equals(role);
        boolean doctor = UserService.ROLE_DOCTOR.equals(role);

        staffFieldsBox.setManaged(!patient);
        staffFieldsBox.setVisible(!patient);
        patientFieldsBox.setManaged(patient);
        patientFieldsBox.setVisible(patient);
        specialityBox.setManaged(doctor);
        specialityBox.setVisible(doctor);
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        specialityField.clear();
        phoneField.clear();
        addressField.clear();
        FormValidator.setMessage(feedbackLabel, "", true);
    }

    private void markFieldForMessage(String message) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("email")) {
            FormValidator.markInvalid(emailField);
        } else if (lowerMessage.contains("password confirmation")) {
            FormValidator.markInvalid(confirmPasswordField);
        } else if (lowerMessage.contains("password")) {
            FormValidator.markInvalid(passwordField);
        } else if (lowerMessage.contains("first name")) {
            FormValidator.markInvalid(firstNameField);
        } else if (lowerMessage.contains("last name")) {
            FormValidator.markInvalid(lastNameField);
        } else if (lowerMessage.contains("full name")) {
            FormValidator.markInvalid(fullNameField);
        } else if (lowerMessage.contains("phone")) {
            FormValidator.markInvalid(phoneField);
        } else if (lowerMessage.contains("address")) {
            FormValidator.markInvalid(addressField);
        } else if (lowerMessage.contains("speciality")) {
            FormValidator.markInvalid(specialityField);
        }
    }

    private User copyUser(User source) {
        User copy = new User();
        copy.setId(source.getId());
        copy.setRole(source.getRole());
        copy.setFirstName(source.getFirstName());
        copy.setLastName(source.getLastName());
        copy.setFullName(source.getFullName());
        copy.setEmail(source.getEmail());
        copy.setPassword(source.getPassword());
        copy.setPhone(source.getPhone());
        copy.setAddress(source.getAddress());
        copy.setSpeciality(source.getSpeciality());
        return copy;
    }

    private String value(String input) {
        return input == null ? "" : input;
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
