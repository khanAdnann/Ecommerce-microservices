package com.ecommerce.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CartEvent {
    
    private String eventId;
    private String eventType;
    private Long cartId;
    private Long userId;
    private Long sessionId;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private String reason;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    public enum CartEventType {
        CART_CREATED, ITEM_ADDED, ITEM_UPDATED, ITEM_REMOVED, CART_CLEARED, CART_CHECKOUT
    }
}
