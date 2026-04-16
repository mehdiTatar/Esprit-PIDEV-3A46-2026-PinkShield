package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import tn.esprit.entities.Appointment;
import tn.esprit.entities.User;
import tn.esprit.services.AppointmentService;
import tn.esprit.services.UserService;
import tn.esprit.utils.FormValidator;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentListController {
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> doctorPatientColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, String> notesColumn;
    @FXML private TableColumn<Appointment, Void> actionsColumn;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button bookAppointmentButton;

    @FXML private Label formTitle;
    @FXML private Label feedbackLabel;
    @FXML private ComboBox<User> doctorCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextArea notesArea;

    private final AppointmentService appointmentService = new AppointmentService();
    private final UserService userService = new UserService();
    private final ObservableList<Appointment> allAppointments = FXCollections.observableArrayList();
    private User currentUser;

    @FXML
    public void initialize() {
        if (appointmentsTable != null) {
            setupTable();
            setupFilter();
        }

        if (doctorCombo != null) {
            loadDoctors();
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setDisable(empty || item.isBefore(LocalDate.now()));
                }
            });
            FormValidator.attachClearOnInput(feedbackLabel, timeField);
            FormValidator.attachClearOnInput(feedbackLabel, doctorCombo);
            notesArea.textProperty().addListener((obs, oldValue, newValue) -> FormValidator.setMessage(feedbackLabel, "", true));
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (bookAppointmentButton != null) {
            boolean canBook = user != null && "user".equals(user.getRole());
            bookAppointmentButton.setManaged(canBook);
            bookAppointmentButton.setVisible(canBook);
        }
        if (appointmentsTable != null) {
            loadAppointments();
        }
    }

    private void setupTable() {
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getAppointmentDate().toLocalDateTime().format(TABLE_DATE_FORMAT)
        ));

        doctorPatientColumn.setCellValueFactory(cellData -> {
            Appointment appointment = cellData.getValue();
            String label = currentUser != null && "doctor".equals(currentUser.getRole())
                    ? appointment.getPatientName()
                    : appointment.getDoctorName();
            return new javafx.beans.property.SimpleStringProperty(label);
        });

        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("role-admin", "role-doctor", "role-user");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.toUpperCase());
                if ("confirmed".equalsIgnoreCase(item)) {
                    getStyleClass().add("role-doctor");
                } else if ("cancelled".equalsIgnoreCase(item)) {
                    getStyleClass().add("feedback-error");
                } else {
                    getStyleClass().add("role-user");
                }
            }
        });

        notesColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNotes()));
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Cancel");
            {
                button.getStyleClass().addAll("button", "danger-button");
                button.setStyle("-fx-font-size: 10; -fx-padding: 6 12;");
                button.setOnAction(event -> handleCancelAppointment(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Appointment appointment = getTableView().getItems().get(getIndex());
                button.setDisable("cancelled".equalsIgnoreCase(appointment.getStatus()));
                setGraphic(button);
            }
        });
    }

    private void loadAppointments() {
        if (currentUser == null || appointmentsTable == null) {
            return;
        }

        List<Appointment> appointments;
        if ("admin".equals(currentUser.getRole())) {
            appointments = appointmentService.getAllAppointments();
        } else if ("doctor".equals(currentUser.getRole())) {
            appointments = appointmentService.getAppointmentsByDoctor(currentUser.getId());
        } else {
            appointments = appointmentService.getAppointmentsByPatient(currentUser.getId());
        }

        allAppointments.setAll(appointments);
        appointmentsTable.setItems(allAppointments);
    }

    private void setupFilter() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "pending", "confirmed", "completed", "cancelled"));
        statusFilter.setValue("All");
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || "All".equalsIgnoreCase(newValue)) {
                appointmentsTable.setItems(allAppointments);
                return;
            }

            appointmentsTable.setItems(allAppointments.filtered(
                    appointment -> appointment.getStatus().equalsIgnoreCase(newValue)
            ));
        });
    }

    private void loadDoctors() {
        List<User> doctors = userService.getAllUsers().stream()
                .filter(user -> "doctor".equals(user.getRole()))
                .collect(Collectors.toList());

        doctorCombo.setItems(FXCollections.observableArrayList(doctors));
        doctorCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : "Dr. " + user.getLastName() + " - " + user.getSpeciality();
            }

            @Override
            public User fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void handleNewAppointment() {
        loadSubView("/fxml/appointment_form.fxml");
    }

    @FXML
    public void handleConfirmBooking() {
        if (currentUser == null) {
            showAlert("Error", "You must be logged in to book an appointment.", Alert.AlertType.ERROR);
            return;
        }

        User doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String timeText = timeField.getText().trim();
        String notes = notesArea.getText().trim();

        if (doctor == null) {
            FormValidator.setMessage(feedbackLabel, "Doctor selection is required.", true);
            return;
        }
        if (date == null) {
            FormValidator.setMessage(feedbackLabel, "Appointment date is required.", true);
            return;
        }
        if (date.isBefore(LocalDate.now())) {
            FormValidator.setMessage(feedbackLabel, "Appointment date cannot be in the past.", true);
            return;
        }
        if (timeText.isEmpty()) {
            FormValidator.markInvalid(timeField);
            FormValidator.setMessage(feedbackLabel, "Appointment time is required.", true);
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeText, TIME_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            FormValidator.markInvalid(timeField);
            FormValidator.setMessage(feedbackLabel, "Time must use the HH:mm format.", true);
            return;
        }

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        if (dateTime.isBefore(LocalDateTime.now())) {
            FormValidator.setMessage(feedbackLabel, "Appointment date and time must be in the future.", true);
            return;
        }

        Timestamp appointmentTimestamp = Timestamp.valueOf(dateTime);
        if (appointmentService.hasAppointmentConflict(doctor.getId(), appointmentTimestamp, null)) {
            FormValidator.setMessage(feedbackLabel, "This doctor already has an appointment at the selected date and time.", true);
            return;
        }
        if (appointmentService.hasPatientDuplicate(currentUser.getId(), doctor.getId(), appointmentTimestamp, null)) {
            FormValidator.setMessage(feedbackLabel, "This appointment already exists for the same doctor, patient, and time.", true);
            return;
        }

        Appointment appointment = new Appointment(
                currentUser.getId(),
                doctor.getId(),
                currentUser.getFullName(),
                "Dr. " + doctor.getLastName(),
                appointmentTimestamp,
                "pending",
                notes
        );

        if (appointmentService.addAppointment(appointment)) {
            showAlert("Success", "Appointment booked successfully.", Alert.AlertType.INFORMATION);
            handleBackToList();
        } else {
            showAlert("Error", "Failed to book appointment.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleCancelForm() {
        handleBackToList();
    }

    private void handleCancelAppointment(Appointment appointment) {
        if (appointmentService.updateStatus(appointment.getId(), "cancelled")) {
            loadAppointments();
        }
    }

    private void handleBackToList() {
        loadSubView("/fxml/appointment_list.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AppointmentListController) {
                ((AppointmentListController) controller).setCurrentUser(currentUser);
            }

            StackPane mainContent = null;
            if (appointmentsTable != null && appointmentsTable.getScene() != null) {
                mainContent = (StackPane) appointmentsTable.getScene().lookup("#mainContent");
            } else if (formTitle != null && formTitle.getScene() != null) {
                mainContent = (StackPane) formTitle.getScene().lookup("#mainContent");
            }

            if (mainContent != null) {
                mainContent.getChildren().setAll(view);
            }
        } catch (IOException e) {
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
