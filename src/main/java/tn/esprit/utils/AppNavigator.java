package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class AppNavigator {
    public static final double APP_WIDTH = 1240;
    public static final double APP_HEIGHT = 820;
    public static final double MIN_WIDTH = 1024;
    public static final double MIN_HEIGHT = 680;

    private AppNavigator() {
    }

    public static Scene createScene(Parent root, Class<?> resourceBase) {
        Scene scene = new Scene(root, APP_WIDTH, APP_HEIGHT);
        scene.getStylesheets().add(resourceBase.getResource("/css/style.css").toExternalForm());
        return scene;
    }

    public static Parent load(Class<?> resourceBase, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(resourceBase.getResource(fxmlPath));
        return loader.load();
    }

    public static void applyStage(Stage stage, Scene scene, String title) {
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setWidth(APP_WIDTH);
        stage.setHeight(APP_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }
}
