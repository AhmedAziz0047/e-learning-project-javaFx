package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.google.gson.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur des logs d'audit (admin).
 */
public class AuditLogsController implements Initializable {

    @FXML private TableView<AuditEntry> auditTable;
    @FXML private TableColumn<AuditEntry, String> colDate;
    @FXML private TableColumn<AuditEntry, String> colUser;
    @FXML private TableColumn<AuditEntry, String> colAction;
    @FXML private TableColumn<AuditEntry, String> colEntity;
    @FXML private TableColumn<AuditEntry, String> colResult;
    @FXML private TableColumn<AuditEntry, String> colIp;
    @FXML private TableColumn<AuditEntry, String> colDuration;
    @FXML private TextField searchField;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageLabel;
    @FXML private Button backButton;

    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<AuditEntry> entries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colEntity.setCellValueFactory(new PropertyValueFactory<>("entity"));
        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        auditTable.setItems(entries);
        loadPage(0);
    }

    private void loadPage(int page) {
        new Thread(() -> {
            try {
                JsonObject response = ApiClient.getAuditLogs(page, 20);
                Platform.runLater(() -> {
                    entries.clear();
                    if (response.has("content")) {
                        for (JsonElement el : response.getAsJsonArray("content")) {
                            JsonObject log = el.getAsJsonObject();
                            entries.add(new AuditEntry(
                                    safeStr(log, "createdAt"),
                                    safeStr(log, "userNom"),
                                    safeStr(log, "action"),
                                    safeStr(log, "entityType"),
                                    safeStr(log, "result"),
                                    safeStr(log, "ipAddress"),
                                    safeStr(log, "executionTimeMs") + " ms"
                            ));
                        }
                    }

                    currentPage = page;
                    totalPages = response.has("totalPages") ? response.get("totalPages").getAsInt() : 1;
                    pageLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);
                    prevPageButton.setDisable(currentPage <= 0);
                    nextPageButton.setDisable(currentPage >= totalPages - 1);
                });
            } catch (Exception e) {
                Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de charger les logs"));
            }
        }).start();
    }

    @FXML private void handlePrevPage() { loadPage(currentPage - 1); }
    @FXML private void handleNextPage() { loadPage(currentPage + 1); }
    @FXML private void handleBack() { SceneManager.showDashboard(); }

    @FXML
    private void handleSearch() {
        // Recharger avec filtre (fonctionnalité simplifiée — filtrage côté client)
        loadPage(0);
    }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    /**
     * Modèle pour une entrée d'audit dans le TableView.
     */
    public static class AuditEntry {
        private final SimpleStringProperty date;
        private final SimpleStringProperty user;
        private final SimpleStringProperty action;
        private final SimpleStringProperty entity;
        private final SimpleStringProperty result;
        private final SimpleStringProperty ip;
        private final SimpleStringProperty duration;

        public AuditEntry(String date, String user, String action, String entity,
                          String result, String ip, String duration) {
            this.date = new SimpleStringProperty(date);
            this.user = new SimpleStringProperty(user);
            this.action = new SimpleStringProperty(action);
            this.entity = new SimpleStringProperty(entity);
            this.result = new SimpleStringProperty(result);
            this.ip = new SimpleStringProperty(ip);
            this.duration = new SimpleStringProperty(duration);
        }

        public String getDate() { return date.get(); }
        public String getUser() { return user.get(); }
        public String getAction() { return action.get(); }
        public String getEntity() { return entity.get(); }
        public String getResult() { return result.get(); }
        public String getIp() { return ip.get(); }
        public String getDuration() { return duration.get(); }
    }
}
