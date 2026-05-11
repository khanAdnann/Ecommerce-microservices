package com.ecommerce.order.dto;

import com.ecommerce.order.entity.Order;
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
public class OrderDto {
    
    private Long id;
    
    private String orderNumber;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;
    
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.00", message = "Subtotal must be positive")
    private BigDecimal subtotal;
    
    @DecimalMin(value = "0.00", message = "Tax amount must be positive")
    private BigDecimal taxAmount;
    
    @DecimalMin(value = "0.00", message = "Shipping amount must be positive")
    private BigDecimal shippingAmount;
    
    @DecimalMin(value = "0.00", message = "Discount amount must be positive")
    private BigDecimal discountAmount;
    
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    private String paymentMethod;
    
    @NotNull(message = "Shipping address is required")
    private AddressDto shippingAddress;
    
    private AddressDto billingAddress;
    
    private String trackingNumber;
    
    private List<OrderItemDto> items;
    private List<OrderStatusHistoryDto> statusHistory;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    
    private String notes;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderRequest {
        @NotNull(message = "Items are required")
        @NotEmpty(message = "At least one item is required")
        private List<CreateOrderItemRequest> items;
        
        @NotNull(message = "Shipping address is required")
        private AddressDto shippingAddress;
        
        private AddressDto billingAddress;
        
        @NotBlank(message = "Payment method is required")
        private String paymentMethod;
        
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderItemRequest {
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
    public static class UpdateOrderStatusRequest {
        @NotNull(message = "Status is required")
        private OrderStatus status;
        
        private String trackingNumber;
        
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Long id;
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusHistoryDto {
        private Long id;
        private String status;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        @NotBlank(message = "Street is required")
        private String street;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Zip code is required")
        private String zipCode;
        
        @NotBlank(message = "Country is required")
        private String country;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSearchRequest {
        private Long userId;
        private OrderStatus status;
        private PaymentStatus paymentStatus;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private String sortBy;
        private String sortDirection;
        private Integer page;
        private Integer size;
    }
}
