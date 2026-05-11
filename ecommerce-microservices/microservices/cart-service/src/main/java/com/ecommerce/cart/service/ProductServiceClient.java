package com.ecommerce.cart.service;

import com.ecommerce.cart.exception.CartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductDto getProduct(Long productId) {
        try {
            String url = productServiceUrl + "/products/" + productId;
            return restTemplate.getForObject(url, ProductDto.class);
        } catch (Exception e) {
            throw new CartException("Failed to fetch product: " + productId);
        }
    }

    public static class ProductDto {
        private Long id;
        private String name;
        private String sku;
        private Double price;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}
