package com.ecommerce.cart.service;

import com.ecommerce.events.config.KafkaConfig;
import com.ecommerce.events.dto.CartEvent;
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

    public void publishCartItemAddedEvent(Long cartId, Long userId, Long productId, Integer quantity) {
        try {
            CartEvent.CartItemDto itemDto = CartEvent.CartItemDto.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .build();
            
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CART_ITEM_ADDED")
                    .cartId(cartId)
                    .userId(userId)
                    .items(java.util.List.of(itemDto))
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.CART_EVENTS, eventJson);
            
            log.info("Published cart item added event for cart: {}", cartId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing cart item added event", e);
        }
    }

    public void publishCartItemRemovedEvent(Long cartId, Long userId, Long productId) {
        try {
            CartEvent.CartItemDto itemDto = CartEvent.CartItemDto.builder()
                    .productId(productId)
                    .build();
            
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CART_ITEM_REMOVED")
                    .cartId(cartId)
                    .userId(userId)
                    .items(java.util.List.of(itemDto))
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.CART_EVENTS, eventJson);
            
            log.info("Published cart item removed event for cart: {}", cartId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing cart item removed event", e);
        }
    }

    public void publishCartClearedEvent(Long cartId, Long userId) {
        try {
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CART_CLEARED")
                    .cartId(cartId)
                    .userId(userId)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.CART_EVENTS, eventJson);
            
            log.info("Published cart cleared event for cart: {}", cartId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing cart cleared event", e);
        }
    }

    public void publishCartCheckoutEvent(Long cartId, Long userId) {
        try {
            CartEvent event = CartEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("CART_CHECKOUT")
                    .cartId(cartId)
                    .userId(userId)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.CART_EVENTS, eventJson);
            
            log.info("Published cart checkout event for cart: {}", cartId);
        } catch (JsonProcessingException e) {
            log.error("Error publishing cart checkout event", e);
        }
    }
}
