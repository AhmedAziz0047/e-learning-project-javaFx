package com.elearning.fx;

import com.elearning.fx.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application JavaFX E-Learning.
 */
public class ElearningFxApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Plateforme E-Learning");
        stage.setMinWidth(1200);
        stage.setMinHeight(750);

        SceneManager.initialize(stage);
        SceneManager.showLogin();
        
        com.elearning.fx.service.NotificationService.start();

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        com.elearning.fx.service.NotificationService.stop();
        super.stop();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
