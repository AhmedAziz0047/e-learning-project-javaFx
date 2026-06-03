package com.elearning.fx.controller;

import com.elearning.fx.service.ApiClient;
import com.elearning.fx.service.StompClient;
import com.elearning.fx.util.SessionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur de la fenêtre de chat STOMP pour une séance en direct.
 */
public class ChatWindowController implements Initializable {

    @FXML private VBox messagesBox;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Label statusLabel;

    private StompClient stompClient;
    private Long sessionId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messagesBox.heightProperty().addListener((observable, oldValue, newValue) -> 
            scrollPane.setVvalue(1.0));
    }

    public void initSession(Long sessionId) {
        this.sessionId = sessionId;
        connectToChat();
    }

    private void connectToChat() {
        statusLabel.setText("Connexion en cours...");
        try {
            URI uri = new URI("ws://localhost:8080/ws");
            stompClient = new StompClient(uri, SessionManager.getToken());
            
            stompClient.setOnConnected(() -> {
                statusLabel.setText("Connecté");
                statusLabel.setStyle("-fx-text-fill: #00C853;");
                
                // Souscription au topic de la séance
                stompClient.subscribe("/topic/session/" + sessionId, this::onMessageReceived);
                
                // Envoi d'un message JOIN
                JsonObject joinMsg = new JsonObject();
                joinMsg.addProperty("sender", SessionManager.getEmail());
                joinMsg.addProperty("senderNom", SessionManager.getFullName());
                joinMsg.addProperty("type", "JOIN");
                stompClient.sendMsg("/app/chat.join/" + sessionId, joinMsg.toString());
            });
            
            stompClient.connect();
        } catch (Exception e) {
            statusLabel.setText("Erreur de connexion");
            statusLabel.setStyle("-fx-text-fill: #FF5252;");
        }
    }

    private void onMessageReceived(String payload) {
        try {
            JsonObject msg = ApiClient.getGson().fromJson(payload, JsonObject.class);
            String type = msg.has("type") && !msg.get("type").isJsonNull() ? msg.get("type").getAsString() : "CHAT";
            String senderNom = msg.has("senderNom") && !msg.get("senderNom").isJsonNull() ? msg.get("senderNom").getAsString() : "Inconnu";
            String content = msg.has("content") && !msg.get("content").isJsonNull() ? msg.get("content").getAsString() : "";
            String time = msg.has("timestamp") && !msg.get("timestamp").isJsonNull() ? msg.get("timestamp").getAsString() : "";

            Label msgLabel = new Label();
            msgLabel.setWrapText(true);
            msgLabel.setMaxWidth(400);

            if ("JOIN".equals(type)) {
                msgLabel.setText("➤ " + content);
                msgLabel.setStyle("-fx-text-fill: #00D2FF; -fx-font-style: italic; -fx-padding: 5;");
            } else if ("LEAVE".equals(type)) {
                msgLabel.setText("➤ " + content);
                msgLabel.setStyle("-fx-text-fill: #FFB300; -fx-font-style: italic; -fx-padding: 5;");
            } else {
                msgLabel.setText("[" + time + "] " + senderNom + " : " + content);
                msgLabel.setStyle("-fx-background-color: #1E2A4A; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 8;");
                
                // Si c'est mon message, on l'aligne différemment (facultatif)
                if (SessionManager.getEmail().equals(msg.has("sender") ? msg.get("sender").getAsString() : "")) {
                    msgLabel.setStyle("-fx-background-color: #6C63FF; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 8;");
                }
            }

            Platform.runLater(() -> messagesBox.getChildren().add(msgLabel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || stompClient == null || !stompClient.isOpen()) return;

        JsonObject msg = new JsonObject();
        msg.addProperty("sender", SessionManager.getEmail());
        msg.addProperty("senderNom", SessionManager.getFullName());
        msg.addProperty("content", text);
        msg.addProperty("type", "CHAT");

        stompClient.sendMsg("/app/chat.send/" + sessionId, msg.toString());
        messageField.clear();
    }

    public void closeConnection() {
        if (stompClient != null && stompClient.isOpen()) {
            JsonObject leaveMsg = new JsonObject();
            leaveMsg.addProperty("sender", SessionManager.getEmail());
            leaveMsg.addProperty("senderNom", SessionManager.getFullName());
            leaveMsg.addProperty("type", "LEAVE");
            stompClient.sendMsg("/app/chat.leave/" + sessionId, leaveMsg.toString());
            stompClient.close();
        }
    }
}
