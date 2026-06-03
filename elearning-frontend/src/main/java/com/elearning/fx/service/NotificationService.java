package com.elearning.fx.service;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service de notifications pour rappeler les événements à venir.
 */
public class NotificationService {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Set<Long> notifiedEvents = new HashSet<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (com.elearning.fx.util.SessionManager.isLoggedIn()) {
                    checkUpcomingEvents();
                }
            } catch (Exception e) {
                // Ignore silent failure
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    private static void checkUpcomingEvents() {
        try {
            List<JsonObject> events = ApiClient.getUpcomingEvents();
            LocalDateTime now = LocalDateTime.now();

            for (JsonObject event : events) {
                Long id = event.get("id").getAsLong();
                if (notifiedEvents.contains(id)) continue;

                String dateStr = event.has("eventDate") && !event.get("eventDate").isJsonNull() ? 
                        event.get("eventDate").getAsString() : null;

                if (dateStr != null) {
                    LocalDateTime eventDate = LocalDateTime.parse(dateStr, formatter);
                    long hoursUntilEvent = ChronoUnit.HOURS.between(now, eventDate);

                    if (hoursUntilEvent > 0 && hoursUntilEvent <= 24) {
                        String title = event.has("title") ? event.get("title").getAsString() : "Événement";
                        String msg = "Rappel : L'événement '" + title + "' commence dans moins de 24h (" + dateStr + ").";
                        
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Rappel d'Événement");
                            alert.setHeaderText("Événement à venir !");
                            alert.setContentText(msg);
                            alert.show();
                        });
                        
                        notifiedEvents.add(id);
                    }
                }
            }
        } catch (Exception e) {
            // Error during polling
        }
    }

    public static void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
