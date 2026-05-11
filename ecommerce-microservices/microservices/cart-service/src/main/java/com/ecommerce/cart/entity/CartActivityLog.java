package com.ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_activity_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private CartActivityType activityType;

    @Column(name = "product_id")
    private Long productId;

    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum CartActivityType {
        CREATED, ITEM_ADDED, ITEM_UPDATED, ITEM_REMOVED, CART_ABANDONED, CHECKED_OUT
    }
}
