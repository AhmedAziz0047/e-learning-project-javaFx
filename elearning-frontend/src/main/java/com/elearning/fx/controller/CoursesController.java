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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur de la liste des cours.
 */
public class CoursesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private VBox coursesListBox;
    @FXML private Button addCourseButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadingIndicator.setVisible(false);

        // Bouton Ajouter visible uniquement pour enseignant/admin
        if (addCourseButton != null) {
            addCourseButton.setVisible(SessionManager.isTeacher() || SessionManager.isAdmin());
        }

        loadCourses();
    }

    private void loadCourses() {
        setLoading(true);
        new Thread(() -> {
            try {
                List<JsonObject> courses = ApiClient.getCourses();
                Platform.runLater(() -> {
                    displayCourses(courses);
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    coursesListBox.getChildren().clear();
                    coursesListBox.getChildren().add(new Label("Erreur de chargement des cours"));
                    setLoading(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadCourses();
            return;
        }

        setLoading(true);
        new Thread(() -> {
            try {
                List<JsonObject> courses = ApiClient.searchCourses(query);
                Platform.runLater(() -> {
                    displayCourses(courses);
                    setLoading(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }

    private void displayCourses(List<JsonObject> courses) {
        coursesListBox.getChildren().clear();

        if (courses.isEmpty()) {
            Label empty = new Label("Aucun cours trouvé");
            empty.getStyleClass().add("empty-message");
            coursesListBox.getChildren().add(empty);
            return;
        }

        for (JsonObject course : courses) {
            coursesListBox.getChildren().add(createCourseCard(course));
        }
    }

    private HBox createCourseCard(JsonObject course) {
        HBox card = new HBox(15);
        card.getStyleClass().add("course-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label titre = new Label(safeStr(course, "titre"));
        titre.getStyleClass().add("course-title");

        Label desc = new Label(safeStr(course, "description"));
        desc.getStyleClass().add("course-description");
        desc.setWrapText(true);
        desc.setMaxWidth(500);

        HBox meta = new HBox(15);
        Label categorie = new Label("📂 " + safeStr(course, "categorie"));
        Label niveau = new Label("📊 " + safeStr(course, "niveau"));
        Label inscrits = new Label("👥 " + safeStr(course, "nombreInscrits") + " inscrits");
        Label ressources = new Label("📎 " + safeStr(course, "nombreRessources") + " ressources");
        meta.getChildren().addAll(categorie, niveau, inscrits, ressources);
        meta.getStyleClass().add("course-meta");

        info.getChildren().addAll(titre, desc, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox actions = new VBox(5);
        actions.setAlignment(Pos.CENTER);

        Button viewBtn = new Button("Voir");
        viewBtn.getStyleClass().add("btn-primary");
        viewBtn.setOnAction(e -> {
            Long courseId = course.get("id").getAsLong();
            SceneManager.showCourseDetail(courseId);
        });
        actions.getChildren().add(viewBtn);

        if (SessionManager.isStudent()) {
            Button enrollBtn = new Button("S'inscrire");
            enrollBtn.getStyleClass().add("btn-success");
            enrollBtn.setOnAction(e -> handleEnroll(course.get("id").getAsLong(), enrollBtn));
            actions.getChildren().add(enrollBtn);
        }

        card.getChildren().addAll(info, spacer, actions);
        return card;
    }

    private void handleEnroll(Long courseId, Button btn) {
        btn.setDisable(true);
        new Thread(() -> {
            try {
                ApiClient.enroll(courseId);
                Platform.runLater(() -> {
                    btn.setText("✓ Inscrit");
                    btn.getStyleClass().remove("btn-success");
                    btn.getStyleClass().add("btn-disabled");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    btn.setDisable(false);
                    SceneManager.showError("Erreur", "Impossible de s'inscrire : " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleAddCourse() {
        setLoading(true);
        new Thread(() -> {
            try {
                java.util.List<JsonObject> groups = ApiClient.getGroups();
                Platform.runLater(() -> {
                    setLoading(false);
                    showAddCourseDialog(groups);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    SceneManager.showError("Erreur", "Impossible de charger les groupes : " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAddCourseDialog(java.util.List<JsonObject> groups) {
        Dialog<JsonObject> dialog = new Dialog<>();
        dialog.setTitle("Nouveau Cours");
        dialog.setHeaderText(null);

        ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        // Même GridPane que le modal Nouvel Événement
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        // Champs identiques au modal Événement
        TextField titreField     = new TextField();
        TextField categorieField = new TextField();
        TextField niveauField    = new TextField();
        TextField dureeField     = new TextField();
        dureeField.setPromptText("Durée (heures)");

        TextArea descField = new TextArea();
        descField.setPrefRowCount(2);
        descField.setWrapText(true);

        // Groupes — cases à cocher comme dans Événement
        VBox groupsBox = new VBox(5);
        java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();
        if (groups.isEmpty()) {
            groupsBox.getChildren().add(new Label("Aucun groupe disponible"));
        } else {
            for (JsonObject group : groups) {
                CheckBox cb = new CheckBox(group.get("name").getAsString());
                cb.setUserData(group.get("id").getAsLong());
                checkBoxes.add(cb);
                groupsBox.getChildren().add(cb);
            }
        }

        // Grille : label à gauche, champ à droite (même structure que Événement)
        grid.add(new Label("Titre :"),        0, 0); grid.add(titreField,     1, 0);
        grid.add(new Label("Description :"),  0, 1); grid.add(descField,      1, 1);
        grid.add(new Label("Catégorie :"),    0, 2); grid.add(categorieField, 1, 2);
        grid.add(new Label("Niveau :"),       0, 3); grid.add(niveauField,    1, 3);
        grid.add(new Label("Durée :"),        0, 4); grid.add(dureeField,     1, 4);
        grid.add(new Label("Groupes :"),      0, 5); grid.add(groupsBox,      1, 5);

        // Pas de stylesheet personnalisé → look natif Windows comme Événement
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == createBtn) {
                if (titreField.getText().trim().isEmpty()) return null;
                JsonObject course = new JsonObject();
                course.addProperty("titre",       titreField.getText().trim());
                course.addProperty("description", descField.getText().trim());
                course.addProperty("categorie",   categorieField.getText().trim());
                course.addProperty("niveau",      niveauField.getText().trim());
                try {
                    course.addProperty("dureeHeures",
                            Integer.parseInt(dureeField.getText().trim()));
                } catch (NumberFormatException ignored) {}

                com.google.gson.JsonArray targetGroupIds = new com.google.gson.JsonArray();
                for (CheckBox cb : checkBoxes) {
                    if (cb.isSelected()) targetGroupIds.add((Long) cb.getUserData());
                }
                course.add("targetGroupIds", targetGroupIds);
                return course;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(course -> {
            new Thread(() -> {
                try {
                    ApiClient.createCourse(course);
                    Platform.runLater(this::loadCourses);
                } catch (Exception e) {
                    Platform.runLater(() -> SceneManager.showError("Erreur",
                            "Impossible de créer le cours : " + e.getMessage()));
                }
            }).start();
        });
    }

    @FXML
    private void handleBack() {
        SceneManager.showDashboard();
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
    }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
