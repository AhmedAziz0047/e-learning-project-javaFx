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

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur des ressources pédagogiques d'un cours.
 */
public class ResourcesController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private VBox resourcesBox;
    @FXML private Button uploadButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;

    private Long courseId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadingIndicator.setVisible(false);
        if (uploadButton != null) {
            boolean canUpload = SessionManager.isTeacher() || SessionManager.isAdmin();
            uploadButton.setVisible(canUpload);
            uploadButton.setManaged(canUpload);
        }
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
        loadResources();
    }

    private void loadResources() {
        if (courseId == null) return;
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                List<JsonObject> resources = ApiClient.getResourcesByCourse(courseId);
                Platform.runLater(() -> {
                    resourcesBox.getChildren().clear();

                    if (resources.isEmpty()) {
                        resourcesBox.getChildren().add(new Label("Aucune ressource disponible"));
                    } else {
                        for (JsonObject res : resources) {
                            resourcesBox.getChildren().add(createResourceCard(res));
                        }
                    }
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    resourcesBox.getChildren().add(new Label("Erreur de chargement"));
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private HBox createResourceCard(JsonObject res) {
        HBox card = new HBox(10);
        card.getStyleClass().add("resource-card");
        card.setPadding(new Insets(12));

        String type = safeStr(res, "resourceType");
        String icon = switch (type) {
            case "DOCUMENT" -> "📄";
            case "IMAGE" -> "🖼";
            case "VIDEO" -> "🎬";
            default -> "📎";
        };

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        VBox info = new VBox(3);
        Label name = new Label(safeStr(res, "resourceName"));
        name.getStyleClass().add("resource-name");
        Label meta = new Label(type + " | " + formatSize(res) + " | v" + safeStr(res, "version"));
        meta.getStyleClass().add("resource-meta");
        Label uploader = new Label("Par : " + safeStr(res, "uploadedByNom"));
        uploader.getStyleClass().add("resource-uploader");
        info.getChildren().addAll(name, meta, uploader);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button downloadBtn = new Button("⬇ Télécharger");
        downloadBtn.getStyleClass().add("btn-primary");
        downloadBtn.setOnAction(e -> handleDownload(res));

        card.getChildren().addAll(iconLabel, info, spacer, downloadBtn);

        if (SessionManager.isTeacher() || SessionManager.isAdmin()) {
            Button deleteBtn = new Button("🗑");
            deleteBtn.getStyleClass().add("btn-danger-small");
            deleteBtn.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Supprimer cette ressource ?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(btn -> {
                    if (btn == ButtonType.YES) {
                        new Thread(() -> {
                            try {
                                ApiClient.deleteResource(res.get("id").getAsLong());
                                Platform.runLater(this::loadResources);
                            } catch (Exception ex) {
                                Platform.runLater(() -> SceneManager.showError("Erreur", "Suppression impossible"));
                            }
                        }).start();
                    }
                });
            });
            card.getChildren().add(deleteBtn);
        }

        return card;
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.docx", "*.pptx"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.avi", "*.mov")
        );

        File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            loadingIndicator.setVisible(true);
            new Thread(() -> {
                try {
                    ApiClient.uploadResource(courseId, file.toPath());
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        SceneManager.showInfo("Succès", "Ressource uploadée avec succès");
                        loadResources();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        SceneManager.showError("Erreur", "Échec de l'upload");
                    });
                }
            }).start();
        }
    }

    private void handleDownload(JsonObject res) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(safeStr(res, "resourceName"));
        File file = fc.showSaveDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            new Thread(() -> {
                try {
                    byte[] data = ApiClient.downloadResource(res.get("id").getAsLong());
                    Files.write(file.toPath(), data);
                    Platform.runLater(() -> SceneManager.showInfo("Succès", "Fichier téléchargé !"));
                } catch (Exception e) {
                    Platform.runLater(() -> SceneManager.showError("Erreur", "Échec du téléchargement"));
                }
            }).start();
        }
    }

    @FXML private void handleBack() { SceneManager.showCourses(); }

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
