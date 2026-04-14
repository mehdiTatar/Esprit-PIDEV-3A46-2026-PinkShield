package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            URL fxmlResource = getClass().getResource("/Dashboard.fxml");
            if (fxmlResource == null) {
                System.err.println("Dashboard.fxml not found in resources!");
                throw new IOException("Dashboard.fxml not found");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            javafx.scene.Parent root = loader.load();
            
            Scene scene = new Scene(root, 1400, 800);
            primaryStage.setTitle("💊 PinkShield - Healthcare & Commerce Management System");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> System.exit(0));
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}



