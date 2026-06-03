package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur des séances en direct.
 */
public class LiveSessionsController implements Initializable {

    @FXML private VBox liveSessionsBox;
    @FXML private VBox upcomingSessionsBox;
    @FXML private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadSessions();
    }

    private void loadSessions() {
        new Thread(() -> {
            try {
                List<JsonObject> live = ApiClient.getLiveSessions();
                List<JsonObject> upcoming = ApiClient.getUpcomingSessions();

                Platform.runLater(() -> {
                    displaySessions(liveSessionsBox, live, "Aucune séance en cours");
                    displaySessions(upcomingSessionsBox, upcoming, "Aucune séance planifiée");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    liveSessionsBox.getChildren().add(new Label("Erreur de chargement"));
                });
            }
        }).start();
    }

    private void displaySessions(VBox container, List<JsonObject> sessions, String emptyMsg) {
        container.getChildren().clear();
        if (sessions.isEmpty()) {
            container.getChildren().add(new Label(emptyMsg));
            return;
        }

        for (JsonObject session : sessions) {
            HBox card = new HBox(15);
            card.getStyleClass().add("session-card");
            card.setPadding(new Insets(12));

            String status = safeStr(session, "status");
            String icon = "LIVE".equals(status) ? "🟢" : "🟡";

            VBox info = new VBox(5);
            Label title = new Label(icon + " " + safeStr(session, "title"));
            title.getStyleClass().add("session-title");
            Label course = new Label("Cours : " + safeStr(session, "courseTitre"));
            Label teacher = new Label("Enseignant : " + safeStr(session, "teacherNom"));
            Label time = new Label("Début : " + safeStr(session, "startTime"));
            info.getChildren().addAll(title, course, teacher, time);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            VBox actions = new VBox(5);
            actions.setAlignment(javafx.geometry.Pos.CENTER);

            if ("LIVE".equals(status)) {
                if (session.has("meetingLink") && !session.get("meetingLink").isJsonNull()) {
                    Button joinBtn = new Button("🎥 Rejoindre la séance");
                    joinBtn.getStyleClass().add("btn-success");
                    joinBtn.setOnAction(e -> {
                        try {
                            Desktop.getDesktop().browse(new URI(session.get("meetingLink").getAsString()));
                        } catch (Exception ex) {
                            SceneManager.showError("Erreur", "Impossible d'ouvrir le lien");
                        }
                    });
                    actions.getChildren().add(joinBtn);
                }
                
                Button chatBtn = new Button("💬 Ouvrir le Chat");
                chatBtn.getStyleClass().add("btn-primary");
                chatBtn.setOnAction(e -> SceneManager.showChatWindow(session.get("id").getAsLong()));
                actions.getChildren().add(chatBtn);
            }

            if ((SessionManager.isTeacher() || SessionManager.isAdmin()) && "PLANNED".equals(status)) {
                Button startBtn = new Button("▶ Démarrer");
                startBtn.getStyleClass().add("btn-primary");
                startBtn.setOnAction(e -> {
                    new Thread(() -> {
                        try {
                            ApiClient.startSession(session.get("id").getAsLong());
                            Platform.runLater(this::loadSessions);
                        } catch (Exception ex) {
                            Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de démarrer"));
                        }
                    }).start();
                });
                actions.getChildren().add(startBtn);
            }

            if ((SessionManager.isTeacher() || SessionManager.isAdmin()) && "LIVE".equals(status)) {
                Button endBtn = new Button("⏹ Terminer");
                endBtn.getStyleClass().add("btn-danger-small");
                endBtn.setOnAction(e -> {
                    new Thread(() -> {
                        try {
                            ApiClient.endSession(session.get("id").getAsLong());
                            Platform.runLater(this::loadSessions);
                        } catch (Exception ex) {
                            Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de terminer"));
                        }
                    }).start();
                });
                actions.getChildren().add(endBtn);
            }

            card.getChildren().addAll(info, spacer, actions);
            container.getChildren().add(card);
        }
    }

    @FXML private void handleBack() { SceneManager.showDashboard(); }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
