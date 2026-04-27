package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class DashboardController {

    private static final String DARK_MODE_CLASS = "dark-mode";

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentArea;

    @FXML
    private ToggleButton nightModeToggle;

    @FXML
    private HBox weatherWarningBox;

    @FXML
    private Label weatherLabel;

    private boolean darkModeEnabled;
    private final AirQualityService airQualityService = new AirQualityService();

    @FXML
    public void initialize() {
        NavigationManager.getInstance().registerContentArea(contentArea);
        NavigationManager.getInstance().setDarkMode(darkModeEnabled);
        loadWelcomeDashboard();
        applyThemeClass(rootPane);
        
        // Initialize air quality widget
        loadAirQualityWidget();
    }

    /**
     * Load air quality widget asynchronously
     * Fetches data from OpenWeatherMap API and updates UI on success
     */
    private void loadAirQualityWidget() {
        if (weatherLabel == null || weatherWarningBox == null) {
            System.out.println("⚠️ Weather widget elements not found in FXML");
            return;
        }

        // Set default message while loading
        weatherLabel.setText("🌍 Loading air quality data...");
        weatherWarningBox.setStyle("-fx-background-color: #e2e3e5; -fx-border-color: #6c757d; -fx-border-radius: 5; -fx-padding: 15;");

        // Fetch air quality asynchronously (non-blocking)
        airQualityService.fetchAirQualityAsync()
                .thenAccept(airQualityData -> {
                    // Update UI on JavaFX thread
                    Platform.runLater(() -> {
                        if (airQualityData != null) {
                            updateWeatherWidget(airQualityData);
                        } else {
                            handleAirQualityError();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Error in air quality async call: " + throwable.getMessage());
                    Platform.runLater(this::handleAirQualityError);
                    return null;
                });
    }

    /**
     * Update weather widget with air quality data
     */
    private void updateWeatherWidget(AirQualityService.AirQualityData data) {
        try {
            // Get health warning message and colors based on AQI
            String message = airQualityService.getHealthWarning(data.aqi);
            String boxStyle = airQualityService.getWarningBoxColor(data.aqi);
            String textStyle = airQualityService.getWarningTextColor(data.aqi);

            // Add border radius and padding to the style
            boxStyle += " -fx-border-radius: 5; -fx-padding: 15;";

            // Add emoji based on AQI level
            String emoji = getAqiEmoji(data.aqi);
            String fullMessage = emoji + " " + message;

            // Update label
            weatherLabel.setText(fullMessage);
            weatherLabel.setStyle(textStyle);

            // Update box background
            weatherWarningBox.setStyle(boxStyle);

            System.out.println("✅ Weather widget updated: " + data);
        } catch (Exception e) {
            System.err.println("❌ Error updating weather widget: " + e.getMessage());
            handleAirQualityError();
        }
    }

    /**
     * Handle air quality service errors
     */
    private void handleAirQualityError() {
        try {
            weatherLabel.setText("⚠️ Unable to fetch air quality data. Please try again later.");
            weatherWarningBox.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffc107; -fx-border-radius: 5; -fx-padding: 15;");
            weatherLabel.setStyle("-fx-text-fill: #856404;");
        } catch (Exception e) {
            System.err.println("❌ Error handling air quality error: " + e.getMessage());
        }
    }

    /**
     * Get emoji representation of AQI level
     */
    private String getAqiEmoji(int aqi) {
        return switch (aqi) {
            case 1, 2 -> "😊"; // Good/Fair
            case 3 -> "😐"; // Moderate
            case 4, 5 -> "😷"; // Poor/Hazardous
            default -> "🌍";
        };
    }
    
    private void loadWelcomeDashboard() {
        VBox welcomeBox = new VBox(30);
        welcomeBox.setAlignment(javafx.geometry.Pos.CENTER);
        welcomeBox.getStyleClass().add("welcome-dashboard");
        welcomeBox.setStyle("-fx-padding: 40;");

        Label titleLabel = new Label("Welcome to PinkShield");
        titleLabel.getStyleClass().add("dashboard-title");

        Label subtitleLabel = new Label("Your trusted healthcare companion");
        subtitleLabel.getStyleClass().add("dashboard-subtitle");

        HBox featureBox = new HBox(20);
        featureBox.setAlignment(javafx.geometry.Pos.CENTER);

        VBox appointmentsCard = createFeatureCard("📅 Appointments", "Book and manage your healthcare appointments");
        VBox parapharmacieCard = createFeatureCard("💊 Parapharmacie", "Browse healthcare products and add to wishlist");
        VBox wishlistCard = createFeatureCard("❤️ Wishlist", "View your saved products");

        featureBox.getChildren().addAll(appointmentsCard, parapharmacieCard, wishlistCard);

        welcomeBox.getChildren().addAll(titleLabel, subtitleLabel, featureBox);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(welcomeBox);
        applyThemeClass(welcomeBox);
    }

    private VBox createFeatureCard(String title, String description) {
        VBox card = new VBox(10);
        card.getStyleClass().add("feature-card");
        card.setStyle("-fx-padding: 25; -fx-cursor: hand;");
        card.setPrefWidth(250);
        card.setPrefHeight(120);
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        Label iconLabel = new Label(title.split(" ")[0]);
        iconLabel.getStyleClass().add("feature-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("feature-title");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("feature-description");
        descLabel.setWrapText(true);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);
        
        card.setOnMouseClicked(e -> {
            if (title.contains("Appointments")) {
                handleAppointments();
            } else if (title.contains("Parapharmacie")) {
                handleParapharmacie();
            } else if (title.contains("Wishlist")) {
                handleWishlist();
            }
        });
        
        return card;
    }

    @FXML
    public void handleToggleNightMode() {
        darkModeEnabled = nightModeToggle != null && nightModeToggle.isSelected();
        if (nightModeToggle != null) {
            nightModeToggle.setText(darkModeEnabled ? "Day Mode" : "Night Mode");
        }
        NavigationManager.getInstance().setDarkMode(darkModeEnabled);
        applyThemeClass(rootPane);
        if (!contentArea.getChildren().isEmpty()) {
            applyThemeClass(contentArea.getChildren().getFirst());
        }
    }

    @FXML
    public void handleAppointments() {
        NavigationManager.getInstance().showAppointments();
    }

    @FXML
    public void handleParapharmacie() {
        NavigationManager.getInstance().showParapharmacie();
    }

    @FXML
    public void handleWishlist() {
        NavigationManager.getInstance().showWishlist();
    }

    @FXML
    public void handleRiskAnalyser() {
        NavigationManager.getInstance().showRiskAnalyser();
    }

    @FXML
    public void handleLogout() {
        if (!confirmLogout()) {
            return;
        }

        UserSession.getInstance().cleanUserSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Auth.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1400, 800));
            stage.setTitle("PinkShield - Sign In");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading login page: " + e.getMessage());
        }
    }

    private boolean confirmLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Logout Confirmation");
        confirmAlert.setHeaderText("Are you sure you want to log out?");
        confirmAlert.setContentText("You will be returned to the sign in page.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    public void handleDashboard() {
        loadWelcomeDashboard();
    }


    private void applyThemeClass(Node node) {
        if (node == null) {
            return;
        }

        if (darkModeEnabled) {
            if (!node.getStyleClass().contains(DARK_MODE_CLASS)) {
                node.getStyleClass().add(DARK_MODE_CLASS);
            }
        } else {
            node.getStyleClass().remove(DARK_MODE_CLASS);
        }
    }
}
