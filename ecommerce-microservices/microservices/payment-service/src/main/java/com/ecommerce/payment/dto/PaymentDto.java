package com.ecommerce.payment.dto;

import com.ecommerce.payment.entity.Payment;
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
public class PaymentDto {
    
    private Long id;
    private String paymentReference;
    private Long orderId;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;
    
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String failureReason;
    private BigDecimal refundAmount;
    private String refundReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    
    private List<PaymentTransactionDto> transactions;
    private List<PaymentCardDto> cards;

    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, PAYPAL, STRIPE, BANK_TRANSFER, CASH_ON_DELIVERY
    }

    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessPaymentRequest {
        @NotNull(message = "Order ID is required")
        private Long orderId;
        
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;
        
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        private String currency;
        
        @NotNull(message = "Payment method is required")
        private PaymentMethod paymentMethod;
        
        private CardDetails cardDetails;
        private AddressDto billingAddress;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDetails {
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "^\\d{13,19}$", message = "Invalid card number")
        private String cardNumber;
        
        @NotBlank(message = "Cardholder name is required")
        private String cardholderName;
        
        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Invalid expiry month")
        @Max(value = 12, message = "Invalid expiry month")
        private Integer expiryMonth;
        
        @NotNull(message = "Expiry year is required")
        @Min(value = 2023, message = "Invalid expiry year")
        private Integer expiryYear;
        
        @NotBlank(message = "CVV is required")
        @Pattern(regexp = "^\\d{3,4}$", message = "Invalid CVV")
        private String cvv;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequest {
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        private BigDecimal refundAmount;
        
        @NotBlank(message = "Refund reason is required")
        private String refundReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentTransactionDto {
        private Long id;
        private TransactionType transactionType;
        private BigDecimal amount;
        private String gatewayTransactionId;
        private TransactionStatus status;
        private String failureReason;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentCardDto {
        private Long id;
        private String cardholderName;
        private String cardNumberMasked;
        private Integer expiryMonth;
        private Integer expiryYear;
        private String cardType;
        private String lastFour;
        private Boolean isDefault;
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
    public static class PaymentSearchRequest {
        private Long orderId;
        private Long userId;
        private String userEmail;
        private TransactionStatus status;
        private TransactionType transactionType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime fromDate;
        private LocalDateTime toDate;
        private BigDecimal minAmount;
        private BigDecimal maxAmount;
        private String paymentMethod;
        private String paymentReference;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortDirection;
    }

    public enum TransactionType {
        PAYMENT, REFUND, CHARGEBACK, VOID
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}
