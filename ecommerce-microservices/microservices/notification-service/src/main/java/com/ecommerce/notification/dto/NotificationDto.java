package com.ecommerce.notification.dto;

import com.ecommerce.notification.entity.Notification;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private NotificationType type;
    private String title;
    private String message;
    private NotificationStatus status;
    private String referenceType;
    private Long referenceId;
    private String sentVia;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateNotificationRequest {
        @NotNull(message = "User ID is required")
        private Long userId;
        
        private String userEmail;
        
        @NotNull(message = "Type is required")
        private NotificationType type;
        
        @NotBlank(message = "Title is required")
        private String title;
        
        @NotBlank(message = "Message is required")
        private String message;
        
        private String referenceType;
        private Long referenceId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSearchRequest {
        private Long userId;
        private NotificationType type;
        private NotificationStatus status;
        private String referenceType;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortDirection;
    }

    public enum NotificationType {
        ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, PAYMENT_SUCCESS, PAYMENT_FAILED,
        LOW_STOCK_ALERT, OUT_OF_STOCK_ALERT, PASSWORD_RESET, EMAIL_VERIFICATION,
        PRODUCT_REVIEW_REQUEST, PROMOTIONAL, SYSTEM_ANNOUNCEMENT
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, READ
    }
}
