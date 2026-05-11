package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryDto;
import com.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Management", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID", description = "Retrieves inventory details for a specific product")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<InventoryDto> getInventoryByProductId(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        InventoryDto inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/sku/{productSku}")
    @Operation(summary = "Get inventory by SKU", description = "Retrieves inventory details by product SKU")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryDto> getInventoryBySku(
            @Parameter(description = "Product SKU") @PathVariable String productSku) {
        InventoryDto inventory = inventoryService.getInventoryBySku(productSku);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock items", description = "Retrieves items with low stock levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryDto>> getLowStockItems() {
        List<InventoryDto> items = inventoryService.getLowStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock items", description = "Retrieves items that are out of stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryDto>> getOutOfStockItems() {
        List<InventoryDto> items = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping
    @Operation(summary = "Get all inventory", description = "Retrieves all inventory records")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryDto>> getAllInventory() {
        List<InventoryDto> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve inventory", description = "Reserves stock for an order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reserveInventory(@Valid @RequestBody List<InventoryDto.ReserveInventoryRequest> request) {
        // This would need order ID from the request
        Long orderId = 1L; // Mock order ID
        inventoryService.reserveInventory(orderId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release/{orderId}")
    @Operation(summary = "Release inventory", description = "Releases reserved stock for an order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> releaseInventory(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        inventoryService.releaseInventory(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-stock")
    @Operation(summary = "Add stock", description = "Adds stock to inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addStock(@Valid @RequestBody InventoryDto.AddStockRequest request) {
        inventoryService.addStock(request.getProductId(), request.getQuantity(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deduct-stock")
    @Operation(summary = "Deduct stock", description = "Deducts stock from inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deductStock(@Valid @RequestBody InventoryDto.DeductStockRequest request) {
        inventoryService.deductStock(request.getProductId(), request.getQuantity(), request.getReason());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/product/{productId}")
    @Operation(summary = "Update inventory", description = "Updates inventory settings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InventoryDto> updateInventory(
            @Parameter(description = "Product ID") @PathVariable Long productId,
            @Valid @RequestBody InventoryDto.UpdateInventoryRequest request) {
        InventoryDto inventory = inventoryService.updateInventory(productId, request);
        return ResponseEntity.ok(inventory);
    }
}
