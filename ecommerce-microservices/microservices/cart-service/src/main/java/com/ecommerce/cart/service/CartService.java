package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.entity.*;
import com.ecommerce.cart.exception.CartException;
import com.ecommerce.cart.repository.ShoppingCartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final ShoppingCartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final KafkaEventProducer kafkaEventProducer;

    public CartDto getCart(Long userId) {
        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));
        
        return convertToDto(cart);
    }

    public CartDto addItem(Long userId, CartDto.AddItemRequest request) {
        log.info("Adding item to cart for user: {}, product: {}, quantity: {}", 
                userId, request.getProductId(), request.getQuantity());

        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseGet(() -> createNewCart(userId));

        // Check if item already exists
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            existingItem.setQuantity(newQuantity);
            existingItem.setTotalPrice(existingItem.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
            
            // Add activity log
            CartActivityLog activityLog = CartActivityLog.builder()
                    .cart(cart)
                    .activityType(CartActivityLog.CartActivityType.ITEM_UPDATED)
                    .productId(request.getProductId())
                    .quantity(newQuantity)
                    .details("Updated quantity from " + existingItem.getQuantity() + " to " + newQuantity)
                    .createdBy(String.valueOf(userId))
                    .build();
            cart.addActivityLog(activityLog);
        } else {
            // Add new item
            var product = productServiceClient.getProduct(request.getProductId());
            
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(request.getQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getPrice()))
                    .totalPrice(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(request.getQuantity())))
                    .build();
            
            cart.addItem(newItem);
            
            // Add activity log
            CartActivityLog activityLog = CartActivityLog.builder()
                    .cart(cart)
                    .activityType(CartActivityLog.CartActivityType.ITEM_ADDED)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .details("Added " + request.getQuantity() + " x " + product.getName())
                    .createdBy(String.valueOf(userId))
                    .build();
            cart.addActivityLog(activityLog);
        }

        // Recalculate totals
        recalculateCartTotals(cart);

        ShoppingCart savedCart = cartRepository.save(cart);

        // Publish cart item added event
        kafkaEventProducer.publishCartItemAddedEvent(savedCart.getId(), userId, request.getProductId(), request.getQuantity());

        log.info("Item added to cart successfully");
        return convertToDto(savedCart);
    }

    public CartDto updateItem(Long userId, Long itemId, CartDto.UpdateItemRequest request) {
        log.info("Updating cart item: {} for user: {}", itemId, userId);

        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CartException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartException("Cart item not found"));

        int oldQuantity = item.getQuantity();
        item.setQuantity(request.getQuantity());
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        // Add activity log
        CartActivityLog activityLog = CartActivityLog.builder()
                .cart(cart)
                .activityType(CartActivityLog.CartActivityType.ITEM_UPDATED)
                .productId(item.getProductId())
                .quantity(request.getQuantity())
                .details("Updated quantity from " + oldQuantity + " to " + request.getQuantity())
                .createdBy(String.valueOf(userId))
                .build();
        cart.addActivityLog(activityLog);

        // Recalculate totals
        recalculateCartTotals(cart);

        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("Cart item updated successfully");
        return convertToDto(savedCart);
    }

    public void removeItem(Long userId, Long itemId) {
        log.info("Removing cart item: {} for user: {}", itemId, userId);

        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CartException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new CartException("Cart item not found"));

        // Add activity log
        CartActivityLog activityLog = CartActivityLog.builder()
                .cart(cart)
                .activityType(CartActivityLog.CartActivityType.ITEM_REMOVED)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .details("Removed " + item.getQuantity() + " x " + item.getProductName())
                .createdBy(String.valueOf(userId))
                .build();
        cart.addActivityLog(activityLog);

        cart.removeItem(item);

        // Recalculate totals
        recalculateCartTotals(cart);

        ShoppingCart savedCart = cartRepository.save(cart);

        // Publish cart item removed event
        kafkaEventProducer.publishCartItemRemovedEvent(savedCart.getId(), userId, item.getProductId());

        log.info("Cart item removed successfully");
    }

    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CartException("Cart not found"));

        // Add activity log
        CartActivityLog activityLog = CartActivityLog.builder()
                .cart(cart)
                .activityType(CartActivityLog.CartActivityType.CART_ABANDONED)
                .details("Cart cleared by user")
                .createdBy(String.valueOf(userId))
                .build();
        cart.addActivityLog(activityLog);

        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalItems(0);

        ShoppingCart savedCart = cartRepository.save(cart);

        // Publish cart cleared event
        kafkaEventProducer.publishCartClearedEvent(savedCart.getId(), userId);

        log.info("Cart cleared successfully");
    }

    public void checkoutCart(Long userId) {
        log.info("Checking out cart for user: {}", userId);

        ShoppingCart cart = cartRepository.findByUserIdAndStatus(userId, ShoppingCart.CartStatus.ACTIVE)
                .orElseThrow(() -> new CartException("Cart not found"));

        if (cart.isEmpty()) {
            throw new CartException("Cannot checkout empty cart");
        }

        // Add activity log
        CartActivityLog activityLog = CartActivityLog.builder()
                .cart(cart)
                .activityType(CartActivityLog.CartActivityType.CHECKED_OUT)
                .details("Cart checked out")
                .createdBy(String.valueOf(userId))
                .build();
        cart.addActivityLog(activityLog);

        cart.setStatus(ShoppingCart.CartStatus.CHECKED_OUT);
        ShoppingCart savedCart = cartRepository.save(cart);

        // Publish cart checkout event
        kafkaEventProducer.publishCartCheckoutEvent(savedCart.getId(), userId);

        log.info("Cart checked out successfully");
    }

    private ShoppingCart createNewCart(Long userId) {
        ShoppingCart cart = ShoppingCart.builder()
                .userId(userId)
                .status(ShoppingCart.CartStatus.ACTIVE)
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        // Add activity log
        CartActivityLog activityLog = CartActivityLog.builder()
                .cart(cart)
                .activityType(CartActivityLog.CartActivityType.CREATED)
                .details("Cart created")
                .createdBy(String.valueOf(userId))
                .build();
        cart.addActivityLog(activityLog);

        ShoppingCart savedCart = cartRepository.save(cart);
        log.info("New cart created for user: {}", userId);
        return savedCart;
    }

    private void recalculateCartTotals(ShoppingCart cart) {
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        cart.setTotalAmount(totalAmount);
        cart.setTotalItems(totalItems);
    }

    private CartDto convertToDto(ShoppingCart cart) {
        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .status(CartDto.CartStatus.valueOf(cart.getStatus().name()))
                .totalAmount(cart.getTotalAmount())
                .totalItems(cart.getTotalItems())
                .items(cart.getItems().stream()
                        .map(item -> CartDto.CartItemDto.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productSku(item.getProductSku())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .addedAt(item.getAddedAt())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }
}
