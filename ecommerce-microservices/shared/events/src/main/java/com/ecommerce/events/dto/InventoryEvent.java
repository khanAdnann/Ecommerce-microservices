package com.ecommerce.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
    
    private String eventId;
    private String eventType;
    private Long productId;
    private String productSku;
    private String productName;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer quantityOnOrder;
    private Integer quantityChanged;
    private String warehouseLocation;
    private String reason;
    private String referenceType;
    private Long referenceId;
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public enum InventoryEventType {
        STOCK_IN, STOCK_OUT, RESERVATION, UNRESERVATION, ADJUSTMENT, RETURN, LOW_STOCK_ALERT
    }
}
