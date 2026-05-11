package com.ecommerce.review.service;

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

    @KafkaListener(topics = "order-events", groupId = "review-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);

            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);

            // Process order event - send review requests for delivered orders
            log.info("Processing order event: {} for order: {}", event.getEventType(), event.getOrderId());

            if ("ORDER_DELIVERED".equals(event.getEventType())) {
                log.info("Order delivered, send review request to user: {}", event.getUserId());
                // Send review request notification to user
                if (event.getItems() != null) {
                    event.getItems().forEach(item ->
                        log.info("Request review for product: {}", item.getProductId())
                    );
                }
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "product-events", groupId = "review-service-group")
    public void handleProductEvent(String productEventJson) {
        try {
            log.info("Received product event: {}", productEventJson);

            ProductEvent event = objectMapper.readValue(productEventJson, ProductEvent.class);

            // Process product event - update product info in reviews
            log.info("Processing product event: {} for product: {}", event.getEventType(), event.getProductId());

            switch (event.getEventType()) {
                case "PRODUCT_DELETED":
                    log.info("Product deleted, hide or remove reviews for product: {}", event.getProductId());
                    // Hide or remove reviews for deleted product
                    break;
                case "PRODUCT_UPDATED":
                    log.info("Product updated, update product name in reviews: {}", event.getProductId());
                    // Update product name in existing reviews
                    break;
                default:
                    log.warn("Unknown product event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }
}
