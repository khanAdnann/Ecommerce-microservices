package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.exception.OrderException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class InventoryServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${inventory.service.url:http://localhost:8086}")
    private String inventoryServiceUrl;

    public InventoryServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void reserveInventory(Long orderId, List<OrderDto.CreateOrderItemRequest> items) {
        try {
            ReserveInventoryRequest request = new ReserveInventoryRequest();
            request.setOrderId(orderId);
            request.setItems(items);
            
            String url = inventoryServiceUrl + "/inventory/reserve";
            restTemplate.postForObject(url, request, Void.class);
        } catch (Exception e) {
            throw new OrderException("Failed to reserve inventory: " + e.getMessage());
        }
    }

    public void releaseInventory(Long orderId) {
        try {
            String url = inventoryServiceUrl + "/inventory/release/" + orderId;
            restTemplate.postForObject(url, null, Void.class);
        } catch (Exception e) {
            throw new OrderException("Failed to release inventory: " + e.getMessage());
        }
    }

    public static class ReserveInventoryRequest {
        private Long orderId;
        private List<OrderDto.CreateOrderItemRequest> items;
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public List<OrderDto.CreateOrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderDto.CreateOrderItemRequest> items) { this.items = items; }
    }
}
