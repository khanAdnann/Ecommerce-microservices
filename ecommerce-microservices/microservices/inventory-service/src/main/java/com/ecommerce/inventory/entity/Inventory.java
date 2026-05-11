package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "product_sku")
    private String productSku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "quantity_on_order", nullable = false)
    @Builder.Default
    private Integer quantityOnOrder = 0;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "reorder_quantity")
    private Integer reorderQuantity;

    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.Set<InventoryMovement> movements = new java.util.HashSet<>();

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.Set<LowStockAlert> lowStockAlerts = new java.util.HashSet<>();

    public void addMovement(InventoryMovement movement) {
        movements.add(movement);
        movement.setInventory(this);
    }

    public void removeMovement(InventoryMovement movement) {
        movements.remove(movement);
        movement.setInventory(null);
    }

    public void addLowStockAlert(LowStockAlert alert) {
        lowStockAlerts.add(alert);
        alert.setInventory(this);
    }

    public void removeLowStockAlert(LowStockAlert alert) {
        lowStockAlerts.remove(alert);
        alert.setInventory(null);
    }

    public Integer getQuantityTotal() {
        return quantityAvailable + quantityReserved;
    }

    public boolean isLowStock() {
        return reorderLevel != null && quantityAvailable <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return quantityAvailable <= 0;
    }

    public boolean canReserve(Integer quantity) {
        return quantityAvailable >= quantity;
    }

    public void reserveStock(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new RuntimeException("Insufficient stock available");
        }
        quantityAvailable -= quantity;
        quantityReserved += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void releaseReservation(Integer quantity) {
        if (quantityReserved < quantity) {
            throw new RuntimeException("Cannot release more than reserved quantity");
        }
        quantityReserved -= quantity;
        quantityAvailable += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void addStock(Integer quantity) {
        quantityAvailable += quantity;
        lastUpdated = LocalDateTime.now();
    }

    public void deductStock(Integer quantity) {
        if (quantityAvailable < quantity) {
            throw new RuntimeException("Insufficient stock available");
        }
        quantityAvailable -= quantity;
        lastUpdated = LocalDateTime.now();
    }
}
