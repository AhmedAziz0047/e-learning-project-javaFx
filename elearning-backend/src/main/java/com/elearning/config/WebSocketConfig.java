package com.elearning.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuration WebSocket avec STOMP pour le chat en direct.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Préfixe pour les messages sortants (serveur → client)
        config.enableSimpleBroker("/topic");
        // Préfixe pour les messages entrants (client → serveur)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée WebSocket (Raw)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
                
        // Point d'entrée WebSocket (SockJS pour navigateurs)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
