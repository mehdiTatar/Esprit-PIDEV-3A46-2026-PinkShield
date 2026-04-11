package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            Tab appointmentTab = new Tab("Appointments", loadFXML("appointment.fxml"));
            appointmentTab.setStyle("-fx-font-size: 14;");

            Tab parapharmacieTab = new Tab("Parapharmacie", loadFXML("parapharmacie.fxml"));
            parapharmacieTab.setStyle("-fx-font-size: 14;");

            Tab wishlistTab = new Tab("Wishlist", loadFXML("wishlist.fxml"));
            wishlistTab.setStyle("-fx-font-size: 14;");

            tabPane.getTabs().addAll(appointmentTab, parapharmacieTab, wishlistTab);

            Scene scene = new Scene(tabPane, 1200, 700);
            primaryStage.setTitle("Healthcare & Commerce Management System");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private javafx.scene.Parent loadFXML(String fxml) throws IOException {
        try {
            URL resource = getClass().getClassLoader().getResource(fxml);
            if (resource == null) {
                System.err.println("FXML file not found: " + fxml);
                throw new IOException("FXML file not found: " + fxml);
            }
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            return fxmlLoader.load();
        } catch (Exception e) {
            System.err.println("Failed to load FXML: " + fxml);
            e.printStackTrace();
            throw new IOException("Error loading FXML file: " + fxml, e);
        }
    }

    static void main(String[] args) {
        launch();
    }
}

