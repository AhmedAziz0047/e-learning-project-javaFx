package com.elearning.fx.service;

import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Client STOMP basique implémenté par-dessus org.java-websocket.
 */
public class StompClient extends WebSocketClient {

    private static final String NULL_CHAR = "\u0000";
    private final Map<String, Consumer<String>> subscriptions = new HashMap<>();
    private Runnable onConnected;
    private final String jwtToken;

    public StompClient(URI serverUri, String jwtToken) {
        super(serverUri);
        this.jwtToken = jwtToken;
    }

    public void setOnConnected(Runnable onConnected) {
        this.onConnected = onConnected;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        // Envoi de la trame CONNECT STOMP
        String connectFrame = "CONNECT\n" +
                "accept-version:1.1,1.0\n" +
                "heart-beat:10000,10000\n";
        if (jwtToken != null) {
            connectFrame += "Authorization:Bearer " + jwtToken + "\n";
        }
        connectFrame += "\n" + NULL_CHAR;
        send(connectFrame);
    }

    @Override
    public void onMessage(String message) {
        if (message.startsWith("CONNECTED")) {
            if (onConnected != null) {
                Platform.runLater(onConnected);
            }
        } else if (message.startsWith("MESSAGE")) {
            // Parser la destination
            String destination = "";
            String[] lines = message.split("\n");
            int bodyStart = 0;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].startsWith("destination:")) {
                    destination = lines[i].substring("destination:".length());
                } else if (lines[i].isEmpty()) {
                    bodyStart = i + 1;
                    break;
                }
            }
            
            // Extraire le corps
            StringBuilder body = new StringBuilder();
            for (int i = bodyStart; i < lines.length; i++) {
                body.append(lines[i]);
                if (i < lines.length - 1) body.append("\n");
            }
            String payload = body.toString().replace(NULL_CHAR, "");

            if (subscriptions.containsKey(destination)) {
                Consumer<String> callback = subscriptions.get(destination);
                Platform.runLater(() -> callback.accept(payload));
            }
        }
    }

    public void subscribe(String destination, Consumer<String> callback) {
        subscriptions.put(destination, callback);
        String id = "sub-" + System.currentTimeMillis();
        String subscribeFrame = "SUBSCRIBE\n" +
                "id:" + id + "\n" +
                "destination:" + destination + "\n" +
                "\n" + NULL_CHAR;
        send(subscribeFrame);
    }

    public void sendMsg(String destination, String payload) {
        String sendFrame = "SEND\n" +
                "destination:" + destination + "\n" +
                "content-type:application/json\n" +
                "content-length:" + payload.length() + "\n" +
                "\n" +
                payload + NULL_CHAR;
        send(sendFrame);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket closed: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
