package tn.esprit.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import tn.esprit.utils.AppNavigator;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        AppNavigator.applyStage(
                primaryStage,
                AppNavigator.createScene(loader.load(), getClass()),
                "PinkShield"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}

