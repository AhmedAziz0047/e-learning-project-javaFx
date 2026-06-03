package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Contrôleur du tableau de bord enseignant avec graphiques.
 */
public class TeacherDashboardController implements Initializable {

    @FXML private FlowPane statsPane;
    @FXML private VBox chartsBox;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadingIndicator;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDashboard();
    }

    private void loadDashboard() {
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                JsonObject stats = ApiClient.getTeacherDashboard();
                Platform.runLater(() -> {
                    displayStats(stats);
                    displayCharts(stats);
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statsPane.getChildren().add(new Label("Erreur de chargement du tableau de bord"));
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void displayStats(JsonObject stats) {
        statsPane.getChildren().clear();
        addStatCard("👥 Étudiants", getLong(stats, "totalEtudiants"));
        addStatCard("📚 Cours", getLong(stats, "totalCours"));
        addStatCard("📈 Progression", getDouble(stats, "progressionMoyenne") + "%");
        addStatCard("✅ Complétion", getDouble(stats, "tauxCompletion") + "%");
        addStatCard("🎥 Séances Live", getLong(stats, "totalSeancesLive"));
        addStatCard("📎 Ressources", getLong(stats, "totalRessources"));
    }

    private void displayCharts(JsonObject stats) {
        chartsBox.getChildren().clear();

        // Graphique 1 : Inscrits par cours (BarChart)
        if (stats.has("inscritParCours") && !stats.get("inscritParCours").isJsonNull()) {
            CategoryAxis xAxis1 = new CategoryAxis();
            xAxis1.setLabel("Cours");
            NumberAxis yAxis1 = new NumberAxis();
            yAxis1.setLabel("Nombre d'inscrits");

            BarChart<String, Number> barChart = new BarChart<>(xAxis1, yAxis1);
            barChart.setTitle("Inscrits par Cours");
            barChart.setPrefHeight(300);
            barChart.setLegendVisible(false);

            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            for (Map.Entry<String, JsonElement> entry :
                    stats.getAsJsonObject("inscritParCours").entrySet()) {
                series1.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().getAsNumber()));
            }
            barChart.getData().add(series1);
            chartsBox.getChildren().add(barChart);
        }

        // Graphique 2 : Progression par cours (BarChart horizontal style)
        if (stats.has("progressionParCours") && !stats.get("progressionParCours").isJsonNull()) {
            CategoryAxis xAxis2 = new CategoryAxis();
            xAxis2.setLabel("Cours");
            NumberAxis yAxis2 = new NumberAxis(0, 100, 10);
            yAxis2.setLabel("Progression (%)");

            BarChart<String, Number> progressChart = new BarChart<>(xAxis2, yAxis2);
            progressChart.setTitle("Progression Moyenne par Cours");
            progressChart.setPrefHeight(300);
            progressChart.setLegendVisible(false);

            XYChart.Series<String, Number> series2 = new XYChart.Series<>();
            for (Map.Entry<String, JsonElement> entry :
                    stats.getAsJsonObject("progressionParCours").entrySet()) {
                series2.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue().getAsNumber()));
            }
            progressChart.getData().add(series2);
            chartsBox.getChildren().add(progressChart);
        }
    }

    private void addStatCard(String label, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(170);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(valueLabel, nameLabel);
        statsPane.getChildren().add(card);
    }

    @FXML private void handleBack() { SceneManager.showDashboard(); }

    private String getLong(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? String.valueOf(obj.get(key).getAsLong()) : "0";
    }

    private String getDouble(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ?
                String.format("%.1f", obj.get(key).getAsDouble()) : "0.0";
    }
}
