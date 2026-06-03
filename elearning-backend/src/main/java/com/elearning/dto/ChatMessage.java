package com.elearning.dto;

import lombok.*;

/**
 * DTO pour les messages de chat en temps réel (WebSocket).
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChatMessage {

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }

    private MessageType type;
    private String content;
    private String sender;
    private String senderNom;
    private Long sessionId;
    private String timestamp;
}
