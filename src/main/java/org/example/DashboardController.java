package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;

import java.io.IOException;
import java.net.URL;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadContent("appointment.fxml");
    }

    @FXML
    public void handleAppointments() {
        loadContent("appointment.fxml");
    }

    @FXML
    public void handleParapharmacie() {
        loadContent("parapharmacie.fxml");
    }

    @FXML
    public void handleWishlist() {
        loadContent("wishlist.fxml");
    }

    @FXML
    public void handleLogout() {
        Platform.exit();
    }

    private void loadContent(String fxmlFileName) {
        try {
            URL resource = getClass().getResource("/" + fxmlFileName);
            if (resource == null) {
                System.err.println("FXML file not found: " + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent content = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlFileName);
            e.printStackTrace();
        }
    }
}

