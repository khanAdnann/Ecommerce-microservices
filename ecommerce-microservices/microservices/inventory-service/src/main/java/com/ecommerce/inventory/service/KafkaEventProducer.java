package com.ecommerce.inventory.service;

import com.ecommerce.events.dto.InventoryEvent;
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

    public void publishInventoryUpdatedEvent(InventoryEvent inventory) {
        try {
            InventoryEvent event = InventoryEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("INVENTORY_UPDATED")
                    .productId(inventory.getProductId())
                    .productSku(inventory.getProductSku())
                    .quantityAvailable(inventory.getQuantityAvailable())
                    .reason("Inventory updated")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("inventory-events", eventJson);
            
            log.info("Published inventory updated event for product: {}", inventory.getProductId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing inventory updated event", e);
        }
    }

    public void publishLowStockAlertEvent(InventoryEvent inventory) {
        try {
            InventoryEvent event = InventoryEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("LOW_STOCK_ALERT")
                    .productId(inventory.getProductId())
                    .productSku(inventory.getProductSku())
                    .quantityAvailable(inventory.getQuantityAvailable())
                    .reason("Low stock alert triggered")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("inventory-events", eventJson);
            
            log.info("Published low stock alert event for product: {}", inventory.getProductId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing low stock alert event", e);
        }
    }
}
