package com.ecommerce.inventory.service;

import com.ecommerce.events.dto.InventoryEvent;
import com.ecommerce.inventory.dto.InventoryDto;
import com.ecommerce.inventory.entity.*;
import com.ecommerce.inventory.exception.InventoryException;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public InventoryDto getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("Inventory not found for product: " + productId));
        return convertToDto(inventory);
    }

    public InventoryDto getInventoryBySku(String productSku) {
        Inventory inventory = inventoryRepository.findByProductSku(productSku)
                .orElseThrow(() -> new InventoryException("Inventory not found for SKU: " + productSku));
        return convertToDto(inventory);
    }

    public void reserveInventory(Long orderId, List<InventoryDto.ReserveInventoryRequest> items) {
        log.info("Reserving inventory for order: {}", orderId);

        for (InventoryDto.ReserveInventoryRequest item : items) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new InventoryException("Inventory not found for product: " + item.getProductId()));

            if (!inventory.canReserve(item.getQuantity())) {
                throw new InventoryException("Insufficient stock for product: " + item.getProductId());
            }

            // Reserve stock
            Integer quantityBefore = inventory.getQuantityAvailable();
            inventory.reserveStock(item.getQuantity());

            // Create movement record
            InventoryMovement movement = InventoryMovement.builder()
                    .inventory(inventory)
                    .movementType(InventoryMovement.MovementType.RESERVATION)
                    .quantity(item.getQuantity())
                    .quantityBefore(quantityBefore)
                    .quantityAfter(inventory.getQuantityAvailable())
                    .referenceType("ORDER")
                    .referenceId(orderId)
                    .notes("Stock reserved for order: " + orderId)
                    .createdBy("system")
                    .build();
            inventory.addMovement(movement);

            // Check for low stock alert
            checkAndCreateLowStockAlert(inventory);

            inventoryRepository.save(inventory);
        }

        log.info("Inventory reservation completed for order: {}", orderId);
    }

    public void releaseInventory(Long orderId) {
        log.info("Releasing inventory for order: {}", orderId);

        List<Inventory> inventories = inventoryRepository.findByReferenceIdAndMovementType(
                orderId, InventoryMovement.MovementType.RESERVATION);

        for (Inventory inventory : inventories) {
            // Calculate total reserved quantity for this order
            Integer totalReserved = inventory.getMovements().stream()
                    .filter(m -> m.getMovementType() == InventoryMovement.MovementType.RESERVATION &&
                            m.getReferenceId().equals(orderId))
                    .mapToInt(InventoryMovement::getQuantity)
                    .sum();

            // Release reservation
            inventory.releaseReservation(totalReserved);

            // Create movement record
            InventoryMovement movement = InventoryMovement.builder()
                    .inventory(inventory)
                    .movementType(InventoryMovement.MovementType.UNRESERVATION)
                    .quantity(totalReserved)
                    .quantityBefore(inventory.getQuantityAvailable())
                    .quantityAfter(inventory.getQuantityAvailable())
                    .referenceType("ORDER")
                    .referenceId(orderId)
                    .notes("Stock reservation released for order: " + orderId)
                    .createdBy("system")
                    .build();
            inventory.addMovement(movement);

            inventoryRepository.save(inventory);
        }

        log.info("Inventory release completed for order: {}", orderId);
    }

    public void addStock(Long productId, Integer quantity, String reason) {
        log.info("Adding stock for product: {}, quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("Inventory not found for product: " + productId));

        Integer quantityBefore = inventory.getQuantityAvailable();
        inventory.addStock(quantity);

        // Create movement record
        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .movementType(InventoryMovement.MovementType.STOCK_IN)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(inventory.getQuantityAvailable())
                .referenceType("STOCK_ADJUSTMENT")
                .notes(reason)
                .createdBy("system")
                .build();
        inventory.addMovement(movement);

        inventoryRepository.save(inventory);

        // Publish inventory updated event
        InventoryEvent event = InventoryEvent.builder()
                .productId(inventory.getProductId())
                .productSku(inventory.getProductSku())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityChanged(quantity)
                .reason(reason)
                .build();
        kafkaEventProducer.publishInventoryUpdatedEvent(event);

        log.info("Stock added successfully for product: {}", productId);
    }

    public void deductStock(Long productId, Integer quantity, String reason) {
        log.info("Deducting stock for product: {}, quantity: {}", productId, quantity);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("Inventory not found for product: " + productId));

        Integer quantityBefore = inventory.getQuantityAvailable();
        inventory.deductStock(quantity);

        // Create movement record
        InventoryMovement movement = InventoryMovement.builder()
                .inventory(inventory)
                .movementType(InventoryMovement.MovementType.STOCK_OUT)
                .quantity(quantity)
                .quantityBefore(quantityBefore)
                .quantityAfter(inventory.getQuantityAvailable())
                .referenceType("STOCK_ADJUSTMENT")
                .notes(reason)
                .createdBy("system")
                .build();
        inventory.addMovement(movement);

        // Check for low stock alert
        checkAndCreateLowStockAlert(inventory);

        inventoryRepository.save(inventory);
        log.info("Stock deducted successfully for product: {}", productId);
    }

    public List<InventoryDto> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InventoryDto> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public InventoryDto updateInventory(Long productId, InventoryDto.UpdateInventoryRequest request) {
        log.info("Updating inventory for product: {}", productId);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryException("Inventory not found for product: " + productId));

        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getReorderQuantity() != null) {
            inventory.setReorderQuantity(request.getReorderQuantity());
        }
        if (request.getWarehouseLocation() != null) {
            inventory.setWarehouseLocation(request.getWarehouseLocation());
        }

        inventoryRepository.save(inventory);

        // Publish inventory updated event
        InventoryEvent event = InventoryEvent.builder()
                .productId(inventory.getProductId())
                .productSku(inventory.getProductSku())
                .quantityAvailable(inventory.getQuantityAvailable())
                .build();
        kafkaEventProducer.publishInventoryUpdatedEvent(event);

        log.info("Inventory updated successfully for product: {}", productId);
        return convertToDto(inventory);
    }

    private void checkAndCreateLowStockAlert(Inventory inventory) {
        if (inventory.isLowStock()) {
            // Check if alert already exists and is not resolved
            boolean hasUnresolvedAlert = inventory.getLowStockAlerts().stream()
                    .anyMatch(alert -> !alert.getIsResolved());

            if (!hasUnresolvedAlert) {
                LowStockAlert alert = LowStockAlert.builder()
                        .inventory(inventory)
                        .currentStock(inventory.getQuantityAvailable())
                        .reorderLevel(inventory.getReorderLevel())
                        .quantityNeeded(inventory.getReorderQuantity() != null ? 
                                inventory.getReorderQuantity() : 10)
                        .severity(determineAlertSeverity(inventory))
                        .isResolved(false)
                        .build();
                inventory.addLowStockAlert(alert);

                // Publish low stock alert event
                InventoryEvent event = InventoryEvent.builder()
                        .productId(inventory.getProductId())
                        .productSku(inventory.getProductSku())
                        .quantityAvailable(inventory.getQuantityAvailable())
                        .build();
                kafkaEventProducer.publishLowStockAlertEvent(event);

                log.warn("Low stock alert created for product: {}", inventory.getProductId());
            }
        }
    }

    private LowStockAlert.AlertSeverity determineAlertSeverity(Inventory inventory) {
        if (inventory.getQuantityAvailable() == 0) {
            return LowStockAlert.AlertSeverity.CRITICAL;
        } else if (inventory.getQuantityAvailable() <= inventory.getReorderLevel() / 2) {
            return LowStockAlert.AlertSeverity.HIGH;
        } else {
            return LowStockAlert.AlertSeverity.MEDIUM;
        }
    }

    private InventoryDto convertToDto(Inventory inventory) {
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .productSku(inventory.getProductSku())
                .productName(inventory.getProductName())
                .quantityAvailable(inventory.getQuantityAvailable())
                .quantityReserved(inventory.getQuantityReserved())
                .quantityOnOrder(inventory.getQuantityOnOrder())
                .reorderLevel(inventory.getReorderLevel())
                .reorderQuantity(inventory.getReorderQuantity())
                .warehouseLocation(inventory.getWarehouseLocation())
                .lastUpdated(inventory.getLastUpdated())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
