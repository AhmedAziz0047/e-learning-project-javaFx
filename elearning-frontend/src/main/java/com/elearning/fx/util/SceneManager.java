package com.elearning.fx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Gestionnaire de scènes — charge et affiche les vues FXML.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static Scene mainScene;

    public static void initialize(Stage stage) {
        primaryStage = stage;
    }

    public static void showLogin() {
        loadScene("/fxml/login.fxml", "Connexion — E-Learning");
    }

    public static void showRegister() {
        loadScene("/fxml/register.fxml", "Inscription — E-Learning");
    }

    public static void showDashboard() {
        loadScene("/fxml/dashboard.fxml", "Tableau de bord — E-Learning");
    }

    public static void showCourses() {
        loadScene("/fxml/courses.fxml", "Cours — E-Learning");
    }

    public static void showCourseDetail(Long courseId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/course_detail.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof com.elearning.fx.controller.CourseDetailController cdc) {
                cdc.setCourseId(courseId);
            }
            mainScene = new Scene(root);
            mainScene.getStylesheets().add(SceneManager.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("Détail du cours — E-Learning");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la vue du cours.");
        }
    }

    public static void showCalendar() {
        loadScene("/fxml/calendar.fxml", "Calendrier — E-Learning");
    }

    public static void showLiveSessions() {
        loadScene("/fxml/live_sessions.fxml", "Séances en direct — E-Learning");
    }

    public static void showResources(Long courseId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/resources.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof com.elearning.fx.controller.ResourcesController rc) {
                rc.setCourseId(courseId);
            }
            mainScene = new Scene(root);
            mainScene.getStylesheets().add(SceneManager.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("Ressources — E-Learning");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger les ressources.");
        }
    }

    public static void showTeacherDashboard() {
        loadScene("/fxml/teacher_dashboard.fxml", "Tableau de bord enseignant — E-Learning");
    }

    public static void showAuditLogs() {
        loadScene("/fxml/audit_logs.fxml", "Logs d'audit — E-Learning");
    }

    public static void showChatWindow(Long sessionId) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/fxml/chat_window.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof com.elearning.fx.controller.ChatWindowController cwc) {
                cwc.initSession(sessionId);
                
                Stage chatStage = new Stage();
                chatStage.setTitle("Chat de la séance");
                Scene scene = new Scene(root);
                scene.getStylesheets().add(SceneManager.class.getResource("/css/style.css").toExternalForm());
                chatStage.setScene(scene);
                
                chatStage.setOnCloseRequest(e -> cwc.closeConnection());
                
                chatStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le chat.");
        }
    }

    private static void loadScene(String fxmlPath, String title) {
        try {
            URL resource = SceneManager.class.getResource(fxmlPath);
            if (resource == null) {
                showError("Erreur", "Vue introuvable : " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            mainScene = new Scene(root);
            URL cssUrl = SceneManager.class.getResource("/css/style.css");
            if (cssUrl != null) {
                mainScene.getStylesheets().add(cssUrl.toExternalForm());
            }
            primaryStage.setScene(mainScene);
            primaryStage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger : " + fxmlPath);
        }
    }

    public static void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
