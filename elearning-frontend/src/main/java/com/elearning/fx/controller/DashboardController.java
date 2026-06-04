package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du tableau de bord principal.
 */
public class DashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private VBox contentArea;
    @FXML private Button coursesButton;
    @FXML private Button calendarButton;
    @FXML private Button liveButton;
    @FXML private Button dashboardTeacherButton;
    @FXML private Button auditButton;
    @FXML private Button logoutButton;
    @FXML private VBox sideMenu;
    @FXML private FlowPane statsPane;
    @FXML private VBox recentCoursesBox;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        welcomeLabel.setText("Bienvenue, " + SessionManager.getFullName() + " !");

        String roleDisplay = switch (SessionManager.getRole()) {
            case "ROLE_ADMIN" -> "Administrateur";
            case "ROLE_TEACHER" -> "Enseignant";
            case "ROLE_STUDENT" -> "Étudiant";
            default -> SessionManager.getRole();
        };
        roleLabel.setText(roleDisplay);

        // Afficher/masquer les menus selon le rôle
        if (dashboardTeacherButton != null) {
            dashboardTeacherButton.setVisible(SessionManager.isTeacher() || SessionManager.isAdmin());
            dashboardTeacherButton.setManaged(SessionManager.isTeacher() || SessionManager.isAdmin());
        }
        if (auditButton != null) {
            auditButton.setVisible(SessionManager.isAdmin());
            auditButton.setManaged(SessionManager.isAdmin());
        }

        if (SessionManager.isStudent()) {
            if ("GRADUATED".equals(SessionManager.getStudyLevel())) {
                Button diplomaBtn = new Button("🎓 Mon Diplôme");
                diplomaBtn.getStyleClass().add("nav-btn");
                diplomaBtn.setOnAction(e -> handleDownloadDiploma());
                sideMenu.getChildren().add(sideMenu.getChildren().size() - 1, diplomaBtn);
                
                if (liveButton != null) {
                    liveButton.setVisible(false);
                    liveButton.setManaged(false);
                }
            } else {
                Button examBtn = new Button("📝 Passer l'examen");
                examBtn.getStyleClass().add("nav-btn");
                examBtn.setOnAction(e -> handlePassExam());
                sideMenu.getChildren().add(sideMenu.getChildren().size() - 1, examBtn);
            }
        }

        loadDashboardData();
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                // Charger les cours récents
                List<JsonObject> courses = ApiClient.getCourses();

                Platform.runLater(() -> {
                    recentCoursesBox.getChildren().clear();

                    if (courses.isEmpty()) {
                        recentCoursesBox.getChildren().add(new Label("Aucun cours disponible"));
                        return;
                    }

                    int count = 0;
                    for (JsonObject course : courses) {
                        if (count >= 6) break;
                        recentCoursesBox.getChildren().add(createCourseCard(course));
                        count++;
                    }
                });

                // Charger les stats si enseignant/admin
                if (SessionManager.isTeacher() || SessionManager.isAdmin()) {
                    try {
                        JsonObject stats = ApiClient.getTeacherDashboard();
                        Platform.runLater(() -> {
                            if (statsPane != null) {
                                statsPane.getChildren().clear();
                                addStatCard(statsPane, "Étudiants", getJsonLong(stats, "totalEtudiants"));
                                addStatCard(statsPane, "Cours", getJsonLong(stats, "totalCours"));
                                addStatCard(statsPane, "Séances Live", getJsonLong(stats, "totalSeancesLive"));
                                addStatCard(statsPane, "Ressources", getJsonLong(stats, "totalRessources"));
                            }
                        });
                    } catch (Exception ignored) {}
                }

                // Charger les inscriptions si étudiant
                if (SessionManager.isStudent()) {
                    List<JsonObject> enrollments = ApiClient.getMyEnrollments();
                    Platform.runLater(() -> {
                        if (statsPane != null) {
                            statsPane.getChildren().clear();
                            addStatCard(statsPane, "Mes Cours", String.valueOf(enrollments.size()));

                            long completed = enrollments.stream()
                                    .filter(e -> "COMPLETED".equals(getJsonString(e, "statut")))
                                    .count();
                            addStatCard(statsPane, "Terminés", String.valueOf(completed));

                            long active = enrollments.stream()
                                    .filter(e -> "ACTIVE".equals(getJsonString(e, "statut")))
                                    .count();
                            addStatCard(statsPane, "En Cours", String.valueOf(active));
                        }
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() ->
                        recentCoursesBox.getChildren().add(new Label("Erreur de chargement des données")));
            }
        }).start();
    }

    private HBox createCourseCard(JsonObject course) {
        HBox card = new HBox(15);
        card.getStyleClass().add("course-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label titre = new Label(getJsonString(course, "titre"));
        titre.getStyleClass().add("course-title");

        Label categorie = new Label(getJsonString(course, "categorie"));
        categorie.getStyleClass().add("course-category");

        Label inscrits = new Label(getJsonLong(course, "nombreInscrits") + " inscrits");
        inscrits.getStyleClass().add("course-stats");

        info.getChildren().addAll(titre, categorie, inscrits);

        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setOnAction(e -> {
            Long courseId = course.get("id").getAsLong();
            SceneManager.showCourseDetail(courseId);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(info, spacer, viewBtn);
        return card;
    }

    private void addStatCard(FlowPane pane, String label, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(150);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(valueLabel, nameLabel);
        pane.getChildren().add(card);
    }

    @FXML private void handleCourses() { SceneManager.showCourses(); }
    @FXML private void handleCalendar() { SceneManager.showCalendar(); }
    @FXML private void handleLive() { SceneManager.showLiveSessions(); }
    @FXML private void handleTeacherDashboard() { SceneManager.showTeacherDashboard(); }
    @FXML private void handleAudit() { SceneManager.showAuditLogs(); }

    private void handlePassExam() {
        TextInputDialog dialog = new TextInputDialog("75");
        dialog.setTitle("Examen de passage");
        dialog.setHeaderText("Simulation d'examen (Entrez un score en %)");
        dialog.setContentText("Score :");
        dialog.showAndWait().ifPresent(scoreStr -> {
            try {
                double score = Double.parseDouble(scoreStr);
                JsonObject res = ApiClient.submitExam(score);
                String msg = res.has("message") ? res.get("message").getAsString() : "Résultat enregistré";
                SceneManager.showError("Résultat de l'examen", msg);
                // Relog for session update
                SceneManager.showLogin();
            } catch (Exception e) {
                SceneManager.showError("Erreur", "Impossible de soumettre l'examen : " + e.getMessage());
            }
        });
    }

    private void handleDownloadDiploma() {
        new Thread(() -> {
            try {
                byte[] pdfData = ApiClient.downloadDiploma();
                java.io.File file = new java.io.File(System.getProperty("user.home") + "/Desktop/diplome.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(pdfData);
                }
                Platform.runLater(() -> SceneManager.showError("Succès", "Diplôme téléchargé sur le bureau : " + file.getAbsolutePath()));
            } catch (Exception e) {
                Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de télécharger le diplôme : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        SceneManager.showLogin();
    }

    private String getJsonString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private String getJsonLong(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? String.valueOf(obj.get(key).getAsLong()) : "0";
    }
}
