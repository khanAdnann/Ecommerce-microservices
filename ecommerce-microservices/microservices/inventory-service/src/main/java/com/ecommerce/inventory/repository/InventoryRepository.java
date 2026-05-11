package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    java.util.Optional<Inventory> findByProductId(Long productId);
    
    java.util.Optional<Inventory> findByProductSku(String productSku);
    
    boolean existsByProductId(Long productId);
    
    List<Inventory> findByQuantityAvailableLessThan(Integer quantity);
    
    List<Inventory> findByQuantityAvailableEquals(Integer quantity);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.reorderLevel")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable = 0")
    List<Inventory> findOutOfStockItems();
    
    @Query("SELECT i FROM Inventory i JOIN i.movements m WHERE m.referenceId = :referenceId AND m.movementType = :movementType")
    List<Inventory> findByReferenceIdAndMovementType(Long referenceId, InventoryMovement.MovementType movementType);
    
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable <= i.reorderLevel")
    long countLowStockItems();
    
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable = 0")
    long countOutOfStockItems();
    
    @Query("SELECT SUM(i.quantityAvailable) FROM Inventory i")
    Long sumTotalAvailableStock();
}
