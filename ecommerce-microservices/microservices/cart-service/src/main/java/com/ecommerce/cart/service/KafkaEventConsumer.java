package com.ecommerce.cart.service;

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

    @KafkaListener(topics = "order-events", groupId = "cart-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);

            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);

            // Process order event - clear cart when order is placed
            log.info("Processing order event: {} for user: {}", event.getEventType(), event.getUserId());

            if ("ORDER_CREATED".equals(event.getEventType())) {
                log.info("Order created, clear cart for user: {}", event.getUserId());
                // Clear user's cart after successful order
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "product-events", groupId = "cart-service-group")
    public void handleProductEvent(String productEventJson) {
        try {
            log.info("Received product event: {}", productEventJson);

            ProductEvent event = objectMapper.readValue(productEventJson, ProductEvent.class);

            // Process product event - update cart items if product is updated or removed
            log.info("Processing product event: {} for product: {}", event.getEventType(), event.getProductId());

            switch (event.getEventType()) {
                case "PRODUCT_DELETED":
                case "PRODUCT_STATUS_UPDATED":
                    log.info("Product {} updated, update carts containing this product", event.getProductId());
                    // Remove or update cart items with this product
                    break;
                case "PRODUCT_UPDATED":
                    log.info("Product {} updated, update cart item prices", event.getProductId());
                    // Update cart item prices if product price changed
                    break;
                default:
                    log.warn("Unknown product event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }
}
