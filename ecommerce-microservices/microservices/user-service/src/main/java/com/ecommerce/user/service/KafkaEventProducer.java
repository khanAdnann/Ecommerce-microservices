package com.ecommerce.user.service;

import com.ecommerce.events.config.KafkaConfig;
import com.ecommerce.events.dto.UserEvent;
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

    public void publishUserRegisteredEvent(Long userId, String email, String firstName, String lastName) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_REGISTERED")
                    .userId(userId)
                    .userEmail(email)
                    .userName(firstName + " " + lastName)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.USER_EVENTS, eventJson);
            
            log.info("Published user registered event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing user registered event", e);
        }
    }

    public void publishUserLoginEvent(Long userId, String email) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_LOGIN")
                    .userId(userId)
                    .userEmail(email)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.USER_EVENTS, eventJson);
            
            log.info("Published user login event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing user login event", e);
        }
    }

    public void publishUserUpdatedEvent(Long userId, String email, String firstName, String lastName) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_UPDATED")
                    .userId(userId)
                    .userEmail(email)
                    .userName(firstName + " " + lastName)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.USER_EVENTS, eventJson);
            
            log.info("Published user updated event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing user updated event", e);
        }
    }

    public void publishUserDeletedEvent(Long userId, String email) {
        try {
            UserEvent event = UserEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("USER_DELETED")
                    .userId(userId)
                    .userEmail(email)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.USER_EVENTS, eventJson);
            
            log.info("Published user deleted event for user: {}", userId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing user deleted event", e);
        }
    }
}
