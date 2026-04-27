package org.example;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class AppointmentCalendarController {

    @FXML
    private Label monthLabel;

    @FXML
    private GridPane calendarGrid;

    private final ServiceAppointment service = new ServiceAppointment();
    private final Map<LocalDate, List<String>> appointmentsByDate = new HashMap<>();
    private YearMonth currentMonth = YearMonth.now();

    @FXML
    public void initialize() {
        loadAppointments();
        renderCalendar();
    }

    @FXML
    public void handleBackToAppointment() {
        NavigationManager.getInstance().showAppointments();
    }

    @FXML
    public void handleAiAssistant() {
        NavigationManager.getInstance().showAiSuggestions("");
    }

    @FXML
    public void handlePreviousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    public void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        renderCalendar();
    }

    private void loadAppointments() {
        appointmentsByDate.clear();

        try {
            UserSession session = UserSession.getInstance();
            if (!session.isLoggedIn()) {
                return;
            }

            for (Appointment appointment : service.getByUserId(session.getUserId())) {
                if (appointment.getAppointment_date() == null) {
                    continue;
                }

                LocalDate date = appointment.getAppointment_date().toLocalDateTime().toLocalDate();
                appointmentsByDate.putIfAbsent(date, new ArrayList<>());
                List<String> names = appointmentsByDate.get(date);

                String patientName = appointment.getPatient_name() != null
                        ? appointment.getPatient_name().trim()
                        : "Patient";

                LinkedHashSet<String> uniqueNames = new LinkedHashSet<>(names);
                uniqueNames.add(patientName);
                names.clear();
                names.addAll(uniqueNames);
            }
        } catch (Exception e) {
            System.err.println("Failed to load calendar appointments: " + e.getMessage());
        }
    }

    private void renderCalendar() {
        if (calendarGrid == null || monthLabel == null) {
            return;
        }

        calendarGrid.getChildren().clear();
        monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());

        String[] headers = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (int col = 0; col < headers.length; col++) {
            Label header = new Label(headers[col]);
            header.getStyleClass().add("calendar-day-header");
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            calendarGrid.add(header, col, 0);
        }

        LocalDate firstDay = currentMonth.atDay(1);
        int firstColumn = firstDay.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        int daysInMonth = currentMonth.lengthOfMonth();

        int row = 1;
        int column = Math.max(0, firstColumn);
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            calendarGrid.add(createDayCell(date), column, row);

            column++;
            if (column > 6) {
                column = 0;
                row++;
            }
        }
    }

    private StackPane createDayCell(LocalDate date) {
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyleClass().add("calendar-day-number");

        StackPane cell = new StackPane(dayNumber);
        cell.getStyleClass().add("calendar-day-cell");
        cell.setMinSize(88, 66);
        cell.setPrefSize(88, 66);
        cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(dayNumber, Pos.TOP_LEFT);

        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("today");
        }

        List<String> namesOnDate = appointmentsByDate.get(date);
        if (namesOnDate != null && !namesOnDate.isEmpty()) {
            Label bookedBy = new Label(abbreviateName(namesOnDate.getFirst()));
            bookedBy.getStyleClass().add("calendar-day-marker");
            StackPane.setAlignment(bookedBy, Pos.BOTTOM_CENTER);
            cell.getChildren().add(bookedBy);
            cell.getStyleClass().add("booked");
            Tooltip.install(cell, new Tooltip("Booked by: " + String.join(", ", namesOnDate)));
        }

        return cell;
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
}

