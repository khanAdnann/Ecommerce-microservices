package com.ecommerce.analytics.service;

import com.ecommerce.events.dto.OrderEvent;
import com.ecommerce.events.dto.PaymentEvent;
import com.ecommerce.events.dto.ProductEvent;
import com.ecommerce.events.dto.UserEvent;
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
    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "order-events", groupId = "analytics-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);

            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);

            // Process order event - update order analytics
            log.info("Processing order event: {} for order: {}", event.getEventType(), event.getOrderId());

            switch (event.getEventType()) {
                case "ORDER_CREATED":
                    log.info("Order created, update order count analytics");
                    analyticsService.saveAnalyticsData("ORDER_COUNT", java.math.BigDecimal.ONE, "order-created");
                    if (event.getTotalAmount() != null) {
                        analyticsService.saveAnalyticsData("ORDER_VALUE", event.getTotalAmount(), "order-created");
                    }
                    break;
                case "ORDER_CANCELLED":
                    log.info("Order cancelled, update cancellation analytics");
                    analyticsService.saveAnalyticsData("ORDER_CANCELLED", java.math.BigDecimal.ONE, "order-cancelled");
                    break;
                case "ORDER_DELIVERED":
                    log.info("Order delivered, update delivery analytics");
                    analyticsService.saveAnalyticsData("ORDER_DELIVERED", java.math.BigDecimal.ONE, "order-delivered");
                    break;
                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "analytics-service-group")
    public void handlePaymentEvent(String paymentEventJson) {
        try {
            log.info("Received payment event: {}", paymentEventJson);

            PaymentEvent event = objectMapper.readValue(paymentEventJson, PaymentEvent.class);

            // Process payment event - update revenue analytics
            log.info("Processing payment event: {} for payment: {}", event.getEventType(), event.getPaymentReference());

            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    log.info("Payment completed, update revenue analytics: {}", event.getAmount());
                    if (event.getAmount() != null) {
                        analyticsService.saveAnalyticsData("REVENUE", event.getAmount(), "payment-completed");
                    }
                    analyticsService.saveAnalyticsData("PAYMENT_COUNT", java.math.BigDecimal.ONE, "payment-completed");
                    break;
                case "PAYMENT_FAILED":
                    log.info("Payment failed, update failed payment analytics");
                    analyticsService.saveAnalyticsData("PAYMENT_FAILED", java.math.BigDecimal.ONE, "payment-failed");
                    break;
                case "PAYMENT_REFUNDED":
                    log.info("Payment refunded, update refund analytics: {}", event.getRefundAmount());
                    if (event.getRefundAmount() != null) {
                        analyticsService.saveAnalyticsData("REFUND", event.getRefundAmount(), "payment-refunded");
                    }
                    break;
                default:
                    log.warn("Unknown payment event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }

    @KafkaListener(topics = "product-events", groupId = "analytics-service-group")
    public void handleProductEvent(String productEventJson) {
        try {
            log.info("Received product event: {}", productEventJson);

            ProductEvent event = objectMapper.readValue(productEventJson, ProductEvent.class);

            // Process product event - update product analytics
            log.info("Processing product event: {} for product: {}", event.getEventType(), event.getProductId());

            switch (event.getEventType()) {
                case "PRODUCT_CREATED":
                    log.info("Product created, update product count analytics");
                    analyticsService.saveAnalyticsData("PRODUCT_COUNT", java.math.BigDecimal.ONE, "product-created");
                    break;
                case "PRODUCT_UPDATED":
                    log.info("Product updated");
                    analyticsService.saveAnalyticsData("PRODUCT_UPDATED", java.math.BigDecimal.ONE, "product-updated");
                    break;
                case "PRODUCT_DELETED":
                    log.info("Product deleted");
                    analyticsService.saveAnalyticsData("PRODUCT_DELETED", java.math.BigDecimal.ONE, "product-deleted");
                    break;
                default:
                    log.warn("Unknown product event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }

    @KafkaListener(topics = "user-events", groupId = "analytics-service-group")
    public void handleUserEvent(String userEventJson) {
        try {
            log.info("Received user event: {}", userEventJson);

            UserEvent event = objectMapper.readValue(userEventJson, UserEvent.class);

            // Process user event - update user analytics
            log.info("Processing user event: {} for user: {}", event.getEventType(), event.getUserId());

            switch (event.getEventType()) {
                case "USER_REGISTERED":
                    log.info("User registered, update user count analytics");
                    analyticsService.saveAnalyticsData("USER_COUNT", java.math.BigDecimal.ONE, "user-registered");
                    break;
                case "USER_LOGIN":
                    log.info("User logged in, update login analytics");
                    analyticsService.saveAnalyticsData("USER_LOGIN", java.math.BigDecimal.ONE, "user-login");
                    break;
                case "USER_UPDATED":
                    log.info("User updated");
                    analyticsService.saveAnalyticsData("USER_UPDATED", java.math.BigDecimal.ONE, "user-updated");
                    break;
                case "USER_DELETED":
                    log.info("User deleted");
                    analyticsService.saveAnalyticsData("USER_DELETED", java.math.BigDecimal.ONE, "user-deleted");
                    break;
                default:
                    log.warn("Unknown user event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing user event", e);
        }
    }
}
