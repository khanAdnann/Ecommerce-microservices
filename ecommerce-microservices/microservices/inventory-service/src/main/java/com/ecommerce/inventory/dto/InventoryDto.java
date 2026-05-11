package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    
    private Long id;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer quantityOnOrder;
    private Integer reorderLevel;
    private Integer reorderQuantity;
    private String warehouseLocation;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReserveInventoryRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateInventoryRequest {
        private Integer reorderLevel;
        private Integer reorderQuantity;
        private String warehouseLocation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddStockRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeductStockRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
        
        @NotBlank(message = "Reason is required")
        private String reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryMovementDto {
        private Long id;
        private MovementType movementType;
        private Integer quantity;
        private Integer quantityBefore;
        private Integer quantityAfter;
        private String referenceType;
        private Long referenceId;
        private String notes;
        private String createdBy;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowStockAlertDto {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer currentStock;
        private Integer reorderLevel;
        private Integer quantityNeeded;
        private AlertSeverity severity;
        private Boolean isResolved;
        private LocalDateTime resolvedAt;
        private LocalDateTime createdAt;
    }

    public enum MovementType {
        STOCK_IN, STOCK_OUT, RESERVATION, UNRESERVATION, ADJUSTMENT, RETURN
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}
