package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Real-time Notifications
 * Enables STOMP protocol over WebSocket for bi-directional communication
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable in-memory message broker
    // /topic - for broadcast messages (user subscriptions)
    // /user - for private messages (specific user)
    config.enableSimpleBroker("/topic", "/user");
    
    // Configure the prefix for messages sent from client to server
    config.setApplicationDestinationPrefixes("/app");
    
    // Configure prefix for user-specific messages
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket endpoint URL: ws://localhost:8081/ws
    // Frontend connects to this URL using StompClient
    registry.addEndpoint("/ws")
        .setAllowedOrigins("http://localhost:5173", "https://efsora-frontend-m3ke.onrender.com")
        .withSockJS(); // Fallback for browsers that don't support WebSocket
  }
}
