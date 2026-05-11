package com.ecommerce.payment.service;

import com.ecommerce.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${order.service.url:http://localhost:8083}")
    private String orderServiceUrl;

    public OrderDto getOrder(Long orderId) {
        try {
            String url = orderServiceUrl + "/orders/" + orderId;
            return restTemplate.getForObject(url, OrderDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch order: {}", orderId, e);
            throw new PaymentException("Failed to fetch order: " + orderId);
        }
    }

    public static class OrderDto {
        private Long id;
        private String orderNumber;
        private Long userId;
        private String status;
        private Double totalAmount;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    }
}
