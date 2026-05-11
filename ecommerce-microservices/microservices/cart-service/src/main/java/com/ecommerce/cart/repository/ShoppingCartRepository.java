package com.ecommerce.cart.repository;

import com.ecommerce.cart.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    
    Optional<ShoppingCart> findByUserIdAndStatus(Long userId, ShoppingCart.CartStatus status);
    
    Optional<ShoppingCart> findBySessionIdAndStatus(String sessionId, ShoppingCart.CartStatus status);
    
    @Query("SELECT c FROM ShoppingCart c WHERE c.status = 'ACTIVE' AND c.expiresAt < CURRENT_TIMESTAMP")
    java.util.List<ShoppingCart> findExpiredCarts();
}
