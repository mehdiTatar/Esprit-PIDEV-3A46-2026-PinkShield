package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppointmentUserController {
    @FXML private TextField txtPatientName, txtPatientEmail, txtHeure;
    @FXML private DatePicker bookingDatePicker, calendarPicker;
    @FXML private ComboBox<String> specialtyComboBox, doctorComboBox;
    @FXML private TextArea notesArea;
    @FXML private VBox recommendationsVBox;
    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colDate, colDoctor, colStatus, colNotes;

    private ServiceAppointment service = new ServiceAppointment();
    private final ServiceParapharmacie parapharmacieService = new ServiceParapharmacie();
    private final ServiceWishlist wishlistService = new ServiceWishlist();
    private final AppointmentPdfService appointmentPdfService = new AppointmentPdfService();
    private ObservableList<Appointment> appointmentList;
    private final Map<LocalDate, List<String>> appointmentsByDate = new HashMap<>();
    private final Map<String, ObservableList<String>> specialtyDoctorsMap = new LinkedHashMap<>();
    private final Map<String, String> doctorToSpecialtyMap = new HashMap<>();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getAppointment_date() != null ? c.getValue().getAppointment_date().toString() : ""));
        colDoctor.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoctor_name()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colNotes.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNotes()));
        populateSpecialtyDoctors();
        specialtyComboBox.setItems(FXCollections.observableArrayList(specialtyDoctorsMap.keySet()));
        doctorComboBox.setDisable(true);
        doctorComboBox.setPromptText("Choose a specialty first");
        configureCalendar();
        setupDateSync();
        setupNotesRecommendations();
        displayRecommendations("");
        loadAppointments();
    }

    private void populateSpecialtyDoctors() {
        registerSpecialty("Dermatologist", "Dr. Amine Ben Jeddou", "Dr. Fatma Toumi", "Dr. Yosra Ben Salem");
        registerSpecialty("Cardiologist", "Dr. Walid Trabelsi", "Dr. Mouna Khelifi", "Dr. Hichem Ben Ammar");
        registerSpecialty("Ophthalmologist", "Dr. Ines Ghannouchi", "Dr. Nizar Toumi", "Dr. Marwa Ben Naceur");
        registerSpecialty("Pediatrician", "Dr. Rim Bouslama", "Dr. Mohamed Ali Charfi", "Dr. Asma Jaziri");
        registerSpecialty("Gynecologist", "Dr. Leila Ben Rhouma", "Dr. Samiya Gharbi", "Dr. Mehdi Zouari");
        registerSpecialty("Dentist", "Dr. Hela Krichen", "Dr. Oussama Ben Yahia", "Dr. Dorsaf Mzoughi");
        registerSpecialty("General Practitioner", "Dr. Firas Ben Hmida", "Dr. Amal Saidi", "Dr. Karim Chatti", "Dr. Nesrine Ayari");
        registerSpecialty("Psychiatrist", "Dr. Olfa Ben Othman", "Dr. Kais Hamza", "Dr. Souhir Belhaj");
        registerSpecialty("Orthopedist", "Dr. Anis Bouaziz", "Dr. Seif Eddine Karray", "Dr. Wafa Cherif");
        registerSpecialty("Endocrinologist", "Dr. Amina Kooli", "Dr. Lotfi Belhaj", "Dr. Emna Ayed");
    }

    private void registerSpecialty(String specialty, String... doctors) {
        ObservableList<String> doctorList = FXCollections.observableArrayList(doctors);
        specialtyDoctorsMap.put(specialty, doctorList);

        for (String doctor : doctors) {
            doctorToSpecialtyMap.put(doctor, specialty);
        }
    }

    private void loadAppointments() {
        try {
            appointmentList = FXCollections.observableArrayList(service.afficherAll());
            table.setItems(appointmentList);
            rebuildCalendarMarkers();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void configureCalendar() {
        if (calendarPicker == null) {
            return;
        }

        if (!calendarPicker.getStyleClass().contains("appointment-calendar")) {
            calendarPicker.getStyleClass().add("appointment-calendar");
        }
        calendarPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setTooltip(null);
                getStyleClass().remove("calendar-marked-day");

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                String dayNumber = String.valueOf(item.getDayOfMonth());
                setPrefSize(66, 48);
                setText(dayNumber);
                setDisable(false);

                if (item.isBefore(LocalDate.now())) {
                    setDisable(true);
                }

                List<String> namesOnDate = appointmentsByDate.get(item);
                if (namesOnDate != null && !namesOnDate.isEmpty()) {
                    String markerName = abbreviateName(namesOnDate.get(0));
                    setText(dayNumber + "\n" + markerName);
                    setTooltip(new Tooltip("Booked by: " + String.join(", ", namesOnDate)));
                    getStyleClass().add("calendar-marked-day");
                }
            }
        });
    }

    private void setupDateSync() {
        if (calendarPicker != null && bookingDatePicker != null) {
            calendarPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    bookingDatePicker.setValue(newDate);
                }
            });
        }
    }

    private void setupNotesRecommendations() {
        if (notesArea != null) {
            notesArea.textProperty().addListener((obs, oldText, newText) -> displayRecommendations(newText));
        }
    }

    @FXML
    public void onSpecialtySelected(ActionEvent event) {
        populateDoctorsForSelectedSpecialty(specialtyComboBox.getValue());
    }

    @FXML
    public void handleAiAssistant() {
        NavigationManager.getInstance().showAiSuggestions(notesArea != null ? notesArea.getText() : "");
    }

    @FXML
    public void handleOpenCalendar() {
        NavigationManager.getInstance().showAppointmentCalendar();
    }

    private void populateDoctorsForSelectedSpecialty(String selectedSpecialty) {
        doctorComboBox.getItems().clear();

        if (selectedSpecialty == null || selectedSpecialty.isBlank() || !specialtyDoctorsMap.containsKey(selectedSpecialty)) {
            doctorComboBox.setDisable(true);
            doctorComboBox.setPromptText("Choose a specialty first");
            doctorComboBox.setValue(null);
            return;
        }

        doctorComboBox.setItems(FXCollections.observableArrayList(specialtyDoctorsMap.get(selectedSpecialty)));
        doctorComboBox.setDisable(false);
        doctorComboBox.setPromptText("Select a doctor");
        doctorComboBox.setValue(null);
    }

    @FXML
    public void handleGenerateRecommendations() {
        displayRecommendations(notesArea != null ? notesArea.getText() : "");
    }

    private void displayRecommendations(String patientNotes) {
        if (recommendationsVBox == null) {
            return;
        }

        recommendationsVBox.getChildren().clear();

        String cleanedNotes = patientNotes == null ? "" : patientNotes.trim();
        if (cleanedNotes.isEmpty()) {
            recommendationsVBox.getChildren().add(createRecommendationMessage("Write your symptoms above to get product suggestions."));
            return;
        }

        List<String> recommendations = getAiRecommendations(cleanedNotes);
        if (recommendations.isEmpty()) {
            recommendationsVBox.getChildren().add(createRecommendationMessage("No matching products found in the parapharmacie inventory."));
            return;
        }

        recommendationsVBox.getChildren().add(createRecommendationMessage("Recommended products based on your notes:"));
        for (String productName : recommendations) {
            recommendationsVBox.getChildren().add(createRecommendationRow(productName));
        }
    }

    public List<String> getAiRecommendations(String patientNotes) {
        List<String> recommendations = new ArrayList<>();
        List<Parapharmacie> inventory = loadParapharmacieInventory();

        if (patientNotes == null || patientNotes.isBlank() || inventory.isEmpty()) {
            return recommendations;
        }

        String notes = patientNotes.toLowerCase();

        addKeywordBasedRecommendations(notes, inventory, recommendations,
                new String[]{"headache", "migraine", "pain", "fever"},
                new String[]{"doliprane", "paracetamol", "panadol", "fever", "pain"});

        addKeywordBasedRecommendations(notes, inventory, recommendations,
                new String[]{"skin", "rash", "acne", "eczema", "dry skin"},
                new String[]{"moisturizer", "cream", "dermo", "skin", "spf", "sun"});

        addKeywordBasedRecommendations(notes, inventory, recommendations,
                new String[]{"cough", "cold", "throat", "flu"},
                new String[]{"syrup", "cough", "throat", "vitamin c", "lozenge"});

        addKeywordBasedRecommendations(notes, inventory, recommendations,
                new String[]{"stomach", "nausea", "digestion", "digestive"},
                new String[]{"digest", "probiotic", "stomach", "gastr", "nausea"});

        addKeywordBasedRecommendations(notes, inventory, recommendations,
                new String[]{"allergy", "sneeze", "itch", "allergic"},
                new String[]{"allergy", "antihistamine", "nasal", "spray"});

        if (recommendations.isEmpty()) {
            for (int i = 0; i < Math.min(3, inventory.size()); i++) {
                addUniqueRecommendation(recommendations, inventory.get(i).getNom());
            }
        }

        return recommendations;
    }

    private List<Parapharmacie> loadParapharmacieInventory() {
        try {
            return parapharmacieService.afficherAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void addKeywordBasedRecommendations(String notes, List<Parapharmacie> inventory, List<String> recommendations,
                                                String[] noteKeywords, String[] productHints) {
        if (!containsAny(notes, noteKeywords)) {
            return;
        }

        for (Parapharmacie product : inventory) {
            String haystack = ((product.getNom() == null ? "" : product.getNom()) + " " +
                    (product.getDescription() == null ? "" : product.getDescription())).toLowerCase();
            if (containsAny(haystack, productHints)) {
                addUniqueRecommendation(recommendations, product.getNom());
            }
        }
    }

    private boolean containsAny(String text, String[] values) {
        for (String value : values) {
            if (text.contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void addUniqueRecommendation(List<String> recommendations, String productName) {
        if (productName != null && !productName.isBlank() && !recommendations.contains(productName)) {
            recommendations.add(productName);
        }
    }

    private Label createRecommendationMessage(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add("label-subtitle");
        return label;
    }

    private HBox createRecommendationRow(String productName) {
        Label productLabel = new Label(productName);
        productLabel.getStyleClass().add("ai-recommendation-title");

        Button addToWishlistButton = new Button("Add to Wishlist");
        addToWishlistButton.getStyleClass().add("btn-primary");
        addToWishlistButton.setOnAction(event -> {
            try {
                Parapharmacie product = parapharmacieService.findByName(productName);
                if (product == null) {
                    showWarningAlert("Wishlist", "Product not found in parapharmacie inventory.");
                    return;
                }

                wishlistService.add(new Product(product.getNom(), product.getPrix(), product.getDescription()));
                NavigationManager.getInstance().showWishlist();
            } catch (Exception ex) {
                showErrorAlert("Wishlist Error", ex.getMessage());
            }
        });

        Button goToParapharmacieButton = new Button("Go to Parapharmacie");
        goToParapharmacieButton.getStyleClass().add("btn-secondary");
        goToParapharmacieButton.setOnAction(event -> NavigationManager.getInstance().showParapharmacie());

        HBox row = new HBox(10, productLabel, addToWishlistButton, goToParapharmacieButton);
        row.setStyle("-fx-padding: 10; -fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e8e9f3; -fx-border-radius: 10;");
        return row;
    }

    private void rebuildCalendarMarkers() {
        appointmentsByDate.clear();
        for (Appointment appointment : appointmentList) {
            if (appointment.getAppointment_date() == null) {
                continue;
            }

            LocalDate date = appointment.getAppointment_date().toLocalDateTime().toLocalDate();
            appointmentsByDate.putIfAbsent(date, new ArrayList<>());
            List<String> names = appointmentsByDate.get(date);
            String patientName = appointment.getPatient_name() != null ? appointment.getPatient_name().trim() : "Patient";

            LinkedHashSet<String> uniqueNames = new LinkedHashSet<>(names);
            uniqueNames.add(patientName);
            names.clear();
            names.addAll(uniqueNames);
        }

        // Refresh the popup cell rendering with the latest markers.
        configureCalendar();
    }

    private String abbreviateName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Name";
        }

        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0];
        if (first.length() <= 6) {
            return first;
        }
        return first.substring(0, 6);
    }

    @FXML
    public void handleAjouter() {
        if (!validateInput()) return;

        try {
            // Fusion de la date et de l'heure
            LocalDateTime ldt = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.parse(txtHeure.getText()));
            Timestamp ts = Timestamp.valueOf(ldt);

            String doctorName = doctorComboBox.getValue();
            Appointment a = new Appointment(
                    txtPatientEmail.getText(),
                    txtPatientName.getText(),
                    buildDoctorEmail(doctorName),
                    doctorName,
                    ts,
                    "pending",
                    notesArea.getText()
            );

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
        if (txtPatientName.getText().isEmpty() || txtPatientEmail.getText().isEmpty() || txtHeure.getText().isEmpty() || bookingDatePicker.getValue() == null) {
            showWarningAlert("Champs vides", "Tous les champs sont obligatoires.");
            return false;
        }
        if (specialtyComboBox.getValue() == null || doctorComboBox.getValue() == null) {
            showWarningAlert("Doctor Selection", "Please choose a specialty and a doctor.");
            return false;
        }
        if (!txtPatientEmail.getText().contains("@")) {
            showWarningAlert("Email", "L'email doit être valide.");
            return false;
        }
        if (bookingDatePicker.getValue().isBefore(LocalDate.now())) {
            showWarningAlert("Date", "Impossible de réserver dans le passé.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        txtPatientName.clear();
        txtPatientEmail.clear();
        txtHeure.clear();
        notesArea.clear();
        bookingDatePicker.setValue(null);
        if (calendarPicker != null) {
            calendarPicker.setValue(null);
        }
        specialtyComboBox.setValue(null);
        doctorComboBox.getItems().clear();
        doctorComboBox.setValue(null);
        doctorComboBox.setDisable(true);
        doctorComboBox.setPromptText("Choose a specialty first");
    }

    @FXML
    public void handleModifier() {
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null && validateInput()) {
            try {
                LocalDateTime ldt = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.parse(txtHeure.getText()));
                s.setPatient_name(txtPatientName.getText());
                s.setNotes(notesArea.getText());
                s.setDoctor_email(buildDoctorEmail(doctorComboBox.getValue()));
                s.setDoctor_name(doctorComboBox.getValue());
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
    public void handleDownloadPdf() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Select an appointment from the table to download its proof PDF.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Appointment Proof");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("appointment-proof-" + selected.getId() + ".pdf");

        Stage stage = (Stage) table.getScene().getWindow();
        File targetFile = fileChooser.showSaveDialog(stage);
        if (targetFile == null) {
            return;
        }

        try {
            appointmentPdfService.exportAppointmentProof(selected, targetFile);
            showInfoAlert("PDF Created", "Appointment proof saved:\n" + targetFile.getAbsolutePath());
        } catch (Exception e) {
            showErrorAlert("PDF Error", "Could not generate PDF proof: " + e.getMessage());
        }
    }

    @FXML
    public void handleDownloadPDF() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select an appointment from the table first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Appointment Invoice");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("INV-" + selected.getId() + ".pdf");

        Stage stage = (Stage) table.getScene().getWindow();
        File targetFile = fileChooser.showSaveDialog(stage);
        if (targetFile == null) {
            return;
        }

        try {
            appointmentPdfService.exportAppointmentInvoice(selected, targetFile);
            showInfoAlert("PDF Created", "Invoice saved successfully:\n" + targetFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            showErrorAlert("PDF Error", "Could not create invoice PDF: " + String.valueOf(e));
        } catch (Exception e) {
            showErrorAlert("PDF Error", "Unexpected error while creating invoice PDF: " + String.valueOf(e));
        }
    }


    @FXML
    public void handleRowSelect() {
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null) {
            txtPatientName.setText(s.getPatient_name());
            txtPatientEmail.setText(s.getPatient_email());
            notesArea.setText(s.getNotes());
            LocalDate selectedDate = s.getAppointment_date().toLocalDateTime().toLocalDate();
            bookingDatePicker.setValue(selectedDate);
            if (calendarPicker != null) {
                calendarPicker.setValue(selectedDate);
            }
            txtHeure.setText(s.getAppointment_date().toLocalDateTime().toLocalTime().toString());

            String specialty = doctorToSpecialtyMap.get(s.getDoctor_name());
            if (specialty != null) {
                specialtyComboBox.setValue(specialty);
                populateDoctorsForSelectedSpecialty(specialty);
                doctorComboBox.setValue(s.getDoctor_name());
            } else {
                specialtyComboBox.setValue(null);
                doctorComboBox.getItems().clear();
                doctorComboBox.setValue(null);
                doctorComboBox.setDisable(true);
                doctorComboBox.setPromptText("Choose a specialty first");
            }
        }
    }

    private String buildDoctorEmail(String doctorName) {
        if (doctorName == null || doctorName.isBlank()) {
            return "doctor@pinkshield.tn";
        }

        String normalized = doctorName.toLowerCase()
                .replace("dr.", "")
                .replaceAll("[^a-z0-9]+", ".")
                .replaceAll("^\\.+|\\.+$", "");

        return normalized.isBlank() ? "doctor@pinkshield.tn" : normalized + "@pinkshield.tn";
    }

    private void showInfoAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showWarningAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
}

