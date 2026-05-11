package com.ecommerce.notification.controller;

import com.ecommerce.notification.dto.NotificationDto;
import com.ecommerce.notification.service.NotificationService;
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
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create notification", description = "Creates a new notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody NotificationDto.CreateNotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());
        NotificationDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user notifications", description = "Retrieves notifications for the authenticated user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
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
        Page<NotificationDto> notifications = notificationService.getUserNotifications(userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieves all notifications (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getAllNotifications(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<NotificationDto> notifications = notificationService.getAllNotifications(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type", description = "Retrieves notifications by type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByType(
            @Parameter(description = "Notification type") @PathVariable NotificationDto.NotificationType type,
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size) {
        
        Page<NotificationDto> notifications = notificationService.getNotificationsByType(com.ecommerce.notification.entity.Notification.NotificationType.valueOf(type.name()), page, size);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<NotificationDto> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Marking notification as read: {}", id);
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all user notifications as read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Returns count of unread notifications for user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long userId = Long.parseLong(userIdHeader);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}
