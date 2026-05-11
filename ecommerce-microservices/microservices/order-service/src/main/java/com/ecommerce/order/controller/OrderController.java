package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create new order", description = "Creates a new order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto.CreateOrderRequest request,
                                              HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Creating order for user: {}", userId);
        
        OrderDto order = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieves order details by ID")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "Order ID") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        String userRolesHeader = httpRequest.getHeader("X-User-Roles");
        
        if (userIdHeader != null && userRolesHeader != null && !userRolesHeader.contains("ROLE_ADMIN")) {
            Long userId = Long.parseLong(userIdHeader);
            OrderDto order = orderService.getOrderById(id);
            
            if (!order.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            return ResponseEntity.ok(order);
        }
        
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieves order details by order number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> getOrderByNumber(
            @Parameter(description = "Order number") @PathVariable String orderNumber) {
        OrderDto order = orderService.getOrderByNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user orders", description = "Retrieves orders for the authenticated user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        Page<OrderDto> orders = orderService.getUserOrders(userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves all orders (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<OrderDto> orders = orderService.getAllOrders(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/search")
    @Operation(summary = "Search orders", description = "Advanced order search with filters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> searchOrders(@Valid @RequestBody OrderDto.OrderSearchRequest searchRequest) {
        Page<OrderDto> orders = orderService.searchOrders(searchRequest);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Updates order status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Valid @RequestBody OrderDto.UpdateOrderStatusRequest request) {
        log.info("Updating order status for ID: {} to {}", id, request.getStatus());
        OrderDto order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderDto> cancelOrder(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        OrderDto order = orderService.getOrderById(id);
        
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("Cancelling order with ID: {}", id);
        OrderDto cancelledOrder = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(cancelledOrder);
    }

    @PutMapping("/{id}/payment-status")
    @Operation(summary = "Update payment status", description = "Updates payment status for an order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePaymentStatus(
            @Parameter(description = "Order ID") @PathVariable Long id,
            @Parameter(description = "Payment status") @RequestParam OrderDto.PaymentStatus paymentStatus) {
        log.info("Updating payment status for order ID: {} to {}", id, paymentStatus);
        orderService.updatePaymentStatus(id, Order.PaymentStatus.valueOf(paymentStatus.name()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get order count by status", description = "Returns count of orders by status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getOrderCountByStatus(
            @Parameter(description = "Order status") @RequestParam OrderDto.OrderStatus status) {
        long count = orderService.getOrderCountByStatus(status);
        return ResponseEntity.ok(count);
    }
}
