package com.ecommerce.review.service;

import com.ecommerce.review.exception.ReviewException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    public ProductDto getProduct(Long productId) {
        try {
            String url = productServiceUrl + "/products/" + productId;
            return restTemplate.getForObject(url, ProductDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch product: {}", productId, e);
            throw new ReviewException("Failed to fetch product: " + productId);
        }
    }

    public static class ProductDto {
        private Long id;
        private String name;
        private String sku;
        private Double price;
        private String status;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
