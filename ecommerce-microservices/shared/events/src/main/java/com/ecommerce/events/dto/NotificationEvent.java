package com.ecommerce.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    
    private String eventId;
    private String eventType;
    private Long userId;
    private String userEmail;
    private NotificationType type;
    private NotificationCategory category;
    private String title;
    private String message;
    private String content;
    private String recipient;
    private String sender;
    private String templateName;
    private Map<String, Object> templateData;
    private String referenceType;
    private Long referenceId;
    private NotificationPriority priority;
    private NotificationStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;

    public enum NotificationType {
        EMAIL, SMS, PUSH, WEBSOCKET
    }

    public enum NotificationCategory {
        ORDER, PAYMENT, ACCOUNT, MARKETING, SYSTEM
    }

    public enum NotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum NotificationStatus {
        PENDING, SENT, DELIVERED, FAILED, CANCELLED
    }
}
