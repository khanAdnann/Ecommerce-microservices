package com.ecommerce.payment.service;

import com.ecommerce.events.dto.NotificationEvent;
import com.ecommerce.events.dto.OrderEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);

            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);

            // Process order event - initiate payment if needed
            log.info("Processing order event: {} for order: {}", event.getEventType(), event.getOrderId());

            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    log.info("Order created, initiate payment for order: {}", event.getOrderId());
                    // Trigger payment processing
                    break;
                case "ORDER_CANCELLED":
                    log.info("Order cancelled, refund payment if applicable for order: {}", event.getOrderId());
                    // Process refund
                    break;
                case "ORDER_CONFIRMED":
                    log.info("Order confirmed, payment already processed for order: {}", event.getOrderId());
                    break;
                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "notification-events", groupId = "payment-service-group")
    public void handleNotificationEvent(String notificationEventJson) {
        try {
            log.info("Received notification event: {}", notificationEventJson);

            NotificationEvent event = objectMapper.readValue(notificationEventJson, NotificationEvent.class);

            // Process notification event
            log.info("Processing notification event: {} for payment: {}", event.getEventType(), event.getReferenceId());

            if ("PAYMENT".equals(event.getReferenceType())) {
                log.info("Payment notification: {} - {}", event.getEventType(), event.getMessage());
                // Update payment notification status
            }

        } catch (Exception e) {
            log.error("Error processing notification event", e);
        }
    }
}
