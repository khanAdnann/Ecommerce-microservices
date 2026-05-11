package com.ecommerce.notification.service;

import com.ecommerce.events.dto.NotificationEvent;
import com.ecommerce.notification.dto.NotificationDto;
import com.ecommerce.notification.entity.Notification;
import com.ecommerce.notification.exception.NotificationException;
import com.ecommerce.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "notification-events", groupId = "notification-service-group")
    public void handleNotificationEvent(String notificationEventJson) {
        try {
            NotificationEvent event = objectMapper.readValue(notificationEventJson, NotificationEvent.class);
            log.info("Received notification event: {}", event.getEventType());

            // Create notification based on event type
            Notification notification = createNotificationFromEvent(event);
            
            // Save notification
            Notification savedNotification = notificationRepository.save(notification);

            // Send notification
            sendNotification(savedNotification);

        } catch (JsonProcessingException e) {
            log.error("Error parsing notification event", e);
        } catch (Exception e) {
            log.error("Error processing notification event", e);
        }
    }

    public NotificationDto createNotification(NotificationDto.CreateNotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .type(Notification.NotificationType.valueOf(request.getType().name()))
                .title(request.getTitle())
                .message(request.getMessage())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .status(Notification.NotificationStatus.PENDING)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Send notification
        sendNotification(savedNotification);

        return convertToDto(savedNotification);
    }

    public Page<NotificationDto> getUserNotifications(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    public Page<NotificationDto> getAllNotifications(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return notificationRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public NotificationDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException("Notification not found: " + notificationId));

        notification.markAsRead();
        Notification updatedNotification = notificationRepository.save(notification);

        log.info("Notification marked as read: {}", notificationId);
        return convertToDto(updatedNotification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndStatus(
                userId, Notification.NotificationStatus.SENT);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
    }

    public Page<NotificationDto> getNotificationsByType(Notification.NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return notificationRepository.findByType(type, pageable)
                .map(this::convertToDto);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.SENT);
    }

    private Notification createNotificationFromEvent(NotificationEvent event) {
        Notification.NotificationType type = mapEventTypeToNotificationType(event.getEventType());
        
        return Notification.builder()
                .userId(event.getUserId())
                .userEmail(event.getUserEmail())
                .type(type)
                .title(generateTitle(type, event))
                .message(generateMessage(type, event))
                .referenceType(event.getReferenceType())
                .referenceId(event.getReferenceId())
                .status(Notification.NotificationStatus.PENDING)
                .build();
    }

    private Notification.NotificationType mapEventTypeToNotificationType(String eventType) {
        switch (eventType) {
            case "ORDER_CONFIRMED":
                return Notification.NotificationType.ORDER_CONFIRMED;
            case "ORDER_SHIPPED":
                return Notification.NotificationType.ORDER_SHIPPED;
            case "ORDER_DELIVERED":
                return Notification.NotificationType.ORDER_DELIVERED;
            case "PAYMENT_SUCCESS":
                return Notification.NotificationType.PAYMENT_SUCCESS;
            case "PAYMENT_FAILED":
                return Notification.NotificationType.PAYMENT_FAILED;
            case "LOW_STOCK_ALERT":
                return Notification.NotificationType.LOW_STOCK_ALERT;
            case "OUT_OF_STOCK_ALERT":
                return Notification.NotificationType.OUT_OF_STOCK_ALERT;
            default:
                return Notification.NotificationType.SYSTEM_ANNOUNCEMENT;
        }
    }

    private String generateTitle(Notification.NotificationType type, NotificationEvent event) {
        switch (type) {
            case ORDER_CONFIRMED:
                return "Order Confirmed";
            case ORDER_SHIPPED:
                return "Order Shipped";
            case ORDER_DELIVERED:
                return "Order Delivered";
            case PAYMENT_SUCCESS:
                return "Payment Successful";
            case PAYMENT_FAILED:
                return "Payment Failed";
            case LOW_STOCK_ALERT:
                return "Low Stock Alert";
            case OUT_OF_STOCK_ALERT:
                return "Out of Stock Alert";
            default:
                return "Notification";
        }
    }

    private String generateMessage(Notification.NotificationType type, NotificationEvent event) {
        switch (type) {
            case ORDER_CONFIRMED:
                return "Your order " + event.getReferenceId() + " has been confirmed.";
            case ORDER_SHIPPED:
                return "Your order " + event.getReferenceId() + " has been shipped.";
            case ORDER_DELIVERED:
                return "Your order " + event.getReferenceId() + " has been delivered.";
            case PAYMENT_SUCCESS:
                return "Payment was successful.";
            case PAYMENT_FAILED:
                return "Payment failed. Please try again.";
            case LOW_STOCK_ALERT:
                return "Product " + event.getReferenceId() + " is running low on stock.";
            case OUT_OF_STOCK_ALERT:
                return "Product " + event.getReferenceId() + " is now out of stock.";
            default:
                return event.getMessage() != null ? event.getMessage() : "System notification";
        }
    }

    private void sendNotification(Notification notification) {
        try {
            // Send email notification
            if (notification.getUserEmail() != null) {
                emailService.sendNotificationEmail(notification.getUserEmail(), notification.getTitle(), notification.getMessage());
                notification.setSentVia("EMAIL");
            }

            // Send WebSocket notification
            if (notification.getUserId() != null) {
                webSocketService.sendNotification(notification.getUserId(), notification.getTitle(), notification.getMessage());
                if (notification.getSentVia() == null) {
                    notification.setSentVia("WEBSOCKET");
                } else {
                    notification.setSentVia(notification.getSentVia() + ", WEBSOCKET");
                }
            }

            // Mark as sent
            notification.markAsSent();
            notificationRepository.save(notification);

            log.info("Notification sent successfully: {}", notification.getId());

        } catch (Exception e) {
            notification.markAsFailed(e.getMessage());
            notificationRepository.save(notification);
            log.error("Failed to send notification: {}", notification.getId(), e);
        }
    }

    private NotificationDto convertToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .userEmail(notification.getUserEmail())
                .type(NotificationDto.NotificationType.valueOf(notification.getType().name()))
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(NotificationDto.NotificationStatus.valueOf(notification.getStatus().name()))
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .sentVia(notification.getSentVia())
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .errorMessage(notification.getErrorMessage())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
