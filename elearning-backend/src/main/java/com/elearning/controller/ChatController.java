package com.elearning.controller;

import com.elearning.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contrôleur WebSocket pour le chat en direct pendant les séances.
 */
@Controller
@Slf4j
public class ChatController {

    @MessageMapping("/chat.send/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage sendMessage(@DestinationVariable Long sessionId,
                                    @Payload ChatMessage message) {
        message.setSessionId(sessionId);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        return message;
    }

    @MessageMapping("/chat.join/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage joinSession(@DestinationVariable Long sessionId,
                                    @Payload ChatMessage message,
                                    SimpMessageHeaderAccessor headerAccessor) {
        // Stocker le nom de l'utilisateur dans la session WebSocket
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        message.setType(ChatMessage.MessageType.JOIN);
        message.setSessionId(sessionId);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        message.setContent(message.getSenderNom() + " a rejoint la séance");
        return message;
    }

    @MessageMapping("/chat.leave/{sessionId}")
    @SendTo("/topic/session/{sessionId}")
    public ChatMessage leaveSession(@DestinationVariable Long sessionId,
                                     @Payload ChatMessage message) {
        message.setType(ChatMessage.MessageType.LEAVE);
        message.setSessionId(sessionId);
        message.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        message.setContent(message.getSenderNom() + " a quitté la séance");
        return message;
    }
}
