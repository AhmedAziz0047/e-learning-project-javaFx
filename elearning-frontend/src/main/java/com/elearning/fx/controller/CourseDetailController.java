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
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du détail d'un cours (ressources, séances, inscriptions).
 */
public class CourseDetailController implements Initializable {

    @FXML private Label courseTitleLabel;
    @FXML private Label courseDescLabel;
    @FXML private Label courseMetaLabel;
    @FXML private VBox resourcesBox;
    @FXML private VBox sessionsBox;
    @FXML private Button uploadButton;
    @FXML private Button addSessionButton;
    @FXML private Button enrollButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;

    private Long courseId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadingIndicator.setVisible(false);

        boolean isTeacherOrAdmin = SessionManager.isTeacher() || SessionManager.isAdmin();
        if (uploadButton != null) {
            uploadButton.setVisible(isTeacherOrAdmin);
            uploadButton.setManaged(isTeacherOrAdmin);
        }
        if (addSessionButton != null) {
            addSessionButton.setVisible(isTeacherOrAdmin);
            addSessionButton.setManaged(isTeacherOrAdmin);
        }
        if (enrollButton != null) {
            enrollButton.setVisible(SessionManager.isStudent());
            enrollButton.setManaged(SessionManager.isStudent());
        }
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
        loadCourseData();
    }

    private void loadCourseData() {
        if (courseId == null) return;
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                JsonObject course = ApiClient.getCourseById(courseId);
                List<JsonObject> resources = ApiClient.getResourcesByCourse(courseId);
                List<JsonObject> sessions = ApiClient.getSessionsByCourse(courseId);

                Platform.runLater(() -> {
                    courseTitleLabel.setText(safeStr(course, "titre"));
                    courseDescLabel.setText(safeStr(course, "description"));
                    courseMetaLabel.setText(
                            "Catégorie : " + safeStr(course, "categorie") +
                            " | Niveau : " + safeStr(course, "niveau") +
                            " | Durée : " + safeStr(course, "dureeHeures") + "h" +
                            " | Enseignant : " + safeStr(course, "enseignantNom"));

                    displayResources(resources);
                    displaySessions(sessions);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    SceneManager.showError("Erreur", "Impossible de charger le cours");
                });
            }
        }).start();
    }

    private void displayResources(List<JsonObject> resources) {
        resourcesBox.getChildren().clear();
        if (resources.isEmpty()) {
            resourcesBox.getChildren().add(new Label("Aucune ressource pour ce cours"));
            return;
        }

        for (JsonObject res : resources) {
            HBox row = new HBox(10);
            row.getStyleClass().add("resource-row");
            row.setPadding(new Insets(8));

            String type = safeStr(res, "resourceType");
            String icon = switch (type) {
                case "DOCUMENT" -> "📄";
                case "IMAGE" -> "🖼";
                case "VIDEO" -> "🎬";
                default -> "📎";
            };

            Label iconLabel = new Label(icon);
            Label nameLabel = new Label(safeStr(res, "resourceName"));
            nameLabel.getStyleClass().add("resource-name");
            Label sizeLabel = new Label(formatSize(res));
            Label versionLabel = new Label("v" + safeStr(res, "version"));
            versionLabel.getStyleClass().add("version-badge");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button downloadBtn = new Button("Télécharger");
            downloadBtn.getStyleClass().add("btn-small");
            downloadBtn.setOnAction(e -> handleDownload(res));

            row.getChildren().addAll(iconLabel, nameLabel, sizeLabel, versionLabel, spacer, downloadBtn);

            if (SessionManager.isTeacher() || SessionManager.isAdmin()) {
                Button deleteBtn = new Button("✕");
                deleteBtn.getStyleClass().add("btn-danger-small");
                deleteBtn.setOnAction(e -> handleDeleteResource(res.get("id").getAsLong()));
                row.getChildren().add(deleteBtn);
            }

            resourcesBox.getChildren().add(row);
        }
    }

    private void displaySessions(List<JsonObject> sessions) {
        sessionsBox.getChildren().clear();
        if (sessions.isEmpty()) {
            sessionsBox.getChildren().add(new Label("Aucune séance programmée"));
            return;
        }

        for (JsonObject session : sessions) {
            HBox row = new HBox(10);
            row.getStyleClass().add("session-row");
            row.setPadding(new Insets(8));

            String status = safeStr(session, "status");
            String statusIcon = switch (status) {
                case "LIVE" -> "🟢";
                case "PLANNED" -> "🟡";
                case "FINISHED" -> "⚫";
                default -> "⬜";
            };

            Label iconLabel = new Label(statusIcon);
            Label titleLabel = new Label(safeStr(session, "title"));
            titleLabel.getStyleClass().add("session-title");
            Label timeLabel = new Label(safeStr(session, "startTime"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(iconLabel, titleLabel, timeLabel, spacer);

            if ("LIVE".equals(status)) {
                if (session.has("meetingLink") && !session.get("meetingLink").isJsonNull()) {
                    Button joinBtn = new Button("Rejoindre la séance");
                    joinBtn.getStyleClass().add("btn-success");
                    joinBtn.setOnAction(e -> {
                        try {
                            Desktop.getDesktop().browse(new URI(session.get("meetingLink").getAsString()));
                        } catch (Exception ex) {
                            SceneManager.showError("Erreur", "Impossible d'ouvrir le lien");
                        }
                    });
                    row.getChildren().add(joinBtn);
                }
                
                Button chatBtn = new Button("💬 Ouvrir le Chat");
                chatBtn.getStyleClass().add("btn-primary");
                chatBtn.setOnAction(e -> SceneManager.showChatWindow(session.get("id").getAsLong()));
                row.getChildren().add(chatBtn);
            }

            if ((SessionManager.isTeacher() || SessionManager.isAdmin()) && "PLANNED".equals(status)) {
                Button startBtn = new Button("Démarrer");
                startBtn.getStyleClass().add("btn-primary");
                startBtn.setOnAction(e -> handleStartSession(session.get("id").getAsLong()));
                row.getChildren().add(startBtn);
            }

            if ((SessionManager.isTeacher() || SessionManager.isAdmin()) && "LIVE".equals(status)) {
                Button endBtn = new Button("Terminer");
                endBtn.getStyleClass().add("btn-danger-small");
                endBtn.setOnAction(e -> handleEndSession(session.get("id").getAsLong()));
                row.getChildren().add(endBtn);
            }

            sessionsBox.getChildren().add(row);
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une ressource");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            loadingIndicator.setVisible(true);
            new Thread(() -> {
                try {
                    ApiClient.uploadResource(courseId, file.toPath());
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        SceneManager.showInfo("Succès", "Ressource uploadée avec succès !");
                        loadCourseData();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        SceneManager.showError("Erreur", "Erreur lors de l'upload : " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void handleDownload(JsonObject res) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la ressource");
        fileChooser.setInitialFileName(safeStr(res, "resourceName"));
        File file = fileChooser.showSaveDialog(SceneManager.getPrimaryStage());

        if (file != null) {
            new Thread(() -> {
                try {
                    byte[] data = ApiClient.downloadResource(res.get("id").getAsLong());
                    Files.write(file.toPath(), data);
                    Platform.runLater(() -> SceneManager.showInfo("Succès", "Fichier téléchargé avec succès !"));
                } catch (Exception e) {
                    Platform.runLater(() -> SceneManager.showError("Erreur", "Erreur lors du téléchargement"));
                }
            }).start();
        }
    }

    private void handleDeleteResource(Long resourceId) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                new Thread(() -> {
                    try {
                        ApiClient.deleteResource(resourceId);
                        Platform.runLater(this::loadCourseData);
                    } catch (Exception e) {
                        Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de supprimer"));
                    }
                }).start();
            }
        });
    }

    private void handleStartSession(Long sessionId) {
        new Thread(() -> {
            try {
                ApiClient.startSession(sessionId);
                Platform.runLater(this::loadCourseData);
            } catch (Exception e) {
                Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de démarrer la séance"));
            }
        }).start();
    }

    private void handleEndSession(Long sessionId) {
        new Thread(() -> {
            try {
                ApiClient.endSession(sessionId);
                Platform.runLater(this::loadCourseData);
            } catch (Exception e) {
                Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de terminer la séance"));
            }
        }).start();
    }

    @FXML
    private void handleEnroll() {
        new Thread(() -> {
            try {
                ApiClient.enroll(courseId);
                Platform.runLater(() -> {
                    enrollButton.setText("✓ Inscrit");
                    enrollButton.setDisable(true);
                    SceneManager.showInfo("Succès", "Inscription effectuée avec succès !");
                });
            } catch (Exception e) {
                Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de s'inscrire"));
            }
        }).start();
    }

    @FXML
    private void handleAddSession() {
        Dialog<JsonObject> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Séance");
        dialog.setHeaderText("Planifier une séance en direct");

        ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(2);
        TextField linkField = new TextField();
        linkField.setPromptText("https://meet.google.com/...");
        TextField dateField = new TextField();
        dateField.setPromptText("2025-01-15 14:00:00");

        grid.add(new Label("Titre :"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description :"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Lien (Meet/Zoom) :"), 0, 2);
        grid.add(linkField, 1, 2);
        grid.add(new Label("Date & Heure :"), 0, 3);
        grid.add(dateField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == createBtn) {
                JsonObject session = new JsonObject();
                session.addProperty("courseId", courseId);
                session.addProperty("title", titleField.getText());
                session.addProperty("description", descField.getText());
                session.addProperty("meetingLink", linkField.getText());
                session.addProperty("startTime", dateField.getText());
                return session;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(session -> {
            new Thread(() -> {
                try {
                    ApiClient.createSession(session);
                    Platform.runLater(this::loadCourseData);
                } catch (Exception e) {
                    Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de créer la séance"));
                }
            }).start();
        });
    }

    @FXML
    private void handleBack() {
        SceneManager.showCourses();
    }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    private String formatSize(JsonObject res) {
        if (!res.has("fileSize") || res.get("fileSize").isJsonNull()) return "";
        long size = res.get("fileSize").getAsLong();
        if (size < 1024) return size + " o";
        if (size < 1048576) return (size / 1024) + " Ko";
        return String.format("%.1f Mo", size / 1048576.0);
    }
}
