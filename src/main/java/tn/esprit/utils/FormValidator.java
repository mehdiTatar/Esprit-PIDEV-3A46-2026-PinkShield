package tn.esprit.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

import java.util.regex.Pattern;

public final class FormValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9 ]{8,20}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z\\s'.-]{1,49}$");

    private FormValidator() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidName(String value) {
        return value != null && NAME_PATTERN.matcher(value.trim()).matches();
    }

    public static void clearFieldState(Control control) {
        if (control == null) {
            return;
        }
        control.getStyleClass().removeAll("field-invalid", "field-valid");
    }

    public static void markInvalid(Control control) {
        clearFieldState(control);
        control.getStyleClass().add("field-invalid");
    }

    public static void markValid(Control control) {
        clearFieldState(control);
        control.getStyleClass().add("field-valid");
    }

    public static void clearStates(Control... controls) {
        for (Control control : controls) {
            clearFieldState(control);
        }
    }

    public static void setMessage(Label label, String message, boolean error) {
        if (label == null) {
            return;
        }
        label.setVisible(message != null && !message.isBlank());
        label.setManaged(message != null && !message.isBlank());
        label.setText(message == null ? "" : message);
        label.getStyleClass().removeAll("feedback-error", "feedback-success");
        if (message != null && !message.isBlank()) {
            label.getStyleClass().add(error ? "feedback-error" : "feedback-success");
        }
    }

    public static void attachClearOnInput(Label label, TextInputControl... controls) {
        for (TextInputControl control : controls) {
            if (control == null) {
                continue;
            }
            control.textProperty().addListener((obs, oldValue, newValue) -> {
                clearFieldState(control);
                setMessage(label, "", true);
            });
        }
    }

    public static void attachClearOnInput(Label label, ComboBox<?>... controls) {
        for (ComboBox<?> control : controls) {
            if (control == null) {
                continue;
            }
            control.valueProperty().addListener((obs, oldValue, newValue) -> {
                clearFieldState(control);
                setMessage(label, "", true);
            });
            control.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
                clearFieldState(control);
                setMessage(label, "", true);
            });
        }
    }
}
