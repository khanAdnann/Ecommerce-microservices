package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusHistory;
import com.ecommerce.order.exception.OrderException;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;

    public OrderDto createOrder(OrderDto.CreateOrderRequest request, Long userId) {
        log.info("Creating order for user: {}", userId);

        // Generate order number
        String orderNumber = generateOrderNumber();

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderDto.CreateOrderItemRequest itemRequest : request.getItems()) {
            // Get product details
            var product = productServiceClient.getProduct(itemRequest.getProductId());
            BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal taxAmount = subtotal.multiply(BigDecimal.valueOf(0.08)); // 8% tax
        BigDecimal shippingAmount = subtotal.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.ZERO : BigDecimal.valueOf(9.99);
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingAmount);

        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .status(Order.OrderStatus.PENDING)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .shippingAmount(shippingAmount)
                .totalAmount(totalAmount)
                .currency("USD")
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(Order.PaymentStatus.PENDING)
                .shippingAddress(convertAddressToString(request.getShippingAddress()))
                .billingAddress(request.getBillingAddress() != null ? 
                        convertAddressToString(request.getBillingAddress()) : 
                        convertAddressToString(request.getShippingAddress()))
                .notes(request.getNotes())
                .build();

        // Add order items
        for (OrderDto.CreateOrderItemRequest itemRequest : request.getItems()) {
            var product = productServiceClient.getProduct(itemRequest.getProductId());
            
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .productSku(product.getSku())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getPrice()))
                    .totalPrice(BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .build();
            
            order.addItem(orderItem);
        }

        // Add initial status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .status(Order.OrderStatus.PENDING.name())
                .notes("Order created")
                .build();
        order.addStatusHistory(history);

        Order savedOrder = orderRepository.save(order);

        // Reserve inventory
        try {
            inventoryServiceClient.reserveInventory(savedOrder.getId(), request.getItems());
        } catch (Exception e) {
            log.error("Failed to reserve inventory for order: {}", orderNumber, e);
            throw new OrderException("Failed to reserve inventory: " + e.getMessage());
        }

        // Publish order created event
        kafkaEventProducer.publishOrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getStatus().name(), savedOrder.getTotalAmount().doubleValue());

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return convertToDto(savedOrder);
    }

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found: " + id));
        return convertToDto(order);
    }

    public OrderDto getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException("Order not found: " + orderNumber));
        return convertToDto(order);
    }

    public Page<OrderDto> getUserOrders(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    public Page<OrderDto> getAllOrders(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return orderRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<OrderDto> searchOrders(OrderDto.OrderSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "desc";
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return orderRepository.searchOrders(
                searchRequest.getUserId(),
                searchRequest.getStatus() != null ? searchRequest.getStatus().name() : null,
                searchRequest.getPaymentStatus() != null ? searchRequest.getPaymentStatus().name() : null,
                searchRequest.getFromDate(),
                searchRequest.getToDate(),
                pageable
        ).map(this::convertToDto);
    }

    public OrderDto updateOrderStatus(Long id, OrderDto.UpdateOrderStatusRequest request) {
        log.info("Updating order status for ID: {} to {}", id, request.getStatus());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found: " + id));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.valueOf(request.getStatus().name()));

        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }

        // Update timestamps based on status
        if (request.getStatus() == OrderDto.OrderStatus.SHIPPED && order.getShippedAt() == null) {
            order.setShippedAt(LocalDateTime.now());
        } else if (request.getStatus() == OrderDto.OrderStatus.DELIVERED && order.getDeliveredAt() == null) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        // Add status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .status(request.getStatus().name())
                .notes(request.getNotes())
                .build();
        order.addStatusHistory(history);

        Order updatedOrder = orderRepository.save(order);

        // Publish order status updated event
        kafkaEventProducer.publishOrderUpdatedEvent(updatedOrder.getId(), updatedOrder.getStatus().name());

        log.info("Order status updated successfully for ID: {}", id);
        return convertToDto(updatedOrder);
    }

    public OrderDto cancelOrder(Long id, String reason) {
        log.info("Cancelling order with ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found: " + id));

        if (!order.canBeCancelled()) {
            throw new OrderException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);

        // Add status history
        OrderStatusHistory history = OrderStatusHistory.builder()
                .status(Order.OrderStatus.CANCELLED.name())
                .notes(reason != null ? reason : "Order cancelled")
                .build();
        order.addStatusHistory(history);

        Order updatedOrder = orderRepository.save(order);

        // Release inventory
        try {
            inventoryServiceClient.releaseInventory(id);
        } catch (Exception e) {
            log.error("Failed to release inventory for cancelled order: {}", id, e);
        }

        // Publish order cancelled event
        kafkaEventProducer.publishOrderCancelledEvent(updatedOrder.getId(), reason);

        log.info("Order cancelled successfully with ID: {}", id);
        return convertToDto(updatedOrder);
    }

    public void updatePaymentStatus(Long id, Order.PaymentStatus paymentStatus) {
        log.info("Updating payment status for order ID: {} to {}", id, paymentStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException("Order not found: " + id));

        order.setPaymentStatus(paymentStatus);
        orderRepository.save(order);

        // Publish payment status updated event using KafkaEventProducer
        kafkaEventProducer.publishOrderUpdatedEvent(order.getId(), order.getStatus().name());
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private String convertAddressToString(OrderDto.AddressDto address) {
        return String.format("{\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zipCode\":\"%s\",\"country\":\"%s\"}",
                address.getStreet(), address.getCity(), address.getState(), address.getZipCode(), address.getCountry());
    }

    private OrderDto convertToDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .status(OrderDto.OrderStatus.valueOf(order.getStatus().name()))
                .paymentStatus(OrderDto.PaymentStatus.valueOf(order.getPaymentStatus().name()))
                .totalAmount(order.getTotalAmount())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .currency(order.getCurrency())
                .paymentMethod(order.getPaymentMethod())
                .trackingNumber(order.getTrackingNumber())
                .items(order.getItems().stream()
                        .map(item -> OrderDto.OrderItemDto.builder()
                                .id(item.getId())
                                .productId(item.getProductId())
                                .productSku(item.getProductSku())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .createdAt(item.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .statusHistory(order.getStatusHistory().stream()
                        .map(history -> OrderDto.OrderStatusHistoryDto.builder()
                                .id(history.getId())
                                .status(history.getStatus())
                                .notes(history.getNotes())
                                .createdAt(history.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .notes(order.getNotes())
                .build();
    }

    public long getOrderCountByStatus(OrderDto.OrderStatus status) {
        return orderRepository.countByStatus(Order.OrderStatus.valueOf(status.name()));
    }
}
