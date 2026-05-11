package com.ecommerce.product.service;

import com.ecommerce.events.dto.InventoryEvent;
import com.ecommerce.events.dto.OrderEvent;
import com.ecommerce.events.dto.ReviewEvent;
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

    @KafkaListener(topics = "order-events", groupId = "product-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);
            
            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);
            
            // Process order event - update product popularity or inventory recommendations
            log.info("Processing order event: {} for order: {}", event.getEventType(), event.getOrderId());
            
            if (event.getItems() != null) {
                event.getItems().forEach(item -> {
                    log.info("Product {} ordered with quantity: {}", item.getProductId(), item.getQuantity());
                    // Here you could update product popularity count
                });
            }
            
        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "product-service-group")
    public void handleInventoryEvent(String inventoryEventJson) {
        try {
            log.info("Received inventory event: {}", inventoryEventJson);
            
            InventoryEvent event = objectMapper.readValue(inventoryEventJson, InventoryEvent.class);
            
            // Process inventory event - update product status based on inventory levels
            log.info("Processing inventory event: {} for product: {}", event.getEventType(), event.getProductId());
            
            switch (event.getEventType()) {
                case "STOCK_IN":
                case "INVENTORY_REPLENISHED":
                    log.info("Inventory replenished for product: {} quantity: {}", event.getProductId(), event.getQuantityAvailable());
                    // Update product status to AVAILABLE
                    break;
                case "STOCK_OUT":
                    log.warn("Product out of stock: {}", event.getProductId());
                    // Update product status to OUT_OF_STOCK
                    break;
                case "LOW_STOCK_ALERT":
                    log.warn("Inventory low for product: {} quantity: {}", event.getProductId(), event.getQuantityAvailable());
                    // Update product status to LOW_STOCK
                    break;
                default:
                    log.warn("Unknown inventory event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing inventory event", e);
        }
    }

    @KafkaListener(topics = "review-events", groupId = "product-service-group")
    public void handleReviewEvent(String reviewEventJson) {
        try {
            log.info("Received review event: {}", reviewEventJson);
            
            ReviewEvent event = objectMapper.readValue(reviewEventJson, ReviewEvent.class);
            
            // Process review event - update product ratings
            log.info("Processing review event: {} for product: {}", event.getEventType(), event.getProductId());
            
            switch (event.getEventType()) {
                case "REVIEW_CREATED":
                case "REVIEW_UPDATED":
                    log.info("Review {} by user: {} with rating: {}", event.getEventType(), event.getUserName(), event.getRating());
                    // Update product average rating
                    break;
                case "REVIEW_DELETED":
                    log.info("Review deleted for product: {}", event.getProductId());
                    // Recalculate product average rating
                    break;
                case "REVIEW_APPROVED":
                case "REVIEW_REJECTED":
                    log.info("Review {} for product: {}", event.getEventType(), event.getProductId());
                    break;
                default:
                    log.warn("Unknown review event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing review event", e);
        }
    }
}
