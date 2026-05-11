package com.ecommerce.product.dto;

import com.ecommerce.product.entity.Product;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "SKU is required")
    @Pattern(regexp = "^[A-Z0-9-]{3,50}$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
    private String sku;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "Cost price must be positive")
    private BigDecimal costPrice;
    
    private Long categoryId;
    private CategoryDto category;
    
    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;
    
    @DecimalMin(value = "0.00", message = "Weight must be positive")
    private BigDecimal weight;
    
    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;
    
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;
    
    @Size(max = 50, message = "Size must not exceed 50 characters")
    private String size;
    
    @Size(max = 100, message = "Material must not exceed 100 characters")
    private String material;
    
    private List<String> images;
    private List<String> tags;
    
    private ProductStatus status;
    private Boolean featured;
    private BigDecimal rating;
    private Integer reviewCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<ProductAttributeDto> attributes;

    public enum ProductStatus {
        ACTIVE, INACTIVE, DISCONTINUED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateProductRequest {
        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
        private String name;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        @NotBlank(message = "SKU is required")
        @Pattern(regexp = "^[A-Z0-9-]{3,50}$", message = "SKU must contain only uppercase letters, numbers, and hyphens")
        private String sku;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;
        
        private BigDecimal costPrice;
        
        @NotNull(message = "Category is required")
        private Long categoryId;
        
        private String brand;
        private BigDecimal weight;
        private String dimensions;
        private String color;
        private String size;
        private String material;
        
        private List<String> images;
        private List<String> tags;
        
        private Boolean featured;
        private List<ProductAttributeDto> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProductRequest {
        @Size(min = 3, max = 255, message = "Product name must be between 3 and 255 characters")
        private String name;
        
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        private String description;
        
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private BigDecimal price;
        
        private BigDecimal costPrice;
        
        private Long categoryId;
        private String brand;
        private BigDecimal weight;
        private String dimensions;
        private String color;
        private String size;
        private String material;
        
        private List<String> images;
        private List<String> tags;
        
        private ProductStatus status;
        private Boolean featured;
        private List<ProductAttributeDto> attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSearchRequest {
        private String query;
        private Long categoryId;
        private String brand;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private BigDecimal minRating;
        private Boolean featured;
        private String sortBy;
        private String sortDirection;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDto {
        private Long id;
        private String name;
        private String description;
        private Long parentId;
        private String imageUrl;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<CategoryDto> children;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductAttributeDto {
        private Long id;
        private String attributeName;
        private String attributeValue;
        private LocalDateTime createdAt;
    }
}
