package com.example.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import com.example.demo.dto.NotificationDto;
import java.time.LocalDateTime;

/**
 * WebSocket Message Handler
 * Processes incoming WebSocket messages and sends notifications
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Broadcast notification to all connected users
   * Client sends to: /app/notify/broadcast
   * Server sends to: /topic/notifications
   */
  @MessageMapping("/notify/broadcast")
  @SendTo("/topic/notifications")
  public NotificationDto broadcastNotification(NotificationDto notification) {
    notification.setTimestamp(LocalDateTime.now());
    return notification;
  }

  /**
   * Send notification to specific user
   * Client sends to: /app/notify/user/{userId}
   */
  @MessageMapping("/notify/user/{userId}")
  public void sendUserNotification(NotificationDto notification) {
    notification.setTimestamp(LocalDateTime.now());
    // Send to specific user's private queue
    messagingTemplate.convertAndSendToUser(
        notification.getRecipientId().toString(),
        "/queue/notifications",
        notification
    );
  }

  /**
   * Health check for WebSocket connection
   * Client sends to: /app/ping
   * Server responds with pong
   */
  @MessageMapping("/ping")
  @SendTo("/user/queue/pong")
  public String ping() {
    return "pong";
  }
}
