package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import tn.esprit.entities.DailyTrackingEntry;
import tn.esprit.entities.DailyTrackingStats;
import tn.esprit.entities.DailyTrackingTrendPoint;
import tn.esprit.entities.User;
import tn.esprit.services.DailyTrackingService;
import tn.esprit.utils.FormValidator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyTrackingController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter CHART_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm");

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label modeBadgeLabel;

    @FXML private VBox logFormCard;
    @FXML private Label formHeadingLabel;
    @FXML private Label formHelperLabel;
    @FXML private Label formFeedbackLabel;
    @FXML private DatePicker entryDatePicker;
    @FXML private Slider moodSlider;
    @FXML private Label moodValueLabel;
    @FXML private Slider stressSlider;
    @FXML private Label stressValueLabel;
    @FXML private Slider energySlider;
    @FXML private Label energyValueLabel;
    @FXML private Slider sleepSlider;
    @FXML private Label sleepValueLabel;
    @FXML private Slider focusSlider;
    @FXML private Label focusValueLabel;
    @FXML private Slider anxietySlider;
    @FXML private Label anxietyValueLabel;
    @FXML private Slider motivationSlider;
    @FXML private Label motivationValueLabel;
    @FXML private Slider socialSlider;
    @FXML private Label socialValueLabel;
    @FXML private Slider appetiteSlider;
    @FXML private Label appetiteValueLabel;
    @FXML private Slider waterSlider;
    @FXML private Label waterValueLabel;
    @FXML private Slider activitySlider;
    @FXML private Label activityValueLabel;
    @FXML private CheckBox medicationTakenCheckBox;
    @FXML private TextArea activitiesArea;
    @FXML private TextArea symptomsArea;
    @FXML private Button saveCheckInButton;

    @FXML private Label latestContextLabel;
    @FXML private Label latestMoodLabel;
    @FXML private Label latestStressLabel;
    @FXML private Label latestEnergyLabel;
    @FXML private Label latestSleepLabel;
    @FXML private Label latestWaterLabel;
    @FXML private Label latestActivityLabel;
    @FXML private Label latestMedicationLabel;
    @FXML private Label activitiesLabel;
    @FXML private Label symptomsLabel;
    @FXML private Label weeklyEntriesLabel;
    @FXML private Label avgMoodLabel;
    @FXML private Label avgStressLabel;
    @FXML private Label avgEnergyLabel;
    @FXML private Label avgSleepLabel;
    @FXML private Label avgWaterLabel;
    @FXML private Label avgActivityLabel;
    @FXML private Label recentEntriesLabel;
    @FXML private VBox recentEntriesBox;
    @FXML private Label trendChartContextLabel;
    @FXML private Label trendEmptyLabel;
    @FXML private LineChart<String, Number> trendChart;
    @FXML private NumberAxis trendYAxis;

    private final DailyTrackingService dailyTrackingService = new DailyTrackingService();

    private User currentUser;
    private boolean patientMode;
    private boolean updatingForm;

    @FXML
    public void initialize() {
        configureDatePicker();
        configureSliders();
        configureFormListeners();
        applyFormDefaults(LocalDate.now());
        Platform.runLater(this::clipTrackingCards);
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        if (user == null) {
            patientMode = false;
            setFormVisible(false);
            renderEmptyState("No user context available.");
            return;
        }

        patientMode = "user".equalsIgnoreCase(user.getRole());
        configureHeader(patientMode);
        setFormVisible(patientMode);
        refreshDashboard();

        if (patientMode) {
            loadFormForSelectedDate(resolveSelectedDate());
        } else {
            clearFormMessage();
        }
    }

    @FXML
    public void handleSaveCheckIn() {
        if (!patientMode || currentUser == null) {
            FormValidator.setMessage(formFeedbackLabel, "Only patient accounts can save daily check-ins.", true);
            return;
        }

        LocalDate entryDate = entryDatePicker.getValue();
        FormValidator.clearFieldState(entryDatePicker);

        if (entryDate == null) {
            FormValidator.markInvalid(entryDatePicker);
            FormValidator.setMessage(formFeedbackLabel, "Please choose a check-in date.", true);
            return;
        }

        if (entryDate.isAfter(LocalDate.now())) {
            FormValidator.markInvalid(entryDatePicker);
            FormValidator.setMessage(formFeedbackLabel, "Check-in dates cannot be in the future.", true);
            return;
        }

        DailyTrackingEntry entry = buildEntryFromForm(entryDate);
        boolean saved = dailyTrackingService.saveEntry(entry);
        if (!saved) {
            FormValidator.setMessage(formFeedbackLabel, "Unable to save the daily check-in. Check the database mapping.", true);
            return;
        }

        refreshDashboard();
        loadFormForSelectedDate(entryDate);
        FormValidator.setMessage(formFeedbackLabel, "Check-in saved for " + entryDate.format(DATE_FORMAT) + ".", false);
    }

    private void configureDatePicker() {
        entryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
        entryDatePicker.setValue(LocalDate.now());
        entryDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> {
            FormValidator.clearFieldState(entryDatePicker);
            clearFormMessage();
            if (updatingForm || !patientMode || currentUser == null || newValue == null) {
                return;
            }
            loadFormForSelectedDate(newValue);
        });
    }

    private void configureSliders() {
        bindSlider(moodSlider, moodValueLabel, "");
        bindSlider(stressSlider, stressValueLabel, "");
        bindSlider(energySlider, energyValueLabel, "");
        bindSlider(sleepSlider, sleepValueLabel, " h");
        bindSlider(focusSlider, focusValueLabel, "");
        bindSlider(anxietySlider, anxietyValueLabel, "");
        bindSlider(motivationSlider, motivationValueLabel, "");
        bindSlider(socialSlider, socialValueLabel, "");
        bindSlider(appetiteSlider, appetiteValueLabel, "");
        bindSlider(waterSlider, waterValueLabel, " cl");
        bindSlider(activitySlider, activityValueLabel, "");
    }

    private void configureFormListeners() {
        FormValidator.attachClearOnInput(formFeedbackLabel, activitiesArea, symptomsArea);

        medicationTakenCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> clearFormMessage());
    }

    private void clipTrackingCards() {
        if (logFormCard == null) {
            return;
        }

        logFormCard.lookupAll(".tracking-input-card").forEach(node -> {
            if (!(node instanceof Region region)) {
                return;
            }

            Rectangle clip = new Rectangle();
            clip.setArcWidth(28);
            clip.setArcHeight(28);
            clip.widthProperty().bind(region.widthProperty());
            clip.heightProperty().bind(region.heightProperty());
            region.setClip(clip);
        });
    }

    private void bindSlider(Slider slider, Label valueLabel, String suffix) {
        valueLabel.setText(formatSliderValue(slider.getValue(), suffix));
        slider.valueProperty().addListener((obs, oldValue, newValue) -> {
            valueLabel.setText(formatSliderValue(newValue.doubleValue(), suffix));
            clearFormMessage();
        });
    }

    private void configureHeader(boolean patientMode) {
        titleLabel.setText("Daily Check In");
        if (patientMode) {
            subtitleLabel.setText("Log a health check-in for any day and review your latest wellness trends.");
            modeBadgeLabel.setText("PERSONAL");
            return;
        }

        subtitleLabel.setText("Monitor the latest patient health progress and weekly wellness averages.");
        modeBadgeLabel.setText("MONITOR");
    }

    private void refreshDashboard() {
        if (currentUser == null) {
            renderEmptyState("No user context available.");
            return;
        }

        DailyTrackingEntry latestEntry = patientMode
                ? dailyTrackingService.getLatestEntryForUser(currentUser.getId())
                : dailyTrackingService.getLatestEntryForAllUsers();

        DailyTrackingStats weeklyStats = patientMode
                ? dailyTrackingService.getWeeklyStatsForUser(currentUser.getId())
                : dailyTrackingService.getWeeklyStatsForAllUsers();

        List<DailyTrackingEntry> recentEntries = patientMode
                ? dailyTrackingService.getRecentEntriesForUser(currentUser.getId(), 5)
                : dailyTrackingService.getRecentEntriesForAllUsers(5);

        List<DailyTrackingTrendPoint> trendPoints = patientMode
                ? dailyTrackingService.getTrendPointsForUser(currentUser.getId())
                : dailyTrackingService.getTrendPointsForAllUsers();

        renderLatestEntry(latestEntry, patientMode);
        renderWeeklyStats(weeklyStats);
        renderRecentEntries(recentEntries, patientMode);
        renderTrendChart(trendPoints, patientMode);
    }

    private void loadFormForSelectedDate(LocalDate selectedDate) {
        if (!patientMode || currentUser == null || selectedDate == null) {
            return;
        }

        DailyTrackingEntry existingEntry = dailyTrackingService.getEntryForUserAndDate(currentUser.getId(), selectedDate);
        if (existingEntry == null) {
            applyFormDefaults(selectedDate);
            formHeadingLabel.setText("Log check-in for " + selectedDate.format(DATE_FORMAT));
            formHelperLabel.setText(selectedDate.equals(LocalDate.now())
                    ? "Save how you feel today. If you already have a log for this date, it will be updated."
                    : "You can backfill a missed day. Saving will create a check-in for this date.");
            return;
        }

        populateForm(existingEntry);
        formHeadingLabel.setText("Update check-in for " + selectedDate.format(DATE_FORMAT));
        formHelperLabel.setText("A check-in already exists for this date. Saving again updates that entry.");
    }

    private void populateForm(DailyTrackingEntry entry) {
        updatingForm = true;
        try {
            entryDatePicker.setValue(entry.getDate() != null ? entry.getDate() : LocalDate.now());
            moodSlider.setValue(safeValue(entry.getMood(), 5));
            stressSlider.setValue(safeValue(entry.getStress(), 5));
            energySlider.setValue(safeValue(entry.getEnergyLevel(), 5));
            sleepSlider.setValue(safeValue(entry.getSleepHours(), 7));
            focusSlider.setValue(safeValue(entry.getFocusLevel(), 5));
            anxietySlider.setValue(safeValue(entry.getAnxietyLevel(), 3));
            motivationSlider.setValue(safeValue(entry.getMotivationLevel(), 5));
            socialSlider.setValue(safeValue(entry.getSocialInteractionLevel(), 5));
            appetiteSlider.setValue(safeValue(entry.getAppetiteLevel(), 5));
            waterSlider.setValue(safeValue(entry.getWaterIntake(), 150));
            activitySlider.setValue(safeValue(entry.getPhysicalActivityLevel(), 3));
            medicationTakenCheckBox.setSelected(Boolean.TRUE.equals(entry.getMedicationTaken()));
            activitiesArea.setText(valueOrEmpty(entry.getActivities()));
            symptomsArea.setText(valueOrEmpty(entry.getSymptoms()));
        } finally {
            updatingForm = false;
        }
    }

    private void applyFormDefaults(LocalDate selectedDate) {
        updatingForm = true;
        try {
            entryDatePicker.setValue(selectedDate != null ? selectedDate : LocalDate.now());
            moodSlider.setValue(5);
            stressSlider.setValue(5);
            energySlider.setValue(5);
            sleepSlider.setValue(7);
            focusSlider.setValue(5);
            anxietySlider.setValue(3);
            motivationSlider.setValue(5);
            socialSlider.setValue(5);
            appetiteSlider.setValue(5);
            waterSlider.setValue(150);
            activitySlider.setValue(3);
            medicationTakenCheckBox.setSelected(false);
            activitiesArea.clear();
            symptomsArea.clear();
        } finally {
            updatingForm = false;
        }
    }

    private DailyTrackingEntry buildEntryFromForm(LocalDate entryDate) {
        DailyTrackingEntry entry = new DailyTrackingEntry();
        entry.setUserId(currentUser.getId());
        entry.setDate(entryDate);
        entry.setMood(sliderValue(moodSlider));
        entry.setStress(sliderValue(stressSlider));
        entry.setEnergyLevel(sliderValue(energySlider));
        entry.setSleepHours(sliderValue(sleepSlider));
        entry.setFocusLevel(sliderValue(focusSlider));
        entry.setAnxietyLevel(sliderValue(anxietySlider));
        entry.setMotivationLevel(sliderValue(motivationSlider));
        entry.setSocialInteractionLevel(sliderValue(socialSlider));
        entry.setAppetiteLevel(sliderValue(appetiteSlider));
        entry.setWaterIntake(sliderValue(waterSlider));
        entry.setPhysicalActivityLevel(sliderValue(activitySlider));
        entry.setMedicationTaken(medicationTakenCheckBox.isSelected());
        entry.setActivities(normalizeText(activitiesArea.getText()));
        entry.setSymptoms(normalizeText(symptomsArea.getText()));
        return entry;
    }

    private void renderLatestEntry(DailyTrackingEntry entry, boolean patientMode) {
        if (entry == null) {
            latestContextLabel.setText("No daily check-in data available yet.");
            latestMoodLabel.setText("-");
            latestStressLabel.setText("-");
            latestEnergyLabel.setText("-");
            latestSleepLabel.setText("-");
            latestWaterLabel.setText("-");
            latestActivityLabel.setText("-");
            latestMedicationLabel.setText("-");
            activitiesLabel.setText("No activities recorded.");
            symptomsLabel.setText("No symptoms recorded.");
            return;
        }

        String contextPrefix = patientMode
                ? "Latest entry"
                : defaultText(entry.getUserName(), "Latest patient") + " - latest entry";
        String timestamp = entry.getCreatedAt() != null
                ? entry.getCreatedAt().format(DATE_TIME_FORMAT)
                : entry.getDate() != null ? entry.getDate().format(DATE_FORMAT) : "Unknown date";

        latestContextLabel.setText(contextPrefix + " - " + timestamp);
        latestMoodLabel.setText(formatTenScale(entry.getMood()));
        latestStressLabel.setText(formatTenScale(entry.getStress()));
        latestEnergyLabel.setText(formatTenScale(entry.getEnergyLevel()));
        latestSleepLabel.setText(formatHours(entry.getSleepHours()));
        latestWaterLabel.setText(formatWater(entry.getWaterIntake()));
        latestActivityLabel.setText(formatTenScale(entry.getPhysicalActivityLevel()));
        latestMedicationLabel.setText(formatMedication(entry.getMedicationTaken()));
        activitiesLabel.setText(defaultText(entry.getActivities(), "No activities recorded."));
        symptomsLabel.setText(defaultText(entry.getSymptoms(), "No symptoms recorded."));
    }

    private void renderWeeklyStats(DailyTrackingStats stats) {
        weeklyEntriesLabel.setText(stats.getEntryCount() + " check-in(s) in the last 7 days");
        avgMoodLabel.setText(formatAverage(stats.getAverageMood(), "/10"));
        avgStressLabel.setText(formatAverage(stats.getAverageStress(), "/10"));
        avgEnergyLabel.setText(formatAverage(stats.getAverageEnergyLevel(), "/10"));
        avgSleepLabel.setText(formatAverage(stats.getAverageSleepHours(), " h"));
        avgWaterLabel.setText(formatAverage(stats.getAverageWaterIntake(), " cl"));
        avgActivityLabel.setText(formatAverage(stats.getAveragePhysicalActivityLevel(), "/10"));
    }

    private void renderRecentEntries(List<DailyTrackingEntry> entries, boolean patientMode) {
        recentEntriesBox.getChildren().clear();
        recentEntriesLabel.setText(entries.size() + " recent check-in(s)");

        if (entries.isEmpty()) {
            recentEntriesBox.getChildren().add(createEmptyEntryCard("No recent check-ins found."));
            return;
        }

        for (DailyTrackingEntry entry : entries) {
            recentEntriesBox.getChildren().add(createEntryCard(entry, patientMode));
        }
    }

    private void renderTrendChart(List<DailyTrackingTrendPoint> points, boolean patientMode) {
        trendChartContextLabel.setText(patientMode
                ? "Mood and stress trend over your last 7 days of check-ins"
                : "Average mood and stress trend across patients for the last 7 days");

        trendChart.getData().clear();
        trendYAxis.setAutoRanging(false);
        trendYAxis.setLowerBound(0);
        trendYAxis.setUpperBound(10);
        trendYAxis.setTickUnit(1);

        if (points.isEmpty()) {
            trendChart.setManaged(false);
            trendChart.setVisible(false);
            trendEmptyLabel.setManaged(true);
            trendEmptyLabel.setVisible(true);
            return;
        }

        XYChart.Series<String, Number> moodSeries = new XYChart.Series<>();
        moodSeries.setName("Mood");

        XYChart.Series<String, Number> stressSeries = new XYChart.Series<>();
        stressSeries.setName("Stress");

        for (DailyTrackingTrendPoint point : points) {
            String label = point.getDate() != null ? point.getDate().format(CHART_DATE_FORMAT) : "Unknown";
            if (point.getAverageMood() != null) {
                moodSeries.getData().add(new XYChart.Data<>(label, point.getAverageMood()));
            }
            if (point.getAverageStress() != null) {
                stressSeries.getData().add(new XYChart.Data<>(label, point.getAverageStress()));
            }
        }

        if (!moodSeries.getData().isEmpty()) {
            trendChart.getData().add(moodSeries);
        }
        if (!stressSeries.getData().isEmpty()) {
            trendChart.getData().add(stressSeries);
        }

        boolean hasChartData = !trendChart.getData().isEmpty();
        trendChart.setManaged(hasChartData);
        trendChart.setVisible(hasChartData);
        trendEmptyLabel.setManaged(!hasChartData);
        trendEmptyLabel.setVisible(!hasChartData);
    }

    private VBox createEntryCard(DailyTrackingEntry entry, boolean patientMode) {
        VBox card = new VBox(8);
        card.getStyleClass().add("tracking-entry-card");
        card.setPadding(new Insets(14));

        HBox header = new HBox(12);
        Label title = new Label(patientMode
                ? formatEntryDate(entry)
                : defaultText(entry.getUserName(), "Unknown patient") + " - " + formatEntryDate(entry));
        title.getStyleClass().add("tracking-entry-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label summary = new Label("Mood " + formatTenScale(entry.getMood())
                + " | Stress " + formatTenScale(entry.getStress())
                + " | Sleep " + formatHours(entry.getSleepHours()));
        summary.getStyleClass().add("tracking-entry-meta");

        header.getChildren().addAll(title, spacer, summary);

        Label notes = new Label(buildNotesSummary(entry));
        notes.setWrapText(true);
        notes.getStyleClass().add("tracking-entry-meta");

        card.getChildren().addAll(header, notes);
        return card;
    }

    private VBox createEmptyEntryCard(String message) {
        VBox card = new VBox();
        card.getStyleClass().add("tracking-entry-card");
        card.setPadding(new Insets(14));
        Label label = new Label(message);
        label.getStyleClass().add("tracking-entry-meta");
        card.getChildren().add(label);
        return card;
    }

    private void renderEmptyState(String message) {
        latestContextLabel.setText(message);
        latestMoodLabel.setText("-");
        latestStressLabel.setText("-");
        latestEnergyLabel.setText("-");
        latestSleepLabel.setText("-");
        latestWaterLabel.setText("-");
        latestActivityLabel.setText("-");
        latestMedicationLabel.setText("-");
        activitiesLabel.setText("No activities recorded.");
        symptomsLabel.setText("No symptoms recorded.");
        renderWeeklyStats(new DailyTrackingStats());
        renderRecentEntries(List.of(), true);
        renderTrendChart(List.of(), true);
    }

    private void setFormVisible(boolean visible) {
        logFormCard.setManaged(visible);
        logFormCard.setVisible(visible);
        if (saveCheckInButton != null) {
            saveCheckInButton.setManaged(visible);
            saveCheckInButton.setVisible(visible);
        }
    }

    private LocalDate resolveSelectedDate() {
        LocalDate selectedDate = entryDatePicker.getValue();
        if (selectedDate != null) {
            return selectedDate;
        }
        return LocalDate.now();
    }

    private String buildNotesSummary(DailyTrackingEntry entry) {
        String activities = defaultText(entry.getActivities(), "No activities");
        String symptoms = defaultText(entry.getSymptoms(), "No symptoms");
        return "Activities: " + activities + " | Symptoms: " + symptoms;
    }

    private String formatEntryDate(DailyTrackingEntry entry) {
        if (entry.getDate() != null) {
            return entry.getDate().format(DATE_FORMAT);
        }
        if (entry.getCreatedAt() != null) {
            return entry.getCreatedAt().format(DATE_TIME_FORMAT);
        }
        return "Unknown date";
    }

    private String formatTenScale(Integer value) {
        return value == null ? "-" : value + "/10";
    }

    private String formatHours(Integer value) {
        return value == null ? "-" : value + " h";
    }

    private String formatWater(Integer value) {
        return value == null ? "-" : value + " cl";
    }

    private String formatMedication(Boolean value) {
        if (value == null) {
            return "-";
        }
        return value ? "Taken" : "Not taken";
    }

    private String formatAverage(Double value, String suffix) {
        return value == null ? "-" : String.format("%.1f%s", value, suffix);
    }

    private String formatSliderValue(double value, String suffix) {
        return Math.round(value) + suffix;
    }

    private int sliderValue(Slider slider) {
        return (int) Math.round(slider.getValue());
    }

    private int safeValue(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void clearFormMessage() {
        FormValidator.setMessage(formFeedbackLabel, "", true);
    }
}
