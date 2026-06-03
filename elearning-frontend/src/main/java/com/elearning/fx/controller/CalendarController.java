package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.util.SceneManager;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Contrôleur du calendrier académique.
 */
public class CalendarController implements Initializable {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private VBox upcomingEventsBox;
    @FXML private Button backButton;
    @FXML private Button addEventButton;

    private YearMonth currentMonth;
    private List<JsonObject> currentEvents = new ArrayList<>();

    private static final String[] MONTH_NAMES = {
        "", "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    private static final String[] DAY_NAMES = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentMonth = YearMonth.now();

        if (addEventButton != null) {
            boolean canAdd = SessionManager.isTeacher() || SessionManager.isAdmin();
            addEventButton.setVisible(canAdd);
            addEventButton.setManaged(canAdd);
        }

        updateCalendar();
        loadUpcomingEvents();
    }

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        updateCalendar();
    }

    private void updateCalendar() {
        monthYearLabel.setText(MONTH_NAMES[currentMonth.getMonthValue()] + " " + currentMonth.getYear());

        new Thread(() -> {
            try {
                List<JsonObject> events = ApiClient.getEventsByMonth(
                        currentMonth.getYear(), currentMonth.getMonthValue());
                currentEvents = events;
                Platform.runLater(() -> renderCalendar(events));
            } catch (Exception e) {
                Platform.runLater(() -> renderCalendar(List.of()));
            }
        }).start();
    }

    private void renderCalendar(List<JsonObject> events) {
        calendarGrid.getChildren().clear();

        // En-têtes des jours
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(DAY_NAMES[i]);
            dayLabel.getStyleClass().add("calendar-day-header");
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Lundi
        int daysInMonth = currentMonth.lengthOfMonth();

        // Map des événements par jour
        Map<Integer, List<JsonObject>> eventsByDay = new HashMap<>();
        for (JsonObject event : events) {
            try {
                String dateStr = event.has("eventDate") ? event.get("eventDate").getAsString() : "";
                if (dateStr.length() >= 10) {
                    int day = Integer.parseInt(dateStr.substring(8, 10));
                    eventsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(event);
                }
            } catch (Exception ignored) {}
        }

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox cell = new VBox(2);
            cell.getStyleClass().add("calendar-cell");
            cell.setPadding(new Insets(4));
            cell.setPrefHeight(80);
            cell.setPrefWidth(120);

            Label dayNum = new Label(String.valueOf(day));
            dayNum.getStyleClass().add("calendar-day-number");

            if (day == LocalDate.now().getDayOfMonth() &&
                currentMonth.equals(YearMonth.now())) {
                dayNum.getStyleClass().add("calendar-today");
            }

            cell.getChildren().add(dayNum);

            if (eventsByDay.containsKey(day)) {
                for (JsonObject evt : eventsByDay.get(day)) {
                    Label eventLabel = new Label(safeStr(evt, "title"));
                    eventLabel.getStyleClass().add("calendar-event");
                    String type = safeStr(evt, "eventType");
                    eventLabel.getStyleClass().add("event-" + type.toLowerCase());
                    eventLabel.setMaxWidth(Double.MAX_VALUE);
                    cell.getChildren().add(eventLabel);
                }
            }

            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private void loadUpcomingEvents() {
        new Thread(() -> {
            try {
                List<JsonObject> events = ApiClient.getUpcomingEvents();
                Platform.runLater(() -> {
                    upcomingEventsBox.getChildren().clear();
                    if (events.isEmpty()) {
                        upcomingEventsBox.getChildren().add(new Label("Aucun événement à venir"));
                        return;
                    }
                    int count = 0;
                    for (JsonObject event : events) {
                        if (count >= 10) break;
                        HBox row = new HBox(10);
                        row.getStyleClass().add("upcoming-event-row");
                        row.setPadding(new Insets(8));

                        Label typeIcon = new Label(getEventIcon(safeStr(event, "eventType")));
                        Label title = new Label(safeStr(event, "title"));
                        title.getStyleClass().add("event-title");
                        Label date = new Label(safeStr(event, "eventDate"));
                        date.getStyleClass().add("event-date");

                        row.getChildren().addAll(typeIcon, title, date);
                        upcomingEventsBox.getChildren().add(row);
                        count++;
                    }
                });
            } catch (Exception ignored) {}
        }).start();
    }

    @FXML
    private void handleAddEvent() {
        Dialog<JsonObject> dialog = new Dialog<>();
        dialog.setTitle("Nouvel Événement");
        ButtonType createBtn = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        TextArea descField = new TextArea(); descField.setPrefRowCount(2);
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("COURS", "SEANCE_LIVE", "EXAMEN", "PROJET", "CERTIFICATION");
        typeBox.setValue("COURS");
        TextField dateField = new TextField();
        dateField.setPromptText("2025-01-15 14:00:00");
        TextField durationField = new TextField();
        durationField.setPromptText("60");

        grid.add(new Label("Titre :"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Description :"), 0, 1); grid.add(descField, 1, 1);
        grid.add(new Label("Type :"), 0, 2); grid.add(typeBox, 1, 2);
        grid.add(new Label("Date & Heure :"), 0, 3); grid.add(dateField, 1, 3);
        grid.add(new Label("Durée (min) :"), 0, 4); grid.add(durationField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == createBtn) {
                JsonObject event = new JsonObject();
                event.addProperty("title", titleField.getText());
                event.addProperty("description", descField.getText());
                event.addProperty("eventType", typeBox.getValue());
                event.addProperty("eventDate", dateField.getText());
                try { event.addProperty("durationMinutes", Integer.parseInt(durationField.getText())); }
                catch (Exception ignored) {}
                return event;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(event -> {
            new Thread(() -> {
                try {
                    ApiClient.createEvent(event);
                    Platform.runLater(() -> { updateCalendar(); loadUpcomingEvents(); });
                } catch (Exception e) {
                    Platform.runLater(() -> SceneManager.showError("Erreur", "Impossible de créer l'événement"));
                }
            }).start();
        });
    }

    @FXML private void handleBack() { SceneManager.showDashboard(); }

    private String getEventIcon(String type) {
        return switch (type) {
            case "COURS" -> "📚";
            case "SEANCE_LIVE" -> "📹";
            case "EXAMEN" -> "📝";
            case "PROJET" -> "📂";
            case "CERTIFICATION" -> "🏆";
            default -> "📌";
        };
    }

    private String safeStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
