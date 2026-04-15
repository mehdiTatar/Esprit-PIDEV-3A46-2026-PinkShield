package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AppointmentUserController {
    @FXML private TextField txtPatientName, txtPatientEmail, txtDoctorName;
    @FXML private DatePicker datePicker;
    @FXML private TextArea txtNotes;
    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colDate, colDoctor, colStatus, colNotes;

    private ServiceAppointment service = new ServiceAppointment();
    private ObservableList<Appointment> appointmentList;

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAppointment_date() != null ? c.getValue().getAppointment_date().toString() : ""));
        colDoctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctor_name()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colNotes.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNotes()));
        loadAppointments();
    }

    private void loadAppointments() {
        try {
            appointmentList = FXCollections.observableArrayList(service.afficherAll());
            table.setItems(appointmentList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void handleAjouter() {
        // 1. On appelle la validation avant de faire quoi que ce soit
        if (!validateInput()) {
            return;
        }

        try {
            Appointment a = new Appointment(
                    txtPatientEmail.getText(),
                    txtPatientName.getText(),
                    "doc@test.com",
                    txtDoctorName.getText(),
                    Timestamp.valueOf(datePicker.getValue().atStartOfDay()),
                    "pending",
                    txtNotes.getText()
            );

            service.ajouter(a);

            // 2. ÉVÉNEMENT : SUCCESS ALERT (L'événement "Booked")
            showInfoAlert("Appointment Booked", "Félicitations ! Votre rendez-vous avec " + txtDoctorName.getText() + " a été enregistré avec succès.");

            clearFields();
            loadAppointments();
        } catch (Exception e) {
            showErrorAlert("Erreur de réservation", "Impossible de réserver : " + e.getMessage());
        }
    }

    // --- SYSTÈME DE VALIDATION (Pour les 2 points du prof) ---
    private boolean validateInput() {
        if (txtPatientName.getText().trim().isEmpty() ||
                txtPatientEmail.getText().trim().isEmpty() ||
                txtDoctorName.getText().trim().isEmpty() ||
                datePicker.getValue() == null) {
            showWarningAlert("Champs manquants", "Tous les champs (Nom, Email, Docteur, Date) sont obligatoires.");
            return false;
        }

        if (!txtPatientEmail.getText().contains("@")) {
            showWarningAlert("Email invalide", "Veuillez saisir une adresse email valide.");
            return false;
        }

        // Bloquer les dates dans le passé
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showWarningAlert("Date invalide", "Vous ne pouvez pas prendre de rendez-vous pour une date déjà passée.");
            return false;
        }

        return true;
    }

    private void clearFields() {
        txtPatientName.clear();
        txtPatientEmail.clear();
        txtDoctorName.clear();
        txtNotes.clear();
        datePicker.setValue(null);
    }

    @FXML
    public void handleModifier() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && validateInput()) {
            try {
                selected.setPatient_name(txtPatientName.getText());
                selected.setPatient_email(txtPatientEmail.getText());
                selected.setDoctor_name(txtDoctorName.getText());
                selected.setNotes(txtNotes.getText());
                selected.setAppointment_date(Timestamp.valueOf(datePicker.getValue().atStartOfDay()));

                service.modifier(selected);
                showInfoAlert("Mise à jour", "Le rendez-vous a été modifié.");
                loadAppointments();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void handleSupprimer() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                service.supprimer(selected.getId());
                showInfoAlert("Suppression", "Le rendez-vous a été annulé.");
                loadAppointments();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void handleRowSelect() {
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtPatientName.setText(s.getPatient_name());
            txtPatientEmail.setText(s.getPatient_email());
            txtDoctorName.setText(s.getDoctor_name());
            txtNotes.setText(s.getNotes());
            datePicker.setValue(s.getAppointment_date().toLocalDateTime().toLocalDate());
        }
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}