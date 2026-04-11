package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SimpleLauncher extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Load FXML files directly from resources
            Tab appointmentTab = new Tab("Appointments", loadFXML("appointment.fxml"));
            Tab parapharmacieTab = new Tab("Parapharmacie", loadFXML("parapharmacie.fxml"));
            Tab wishlistTab = new Tab("Wishlist", loadFXML("wishlist.fxml"));

            tabPane.getTabs().addAll(appointmentTab, parapharmacieTab, wishlistTab);

            Scene scene = new Scene(tabPane, 1200, 700);
            primaryStage.setTitle("Healthcare & Commerce Management System");
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("Application started successfully!");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private javafx.scene.Parent loadFXML(String fxml) throws IOException {
        URL resource = getClass().getClassLoader().getResource(fxml);
        if (resource == null) {
            System.out.println("Looking for: " + fxml);
            System.out.println("In classpath, trying file path...");
            java.io.File file = new java.io.File(fxml);
            if (!file.exists()) {
                file = new java.io.File("src/main/resources/" + fxml);
            }
            if (!file.exists()) {
                file = new java.io.File("target/classes/" + fxml);
            }
            if (file.exists()) {
                resource = file.toURI().toURL();
            } else {
                throw new IOException("FXML file not found: " + fxml);
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
