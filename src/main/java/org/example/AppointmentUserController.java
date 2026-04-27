package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import java.util.concurrent.CompletableFuture;

public class AppointmentUserController {
    private static final int MAX_RECOMMENDATIONS = 3;
    @FXML private TextField txtPatientName, txtPatientEmail, txtHeure;
    @FXML private DatePicker bookingDatePicker, calendarPicker;
    @FXML private ComboBox<String> specialtyComboBox, doctorComboBox;
    @FXML private TextArea notesArea;
    @FXML private VBox recommendationsVBox;
    @FXML private TableView<Appointment> table;
    @FXML private TableColumn<Appointment, String> colDate, colDoctor, colStatus, colNotes;
    @FXML private ImageView qrCodeImageView;
    @FXML private ImageView clinicMapView;

    private ServiceAppointment service = new ServiceAppointment();
    private final ServiceParapharmacie parapharmacieService = new ServiceParapharmacie();
    private final ServiceWishlist wishlistService = new ServiceWishlist();
    private final AppointmentPdfService appointmentPdfService = new AppointmentPdfService();
    private ObservableList<Appointment> appointmentList;
    private final Map<LocalDate, List<String>> appointmentsByDate = new HashMap<>();
    private final Map<String, ObservableList<String>> specialtyDoctorsMap = new LinkedHashMap<>();
    private final Map<String, String> doctorToSpecialtyMap = new HashMap<>();
    private final Map<Integer, String> appointmentInvoiceUrls = new HashMap<>();

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
        bindSessionIdentity();
        loadAppointments();
        
        // Load clinic map
        loadClinicMap();
        
