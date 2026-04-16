package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

public class AppointmentUserController {
    @FXML private TextField txtPatientName, txtPatientEmail, txtDoctorName, txtHeure;
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
        if (!validateInput()) return;

        try {
            // Fusion de la date et de l'heure
            LocalDateTime ldt = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(txtHeure.getText()));
            Timestamp ts = Timestamp.valueOf(ldt);

            Appointment a = new Appointment(txtPatientEmail.getText(), txtPatientName.getText(), "doc@test.com", txtDoctorName.getText(), ts, "pending", txtNotes.getText());

            service.ajouter(a);

            // EVENT SUCCESS
            showInfoAlert("Appointment Booked", "Succès ! Rendez-vous enregistré pour " + txtPatientName.getText());

            clearFields();
            loadAppointments();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Format heure invalide (HH:mm) ou erreur DB.");
        }
    }

    private boolean validateInput() {
        if (txtPatientName.getText().isEmpty() || txtPatientEmail.getText().isEmpty() || txtHeure.getText().isEmpty() || datePicker.getValue() == null) {
            showWarningAlert("Champs vides", "Tous les champs sont obligatoires.");
            return false;
        }
        if (!txtPatientEmail.getText().contains("@")) {
            showWarningAlert("Email", "L'email doit être valide.");
            return false;
        }
        if (datePicker.getValue().isBefore(LocalDate.now())) {
            showWarningAlert("Date", "Impossible de réserver dans le passé.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtPatientName.clear(); txtPatientEmail.clear(); txtDoctorName.clear(); txtHeure.clear(); txtNotes.clear(); datePicker.setValue(null);
    }

    @FXML
    public void handleModifier() {
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null && validateInput()) {
            try {
                LocalDateTime ldt = LocalDateTime.of(datePicker.getValue(), LocalTime.parse(txtHeure.getText()));
                s.setPatient_name(txtPatientName.getText());
                s.setAppointment_date(Timestamp.valueOf(ldt));
                service.modifier(s);
                loadAppointments();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    public void handleSupprimer() {
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null) {
            try {
                service.supprimer(s.getId());
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
            txtHeure.setText(s.getAppointment_date().toLocalDateTime().toLocalTime().toString());
        }
    }

    private void showInfoAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showWarningAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
}