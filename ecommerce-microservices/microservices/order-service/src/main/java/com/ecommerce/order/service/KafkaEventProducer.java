package com.ecommerce.order.service;

import com.ecommerce.events.config.KafkaConfig;
import com.ecommerce.events.dto.OrderEvent;
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

    public void publishOrderCreatedEvent(Long orderId, Long userId, String orderStatus, Double totalAmount) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_CREATED")
                    .orderId(orderId)
                    .userId(userId)
                    .status(OrderEvent.OrderStatus.valueOf(orderStatus))
                    .totalAmount(java.math.BigDecimal.valueOf(totalAmount))
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.ORDER_EVENTS, eventJson);
            
            log.info("Published order created event for order: {}", orderId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing order created event", e);
        }
    }

    public void publishOrderUpdatedEvent(Long orderId, String orderStatus) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_UPDATED")
                    .orderId(orderId)
                    .status(OrderEvent.OrderStatus.valueOf(orderStatus))
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.ORDER_EVENTS, eventJson);
            
            log.info("Published order updated event for order: {}", orderId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing order updated event", e);
        }
    }

    public void publishOrderCancelledEvent(Long orderId, String reason) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_CANCELLED")
                    .orderId(orderId)
                    .status(OrderEvent.OrderStatus.CANCELLED)
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.ORDER_EVENTS, eventJson);
            
            log.info("Published order cancelled event for order: {}", orderId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing order cancelled event", e);
        }
    }

    public void publishOrderShippedEvent(Long orderId, String trackingNumber) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("ORDER_SHIPPED")
                    .orderId(orderId)
                    .status(OrderEvent.OrderStatus.SHIPPED)
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.ORDER_EVENTS, eventJson);
            
            log.info("Published order shipped event for order: {}", orderId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing order shipped event", e);
        }
    }
}
