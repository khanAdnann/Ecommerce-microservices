package com.ecommerce.payment.controller;

import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.service.PaymentService;
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
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Processes a new payment for an order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentDto> processPayment(@Valid @RequestBody PaymentDto.ProcessPaymentRequest request,
                                                   HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        log.info("Processing payment for user: {}", userIdHeader);
        PaymentDto payment = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/reference/{paymentReference}")
    @Operation(summary = "Get payment by reference", description = "Retrieves payment details by reference")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentByReference(
            @Parameter(description = "Payment reference") @PathVariable String paymentReference) {
        PaymentDto payment = paymentService.getPaymentByReference(paymentReference);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> getPaymentById(
            @Parameter(description = "Payment ID") @PathVariable Long id) {
        PaymentDto payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/my")
    @Operation(summary = "Get user payments", description = "Retrieves payments for the authenticated user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<PaymentDto>> getUserPayments(
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
        Page<PaymentDto> payments = paymentService.getUserPayments(userId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    @Operation(summary = "Get all payments", description = "Retrieves all payments (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentDto>> getAllPayments(
            @Parameter(description = "Page number (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (default: createdAt)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (default: desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Page<PaymentDto> payments = paymentService.getAllPayments(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/search")
    @Operation(summary = "Search payments", description = "Advanced payment search with filters")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentDto>> searchPayments(@Valid @RequestBody PaymentDto.PaymentSearchRequest searchRequest) {
        Page<PaymentDto> payments = paymentService.searchPayments(searchRequest);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/refund/{paymentReference}")
    @Operation(summary = "Refund payment", description="Processes a refund for a payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentDto> refundPayment(
            @Parameter(description = "Payment reference") @PathVariable String paymentReference,
            @Valid @RequestBody PaymentDto.RefundRequest request) {
        log.info("Processing refund for payment: {}", paymentReference);
        PaymentDto payment = paymentService.refundPayment(paymentReference, request);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/count")
    @Operation(summary = "Get payment count by status", description = "Returns count of payments by status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getPaymentCountByStatus(
            @Parameter(description = "Payment status") @RequestParam PaymentDto.PaymentStatus status) {
        // This would need to be implemented in PaymentService
        return ResponseEntity.ok(0L);
    }
}
