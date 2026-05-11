package com.ecommerce.notification.service;

import com.ecommerce.events.dto.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishNotificationSentEvent(NotificationEvent notification) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("NOTIFICATION_SENT")
                    .userId(notification.getUserId())
                    .userEmail(notification.getUserEmail())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                                        .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("notification-events", eventJson);
            
            log.info("Published notification sent event for user: {}", notification.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing notification sent event", e);
        }
    }

    public void publishNotificationFailedEvent(NotificationEvent notification, String errorMessage) {
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("NOTIFICATION_FAILED")
                    .userId(notification.getUserId())
                    .userEmail(notification.getUserEmail())
                    .type(notification.getType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                                        .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("notification-events", eventJson);
            
            log.info("Published notification failed event for user: {}", notification.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing notification failed event", e);
        }
    }
}
