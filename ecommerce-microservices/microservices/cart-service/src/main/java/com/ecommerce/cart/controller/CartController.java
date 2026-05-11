package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDto;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Management", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user cart", description = "Retrieves the user's shopping cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDto> getCart(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        CartDto cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds an item to the user's shopping cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDto> addItem(@Valid @RequestBody CartDto.AddItemRequest request,
                                          HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Adding item to cart for user: {}", userId);
        
        CartDto cart = cartService.addItem(userId, request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item", description = "Updates quantity of a cart item")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartDto> updateItem(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            @Valid @RequestBody CartDto.UpdateItemRequest request,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Updating cart item: {} for user: {}", itemId, userId);
        
        CartDto cart = cartService.updateItem(userId, itemId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's shopping cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeItem(
            @Parameter(description = "Cart item ID") @PathVariable Long itemId,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Removing cart item: {} for user: {}", itemId, userId);
        
        cartService.removeItem(userId, itemId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear cart", description = "Clears all items from the user's shopping cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> clearCart(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Clearing cart for user: {}", userId);
        
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    @Operation(summary = "Checkout cart", description = "Marks the cart as checked out")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> checkoutCart(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Checking out cart for user: {}", userId);
        
        cartService.checkoutCart(userId);
        return ResponseEntity.ok().build();
    }
}
