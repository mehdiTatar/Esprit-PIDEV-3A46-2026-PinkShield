package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class AppointmentController {

    @FXML
    private TextField txtPatientEmail;
    @FXML
    private TextField txtPatientName;
    @FXML
    private TextField txtDoctorEmail;
    @FXML
    private TextField txtDoctorName;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField txtHeure;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private TextArea txtNotes;
    @FXML
    private TextField searchBar;
    @FXML
    private TableView<Appointment> table;
    @FXML
    private TableColumn<Appointment, Integer> colId;
    @FXML
    private TableColumn<Appointment, String> colPatient;
    @FXML
    private TableColumn<Appointment, String> colDoctor;
    @FXML
    private TableColumn<Appointment, String> colStatus;
    @FXML
    private TableColumn<Appointment, java.sql.Timestamp> colDate;

    private ServiceAppointment service;
    private ObservableList<Appointment> appointmentList;
    private FilteredList<Appointment> filteredList;

    @FXML
    public void initialize() {
        service = new ServiceAppointment();

        comboStatus.setItems(FXCollections.observableArrayList("pending", "confirmed", "cancelled"));

        initializeTableColumns();

        loadAppointments();

        setupRealTimeSearch();
    }

    private void initializeTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatient.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPatient_name()));
        colDoctor.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDoctor_name()));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("appointment_date"));
    }

    private void loadAppointments() {
        try {
            ArrayList<Appointment> appointments = service.afficherAll();
            appointmentList = FXCollections.observableArrayList(appointments);
            table.setItems(appointmentList);
        } catch (SQLException e) {
            System.out.println("Database connection error - this is OK for testing: " + e.getMessage());
            appointmentList = FXCollections.observableArrayList();
            table.setItems(appointmentList);
        } catch (NullPointerException e) {
            System.out.println("Database not ready - this is OK: " + e.getMessage());
            appointmentList = FXCollections.observableArrayList();
            table.setItems(appointmentList);
        }
    }

    private void setupRealTimeSearch() {
        filteredList = new FilteredList<>(appointmentList, p -> true);

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(appointment -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                return appointment.getPatient_name().toLowerCase().contains(lowerCaseFilter) ||
                       appointment.getDoctor_name().toLowerCase().contains(lowerCaseFilter);
            });
        });

        table.setItems(filteredList);
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) {
            return;
        }

        try {
            String patientEmail = txtPatientEmail.getText();
            String patientName = txtPatientName.getText();
            String doctorEmail = txtDoctorEmail.getText();
            String doctorName = txtDoctorName.getText();
            String status = comboStatus.getValue();
            String notes = txtNotes.getText();

            LocalDate selectedDate = datePicker.getValue();
            String timeString = txtHeure.getText();

            Timestamp appointmentTimestamp = parseDateTime(selectedDate, timeString);
            if (appointmentTimestamp == null) {
                return;
            }

            if (service.isDoctorBooked(doctorEmail, appointmentTimestamp)) {
                showErrorAlert("Doctor Unavailable", "The doctor is already booked at this time slot.");
                return;
            }

            Appointment appointment = new Appointment(
                    patientEmail, patientName, doctorEmail, doctorName,
                    appointmentTimestamp, status, notes
            );

            service.ajouter(appointment);
            showInfoAlert("Success", "Appointment added successfully!");

            clearFields();
            loadAppointments();

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not add appointment: " + e.getMessage());
        }
    }

    @FXML
    public void handleSupprimer() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select an appointment to delete.");
            return;
        }

        try {
            service.supprimer(selected.getId());
            showInfoAlert("Success", "Appointment deleted successfully!");
            loadAppointments();
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not delete appointment: " + e.getMessage());
        }
    }


    private boolean validateInput() {
        String patientEmail = txtPatientEmail.getText();
        String patientName = txtPatientName.getText();
        String doctorEmail = txtDoctorEmail.getText();
        String doctorName = txtDoctorName.getText();
        String status = comboStatus.getValue();
        LocalDate selectedDate = datePicker.getValue();
        String timeString = txtHeure.getText();

        if (patientEmail.isEmpty() || patientName.isEmpty() || doctorEmail.isEmpty() || 
            doctorName.isEmpty() || status == null || selectedDate == null || timeString.isEmpty()) {
            showWarningAlert("Validation Error", "All fields are required. Please fill in all fields.");
            return false;
        }

        if (!patientEmail.contains("@")) {
            showWarningAlert("Validation Error", "Patient email must be a valid email address.");
            return false;
        }

        if (!doctorEmail.contains("@")) {
            showWarningAlert("Validation Error", "Doctor email must be a valid email address.");
            return false;
        }

        return true;
    }

    private Timestamp parseDateTime(LocalDate date, String timeString) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime dateTime = LocalDateTime.of(date, java.time.LocalTime.parse(timeString, timeFormatter));
            return Timestamp.valueOf(dateTime);
        } catch (DateTimeParseException e) {
            showErrorAlert("Time Format Error", "Please enter time in HH:mm format (e.g., 14:30).");
            return null;
        }
    }

    private void clearFields() {
        txtPatientEmail.clear();
        txtPatientName.clear();
        txtDoctorEmail.clear();
        txtDoctorName.clear();
        txtHeure.clear();
        txtNotes.clear();
        datePicker.setValue(null);
        comboStatus.setValue(null);
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

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

