package com.ecommerce.user.service;

import com.ecommerce.events.dto.OrderEvent;
import com.ecommerce.events.dto.PaymentEvent;
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

    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    public void handleOrderEvent(String orderEventJson) {
        try {
            log.info("Received order event: {}", orderEventJson);
            
            OrderEvent event = objectMapper.readValue(orderEventJson, OrderEvent.class);
            
            // Process order event - update user order history
            log.info("Processing order event: {} for user: {}", event.getEventType(), event.getUserId());
            
            switch (event.getEventType()) {
                case "ORDER_CREATED":
                case "ORDER_CONFIRMED":
                case "ORDER_SHIPPED":
                case "ORDER_DELIVERED":
                case "ORDER_CANCELLED":
                case "ORDER_REFUNDED":
                    log.info("Order status updated: {} for user: {}", event.getStatus(), event.getUserId());
                    // Here you could update user statistics or send notifications
                    break;
                default:
                    log.warn("Unknown order event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    @KafkaListener(topics = "payment-events", groupId = "user-service-group")
    public void handlePaymentEvent(String paymentEventJson) {
        try {
            log.info("Received payment event: {}", paymentEventJson);
            
            PaymentEvent event = objectMapper.readValue(paymentEventJson, PaymentEvent.class);
            
            // Process payment event - update user payment history
            log.info("Processing payment event: {} for user: {}", event.getEventType(), event.getUserId());
            
            switch (event.getEventType()) {
                case "PAYMENT_COMPLETED":
                    log.info("Payment completed for order: {} amount: {}", event.getOrderId(), event.getAmount());
                    // Update user payment statistics
                    break;
                case "PAYMENT_FAILED":
                    log.warn("Payment failed for order: {} reason: {}", event.getOrderId(), event.getFailureReason());
                    break;
                case "PAYMENT_REFUNDED":
                    log.info("Payment refunded for order: {} amount: {}", event.getOrderId(), event.getRefundAmount());
                    break;
                default:
                    log.warn("Unknown payment event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing payment event", e);
        }
    }

    @KafkaListener(topics = "review-events", groupId = "user-service-group")
    public void handleReviewEvent(String reviewEventJson) {
        try {
            log.info("Received review event: {}", reviewEventJson);
            
            ReviewEvent event = objectMapper.readValue(reviewEventJson, ReviewEvent.class);
            
            // Process review event - update user review history
            log.info("Processing review event: {} for user: {}", event.getEventType(), event.getUserId());
            
            switch (event.getEventType()) {
                case "REVIEW_CREATED":
                    log.info("Review created by user: {} for product: {}", event.getUserId(), event.getProductId());
                    // Update user review count
                    break;
                case "REVIEW_UPDATED":
                    log.info("Review updated by user: {} for product: {}", event.getUserId(), event.getProductId());
                    break;
                case "REVIEW_DELETED":
                    log.info("Review deleted by user: {} for product: {}", event.getUserId(), event.getProductId());
                    break;
                case "REVIEW_APPROVED":
                case "REVIEW_REJECTED":
                    log.info("Review {} for user: {} reason: {}", event.getEventType(), event.getUserId(), event.getReason());
                    break;
                default:
                    log.warn("Unknown review event type: {}", event.getEventType());
            }
            
        } catch (Exception e) {
            log.error("Error processing review event", e);
        }
    }
}
