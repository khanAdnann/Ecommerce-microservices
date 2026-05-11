package com.ecommerce.inventory.service;

import com.ecommerce.events.dto.OrderEvent;
import com.ecommerce.events.dto.ProductEvent;
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

    @KafkaListener(topics = "order-events", groupId = "inventory-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);

            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);

            // Process order event - reserve inventory for new orders
            log.info("Processing order event: {} for order: {}", event.getEventType(), event.getOrderId());

            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    log.info("Order created, reserve inventory for order: {}", event.getOrderId());
                    if (event.getItems() != null) {
                        event.getItems().forEach(item ->
                            log.info("Reserve {} units of product {}", item.getQuantity(), item.getProductId())
                        );
                    }
                    break;
                case "ORDER_CANCELLED":
                    log.info("Order cancelled, release inventory for order: {}", event.getOrderId());
                    // Release reserved inventory
                    break;
                case "ORDER_REFUNDED":
                    log.info("Order refunded, return inventory to stock for order: {}", event.getOrderId());
                    // Return items to inventory
                    break;
                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "product-events", groupId = "inventory-service-group")
    public void handleProductEvent(String productEventJson) {
        try {
            log.info("Received product event: {}", productEventJson);

            ProductEvent event = objectMapper.readValue(productEventJson, ProductEvent.class);

            // Process product event - update inventory for new products
            log.info("Processing product event: {} for product: {}", event.getEventType(), event.getProductId());

            switch (event.getEventType()) {
                case "PRODUCT_CREATED":
                    log.info("Product created, initialize inventory for product: {}", event.getProductId());
                    // Initialize inventory record for new product
                    break;
                case "PRODUCT_DELETED":
                    log.info("Product deleted, remove inventory for product: {}", event.getProductId());
                    // Remove inventory record
                    break;
                case "PRODUCT_STATUS_UPDATED":
                    log.info("Product status updated, check inventory for product: {}", event.getProductId());
                    // Update inventory status based on product status
                    break;
                default:
                    log.warn("Unknown product event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }
}
