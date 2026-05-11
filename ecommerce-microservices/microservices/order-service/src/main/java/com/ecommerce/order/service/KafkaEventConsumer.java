package com.ecommerce.order.service;

import com.ecommerce.events.dto.InventoryEvent;
import com.ecommerce.events.dto.NotificationEvent;
import com.ecommerce.events.dto.PaymentEvent;
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

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(String paymentEventJson) {
        try {
            log.info("Received payment event: {}", paymentEventJson);
            
            PaymentEvent event = objectMapper.readValue(paymentEventJson, PaymentEvent.class);
            
            // Process payment event - update order payment status based on payment completion
            log.info("Processing payment event: {} for order: {}", event.getEventType(), event.getOrderId());
            
            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    log.info("Payment completed for order: {} amount: {}", event.getOrderId(), event.getAmount());
                    // Update order status to CONFIRMED
                    break;
                case "PAYMENT_FAILED":
                    log.warn("Payment failed for order: {} reason: {}", event.getOrderId(), event.getFailureReason());
                    // Update order status to PAYMENT_FAILED or CANCELLED
                    break;
                case "PAYMENT_REFUNDED":
                    log.info("Payment refunded for order: {} amount: {}", event.getOrderId(), event.getRefundAmount());
                    // Update order status to REFUNDED
                    break;
                default:
                    log.warn("Unknown payment event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "order-service-group")
    public void handleInventoryEvent(String inventoryEventJson) {
        try {
            log.info("Received inventory event: {}", inventoryEventJson);
            
            InventoryEvent event = objectMapper.readValue(inventoryEventJson, InventoryEvent.class);
            
            // Process inventory event - update order status based on inventory availability
            log.info("Processing inventory event: {} for product: {}", event.getEventType(), event.getProductId());
            
            switch (event.getEventType()) {
                case "STOCK_OUT":
                    log.warn("Product out of stock: {}", event.getProductId());
                    // If referenceType is ORDER, update order status to OUT_OF_STOCK
                    if ("ORDER".equals(event.getReferenceType())) {
                        log.info("Inventory issue for order: {}", event.getReferenceId());
                        // Update order status accordingly
                    }
                    break;
                case "STOCK_IN":
                case "INVENTORY_REPLENISHED":
                    log.info("Inventory replenished for product: {}", event.getProductId());
                    // Process pending orders
                    break;
                case "RESERVATION":
                    log.info("Inventory reserved for order: {}", event.getReferenceId());
                    // Update order status to PROCESSING
                    break;
                default:
                    log.warn("Unknown inventory event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing inventory event", e);
        }
    }

    @KafkaListener(topics = "notification-events", groupId = "order-service-group")
    public void handleNotificationEvent(String notificationEventJson) {
        try {
            log.info("Received notification event: {}", notificationEventJson);
            
            NotificationEvent event = objectMapper.readValue(notificationEventJson, NotificationEvent.class);
            
            // Process notification event - handle order-related notifications
            log.info("Processing notification event: {} for order: {}", event.getEventType(), event.getReferenceId());
            
            // Log notification for order status updates
            if ("ORDER".equals(event.getReferenceType())) {
                log.info("Order notification: {} - {}", event.getEventType(), event.getMessage());
                // Update order notification status
            }
            
        } catch (Exception e) {
            log.error("Error processing notification event", e);
        }
    }
}
