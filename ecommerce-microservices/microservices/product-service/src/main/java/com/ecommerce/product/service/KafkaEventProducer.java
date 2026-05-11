package com.ecommerce.product.service;

import com.ecommerce.events.config.KafkaConfig;
import com.ecommerce.events.dto.ProductEvent;
import com.ecommerce.product.entity.Product;
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

    public void publishProductCreatedEvent(Product product) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PRODUCT_CREATED")
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                    .price(product.getPrice())
                    .status(ProductEvent.ProductStatus.valueOf(product.getStatus().name()))
                    .featured(product.getFeatured())
                    .createdAt(product.getCreatedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.PRODUCT_EVENTS, eventJson);
            
            log.info("Published product created event for product ID: {}", product.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing product created event", e);
        }
    }

    public void publishProductUpdatedEvent(Product product) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PRODUCT_UPDATED")
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                    .price(product.getPrice())
                    .status(ProductEvent.ProductStatus.valueOf(product.getStatus().name()))
                    .featured(product.getFeatured())
                    .updatedAt(product.getUpdatedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.PRODUCT_EVENTS, eventJson);
            
            log.info("Published product updated event for product ID: {}", product.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing product updated event", e);
        }
    }

    public void publishProductDeletedEvent(Product product) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PRODUCT_DELETED")
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                    .price(product.getPrice())
                    .status(ProductEvent.ProductStatus.valueOf(product.getStatus().name()))
                    .featured(product.getFeatured())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.PRODUCT_EVENTS, eventJson);
            
            log.info("Published product deleted event for product ID: {}", product.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing product deleted event", e);
        }
    }

    public void publishProductStatusUpdatedEvent(Product product) {
        try {
            ProductEvent event = ProductEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PRODUCT_STATUS_UPDATED")
                    .productId(product.getId())
                    .productSku(product.getSku())
                    .productName(product.getName())
                    .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                    .price(product.getPrice())
                    .status(ProductEvent.ProductStatus.valueOf(product.getStatus().name()))
                    .featured(product.getFeatured())
                    .updatedAt(product.getUpdatedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(KafkaConfig.Topics.PRODUCT_EVENTS, eventJson);
            
            log.info("Published product status updated event for product ID: {}", product.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing product status updated event", e);
        }
    }
}