        // Add listener for TableView selection to update QR code
        if (table != null) {
            table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateQRCode(newVal);
                } else {
                    if (qrCodeImageView != null) {
                        qrCodeImageView.setImage(null);
                    }
                }
            });
        }
    }

    private void bindSessionIdentity() {
        UserSession session = UserSession.getInstance();
        if (!session.isLoggedIn()) {
            showWarningAlert("Authentication Required", "Please sign in to access your appointments.");
            return;
        }

        if (txtPatientName != null) {
            txtPatientName.setText(session.getName());
        }
        if (txtPatientEmail != null) {
            txtPatientEmail.setText(session.getEmail());
        }
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
            UserSession session = UserSession.getInstance();
            if (!session.isLoggedIn()) {
                appointmentList = FXCollections.observableArrayList();
                table.setItems(appointmentList);
                displayRecommendations("");
                return;
            }
            appointmentList = FXCollections.observableArrayList(service.getByUserId(session.getUserId()));
            table.setItems(appointmentList);
            rebuildCalendarMarkers();
            displayRecommendations(notesArea != null ? notesArea.getText() : "");
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
        // Keep recommendation trigger explicit; avoid suggesting items before appointments exist.
    }

    @FXML
    public void onSpecialtySelected(ActionEvent event) {
        populateDoctorsForSelectedSpecialty(specialtyComboBox.getValue());
    }

    @FXML
    public void handleAiAssistant() {
        if (!ensureLoggedIn()) {
            return;
        }
        if (!hasBookedAppointments()) {
            showWarningAlert("AI Assistant", "Please book at least one appointment before using AI suggestions.");
            return;
        }

        String notes = notesArea != null ? notesArea.getText() : "";
        if ((notes == null || notes.isBlank()) && table != null) {
            Appointment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                notes = selected.getNotes();
            }
        }

        NavigationManager.getInstance().showAiSuggestions(notes == null ? "" : notes.trim());
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
        if (!ensureLoggedIn()) {
            return;
        }
        if (!hasBookedAppointments()) {
            showWarningAlert("Recommendations", "Book an appointment first to get relevant product suggestions.");
            return;
        }
        displayRecommendations(notesArea != null ? notesArea.getText() : "");
    }

    private void displayRecommendations(String patientNotes) {
        if (recommendationsVBox == null) {
            return;
        }

        recommendationsVBox.getChildren().clear();

        if (!hasBookedAppointments()) {
            recommendationsVBox.getChildren().add(createRecommendationMessage("Book an appointment first, then click 'Generate Recommendations' to get AI suggestions."));
            return;
        }

        String cleanedNotes = patientNotes == null ? "" : patientNotes.trim();
        if (cleanedNotes.isEmpty()) {
            recommendationsVBox.getChildren().add(createRecommendationMessage("Add clear symptoms in notes, then click 'Generate Recommendations'."));
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

        if (recommendations.size() > MAX_RECOMMENDATIONS) {
            return new ArrayList<>(recommendations.subList(0, MAX_RECOMMENDATIONS));
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
        if (!ensureLoggedIn()) {
            return;
        }
        if (!validateInput()) return;

        try {
            UserSession session = UserSession.getInstance();
            // Fusion de la date et de l'heure
            LocalDateTime ldt = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.parse(txtHeure.getText()));
            Timestamp ts = Timestamp.valueOf(ldt);

            String doctorName = doctorComboBox.getValue();
            String patientEmail = session.getEmail();
            String patientName = session.getName();
            
            Appointment a = new Appointment(
                    patientEmail,
                    patientName,
                    buildDoctorEmail(doctorName),
                    doctorName,
                    ts,
                    "pending",
                    notesArea.getText()
            );

            service.ajouter(a);

            // EVENT SUCCESS
            showInfoAlert("Appointment Booked", "Succès ! Rendez-vous enregistré pour " + txtPatientName.getText());

            // 🚀 SEND SMS NOTIFICATION ASYNCHRONOUSLY (Background Thread)
            sendAppointmentConfirmationSms(patientEmail, patientName, ts.toString());

            clearFields();
            loadAppointments();
        } catch (Exception e) {
            showErrorAlert("Erreur", "Format heure invalide (HH:mm) ou erreur DB.");
        }
    }

    /**
     * Send appointment confirmation SMS asynchronously (non-blocking)
     * Uses CompletableFuture to run on a background thread
     */
    private void sendAppointmentConfirmationSms(String patientEmail, String patientName, String appointmentDateTime) {
        CompletableFuture.runAsync(() -> {
            try {
                // Extract phone number from patient email or use default Tunisia format
                String patientPhone = extractPhoneFromEmail(patientEmail);
                
                if (patientPhone != null && !patientPhone.isEmpty()) {
                    // Normalize to Tunisia format if needed
                    String normalizedPhone = smsService.normalizeTunisianPhone(patientPhone);
                    
                    System.out.println("📱 Sending SMS to: " + normalizedPhone);
                    smsService.sendAppointmentConfirmation(normalizedPhone, patientName, appointmentDateTime);
                } else {
                    System.out.println("⚠️ No valid phone number found for SMS notification");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send SMS: " + e.getMessage());
                e.printStackTrace();
            }
        }).exceptionally(throwable -> {
            System.err.println("❌ Async SMS Error: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
    }

    /**
     * Extract phone number from patient email
     * For now, returns null (you can implement custom logic here)
     * In production, fetch from patient profile or database
     */
    private String extractPhoneFromEmail(String email) {
        // TODO: Implement logic to get phone number from:
        // 1. Patient profile in database
        // 2. Additional user info stored during registration
        // 3. A phone field in the appointment form
        
        // For now, return null to prevent errors
        return null;
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
        if (!ensureLoggedIn()) {
            return;
        }
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null && validateInput()) {
            try {
                UserSession session = UserSession.getInstance();
                LocalDateTime ldt = LocalDateTime.of(bookingDatePicker.getValue(), LocalTime.parse(txtHeure.getText()));
                s.setPatient_email(session.getEmail());
                s.setPatient_name(session.getName());
                s.setNotes(notesArea.getText());
                s.setDoctor_email(buildDoctorEmail(doctorComboBox.getValue()));
                s.setDoctor_name(doctorComboBox.getValue());
                s.setAppointment_date(Timestamp.valueOf(ldt));
                service.modifier(s);
                
                // 🚀 SEND SMS NOTIFICATION FOR UPDATE (Background Thread)
                sendAppointmentModificationSms(session.getEmail(), session.getName(), 
                                               s.getAppointment_date().toString());
                
                loadAppointments();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Send appointment modification SMS asynchronously
     */
    private void sendAppointmentModificationSms(String patientEmail, String patientName, String newDateTime) {
        CompletableFuture.runAsync(() -> {
            try {
                String patientPhone = extractPhoneFromEmail(patientEmail);
                if (patientPhone != null && !patientPhone.isEmpty()) {
                    String normalizedPhone = smsService.normalizeTunisianPhone(patientPhone);
                    System.out.println("📱 Sending appointment update SMS to: " + normalizedPhone);
                    smsService.sendAppointmentConfirmation(normalizedPhone, patientName, newDateTime);
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send update SMS: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("❌ Async SMS Update Error: " + throwable.getMessage());
            return null;
        });
    }

    @FXML
    public void handleSupprimer() {
        if (!ensureLoggedIn()) {
            return;
        }
        Appointment s = table.getSelectionModel().getSelectedItem();
        if (s != null) {
            try {
                // 🚀 SEND SMS CANCELLATION NOTIFICATION (Background Thread)
                sendAppointmentCancellationSms(s.getPatient_email(), s.getPatient_name());
                
                service.supprimer(s.getId());
                loadAppointments();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    /**
     * Send appointment cancellation SMS asynchronously
     */
    private void sendAppointmentCancellationSms(String patientEmail, String patientName) {
        CompletableFuture.runAsync(() -> {
            try {
                String patientPhone = extractPhoneFromEmail(patientEmail);
                if (patientPhone != null && !patientPhone.isEmpty()) {
                    String normalizedPhone = smsService.normalizeTunisianPhone(patientPhone);
                    System.out.println("📱 Sending appointment cancellation SMS to: " + normalizedPhone);
                    smsService.sendAppointmentCancellation(normalizedPhone, patientName);
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send cancellation SMS: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            System.err.println("❌ Async SMS Cancellation Error: " + throwable.getMessage());
            return null;
        });
    }

    @FXML
    public void handleDownloadPdf() {
        if (!ensureLoggedIn()) {
            return;
        }
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Select an appointment from the table to download its proof PDF.");
            return;
        }

        File downloadsDir = getDownloadsDirectory();
        File targetFile = buildUniquePdfFile(downloadsDir, "appointment-proof-" + selected.getId());

        try {
            appointmentPdfService.exportAppointmentProof(selected, targetFile);
            showInfoAlert("PDF Created", "Appointment proof saved:\n" + targetFile.getAbsolutePath());
        } catch (Exception e) {
            showErrorAlert("PDF Error", "Could not generate PDF proof: " + e.getMessage());
        }
    }

    @FXML
    public void handleDownloadPDF() {
        if (!ensureLoggedIn()) {
            return;
        }
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select an appointment from the table first.");
            return;
        }

        File downloadsDir = getDownloadsDirectory();
        File targetFile = buildUniquePdfFile(downloadsDir, "INV-" + selected.getId());

        try {
            AppointmentPdfService.CloudPdfResult result = appointmentPdfService.exportAppointmentInvoiceWithCloudLink(selected, targetFile);
            appointmentInvoiceUrls.put(selected.getId(), result.getPublicUrl());
            updateQRCode(selected);

            String mode = result.isGeneratedByCloudApi() ? "Cloud API" : "Local fallback";
            showInfoAlert(
                    "PDF Created",
                    "Invoice saved successfully (" + mode + "):\n"
                            + targetFile.getAbsolutePath()
                            + "\n\nOnline URL for QR:\n"
                            + result.getPublicUrl()
            );
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

    private boolean ensureLoggedIn() {
        if (UserSession.getInstance().isLoggedIn()) {
            return true;
        }
        showWarningAlert("Authentication Required", "Please sign in to perform this action.");
        return false;
    }

    private boolean hasBookedAppointments() {
        return appointmentList != null && !appointmentList.isEmpty();
    }

    private File getDownloadsDirectory() {
        String userHome = System.getProperty("user.home", ".");
        File downloads = new File(userHome, "Downloads");
        if (downloads.exists() || downloads.mkdirs()) {
            return downloads;
        }
        return new File(userHome);
    }

    private File buildUniquePdfFile(File directory, String baseName) {
        File file = new File(directory, baseName + ".pdf");
        int suffix = 1;
        while (file.exists()) {
            file = new File(directory, baseName + "-" + suffix + ".pdf");
            suffix++;
        }
        return file;
    }

    private void updateQRCode(Appointment appt) {
        if (qrCodeImageView == null) {
            return;
        }

        try {
            String publicPdfUrl = appointmentInvoiceUrls.get(appt.getId());
            if (publicPdfUrl == null || publicPdfUrl.isBlank()) {
                publicPdfUrl = appointmentPdfService.getPublicPdfUrl(appt);
            }

            String encodedData = URLEncoder.encode(publicPdfUrl, StandardCharsets.UTF_8.toString());

            // Construct the QRServer API endpoint
            String qrApiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + encodedData;

            // Load the image asynchronously (non-blocking)
            Image qrImage = new Image(qrApiUrl, 150, 150, true, true);
            qrCodeImageView.setImage(qrImage);

        } catch (Exception e) {
            System.err.println("Error generating QR code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load clinic location map asynchronously
     * Fetches static map image from API and displays it in ImageView
     * Handles errors gracefully with fallback UI
     */
    private void loadClinicMap() {
        if (clinicMapView == null) {
            System.out.println("⚠️ Clinic map view not found in FXML");
            return;
        }

        // Set placeholder while loading
        clinicMapView.setStyle("-fx-opacity: 0.7;");
        
        // Fetch map asynchronously (non-blocking)
        StaticMapService.fetchClinicMapAsync()
                .thenAccept(mapImage -> {
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        if (mapImage != null) {
                            try {
                                clinicMapView.setImage(mapImage);
                                clinicMapView.setStyle("-fx-opacity: 1.0; -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 3);");
                                System.out.println("✅ Clinic map displayed successfully");
                            } catch (Exception e) {
                                System.err.println("❌ Error setting clinic map image: " + e.getMessage());
                                handleMapLoadError();
                            }
                        } else {
                            handleMapLoadError();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Error in async map loading: " + throwable.getMessage());
                    Platform.runLater(this::handleMapLoadError);
                    return null;
                });
    }

    /**
     * Handle clinic map loading errors
     * Shows error message and hides the map view gracefully
     */
    private void handleMapLoadError() {
        try {
            // Set a light gray background to indicate error
            clinicMapView.setStyle(
                "-fx-background-color: #f0f0f0; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.15), 8, 0, 0, 3); " +
                "-fx-opacity: 0.5;"
            );
            System.out.println("⚠️ Clinic map failed to load - showing placeholder");
        } catch (Exception e) {
            System.err.println("❌ Error handling map load error: " + e.getMessage());
        }
    }

    private void showInfoAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showWarningAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
    private void showErrorAlert(String t, String c) { Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait(); }
}

