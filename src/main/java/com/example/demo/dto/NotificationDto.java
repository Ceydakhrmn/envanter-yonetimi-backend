package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for WebSocket Notifications
 * Represents a real-time notification message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
  
  private String id;                    // Unique notification ID
  private String type;                  // notification type: USER_CREATED, ASSET_ADDED, etc.
  private String title;                 // Notification title
  private String message;               // Notification message
  private String icon;                  // Icon name (lucide-react icon)
  private String severity;              // info, success, warning, error
  private Long recipientId;             // Target user ID (if null = broadcast)
  private Long relatedId;               // Related resource ID (user/asset/etc)
  private String actionUrl;             // URL to navigate when clicked
  private Boolean read;                 // Is notification read
  private LocalDateTime timestamp;      // Notification timestamp

  // Notification type constants
  public static final String TYPE_USER_CREATED = "USER_CREATED";
  public static final String TYPE_USER_UPDATED = "USER_UPDATED";
  public static final String TYPE_USER_DELETED = "USER_DELETED";
  public static final String TYPE_ASSET_CREATED = "ASSET_CREATED";
  public static final String TYPE_ASSET_UPDATED = "ASSET_UPDATED";
  public static final String TYPE_ASSET_EXPIRED = "ASSET_EXPIRED";
  public static final String TYPE_ASSET_ASSIGNED = "ASSET_ASSIGNED";
  public static final String TYPE_SYSTEM_ALERT = "SYSTEM_ALERT";
  public static final String TYPE_ACTIVITY_LOG = "ACTIVITY_LOG";
}
