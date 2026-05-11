package com.ecommerce.payment.service;

import com.ecommerce.events.dto.PaymentEvent;
import com.ecommerce.payment.dto.PaymentDto;
import com.ecommerce.payment.entity.*;
import com.ecommerce.payment.exception.PaymentException;
import com.ecommerce.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final PaymentGatewayService paymentGatewayService;

    public PaymentDto processPayment(PaymentDto.ProcessPaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Generate payment reference
        String paymentReference = generatePaymentReference();

        // Create payment record
        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod().name()))
                .status(Payment.PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Process payment through gateway
        PaymentGatewayResponse gatewayResponse = paymentGatewayService.processPayment(request, paymentReference);

        // Update payment based on gateway response
        if (gatewayResponse.isSuccess()) {
            savedPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            savedPayment.setGatewayTransactionId(gatewayResponse.getTransactionId());
            savedPayment.setGatewayResponse(gatewayResponse.getResponseData());
            savedPayment.setProcessedAt(LocalDateTime.now());

            // Create payment transaction
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(savedPayment)
                    .transactionType(PaymentTransaction.TransactionType.PAYMENT)
                    .amount(request.getAmount())
                    .gatewayTransactionId(gatewayResponse.getTransactionId())
                    .gatewayRequest(gatewayResponse.getRequestData())
                    .gatewayResponse(gatewayResponse.getResponseData())
                    .status(PaymentTransaction.TransactionStatus.COMPLETED)
                    .build();
            savedPayment.addTransaction(transaction);

            // Save card details if provided
            if (request.getCardDetails() != null) {
                PaymentCard card = PaymentCard.builder()
                        .cardholderName(request.getCardDetails().getCardholderName())
                        .cardNumberMasked(maskCardNumber(request.getCardDetails().getCardNumber()))
                        .expiryMonth(request.getCardDetails().getExpiryMonth())
                        .expiryYear(request.getCardDetails().getExpiryYear())
                        .cardType(detectCardType(request.getCardDetails().getCardNumber()))
                        .lastFour(request.getCardDetails().getCardNumber().substring(request.getCardDetails().getCardNumber().length() - 4))
                        .build();
                savedPayment.addCard(card);
            }

            // Publish payment completed event
            publishPaymentCompletedEvent(savedPayment);

            log.info("Payment processed successfully: {}", paymentReference);
        } else {
            savedPayment.setStatus(Payment.PaymentStatus.FAILED);
            savedPayment.setGatewayResponse(gatewayResponse.getResponseData());
            savedPayment.setFailureReason(gatewayResponse.getErrorMessage());

            // Create failed transaction
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(savedPayment)
                    .transactionType(PaymentTransaction.TransactionType.PAYMENT)
                    .amount(request.getAmount())
                    .gatewayResponse(gatewayResponse.getResponseData())
                    .status(PaymentTransaction.TransactionStatus.FAILED)
                    .failureReason(gatewayResponse.getErrorMessage())
                    .build();
            savedPayment.addTransaction(transaction);

            // Publish payment failed event
            publishPaymentFailedEvent(savedPayment);

            log.error("Payment processing failed: {} - {}", paymentReference, gatewayResponse.getErrorMessage());
        }

        Payment finalPayment = paymentRepository.save(savedPayment);
        return convertToDto(finalPayment);
    }

    public PaymentDto getPaymentByReference(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentReference));
        return convertToDto(payment);
    }

    public PaymentDto getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentException("Payment not found: " + id));
        return convertToDto(payment);
    }

    public Page<PaymentDto> getUserPayments(Long userId, int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return paymentRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    public Page<PaymentDto> getAllPayments(int page, int size, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return paymentRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public Page<PaymentDto> searchPayments(PaymentDto.PaymentSearchRequest searchRequest) {
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "desc";
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return paymentRepository.searchPayments(
                searchRequest.getUserId(),
                searchRequest.getOrderId(),
                searchRequest.getStatus() != null ? Payment.PaymentStatus.valueOf(searchRequest.getStatus().name()) : null,
                searchRequest.getPaymentMethod() != null ? Payment.PaymentMethod.valueOf(searchRequest.getPaymentMethod()) : null,
                searchRequest.getFromDate(),
                searchRequest.getToDate(),
                pageable
        ).map(this::convertToDto);
    }

    public PaymentDto refundPayment(String paymentReference, PaymentDto.RefundRequest request) {
        log.info("Processing refund for payment: {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentReference));

        if (!payment.canBeRefunded()) {
            throw new PaymentException("Payment cannot be refunded: " + paymentReference);
        }

        BigDecimal refundAmount = request.getRefundAmount() != null ? 
                request.getRefundAmount() : payment.getRefundableAmount();

        if (refundAmount.compareTo(payment.getRefundableAmount()) > 0) {
            throw new PaymentException("Refund amount exceeds refundable amount");
        }

        // Process refund through gateway
        PaymentGatewayResponse gatewayResponse = paymentGatewayService.processRefund(
                payment.getGatewayTransactionId(), refundAmount, paymentReference);

        if (gatewayResponse.isSuccess()) {
            // Update payment refund info
            BigDecimal currentRefundAmount = payment.getRefundAmount() != null ? payment.getRefundAmount() : BigDecimal.ZERO;
            payment.setRefundAmount(currentRefundAmount.add(refundAmount));
            payment.setRefundReason(request.getRefundReason());

            // Update status if fully refunded
            if (payment.getRefundAmount().compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }

            // Create refund transaction
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .payment(payment)
                    .transactionType(PaymentTransaction.TransactionType.REFUND)
                    .amount(refundAmount)
                    .gatewayTransactionId(gatewayResponse.getTransactionId())
                    .gatewayResponse(gatewayResponse.getResponseData())
                    .status(PaymentTransaction.TransactionStatus.COMPLETED)
                    .build();
            payment.addTransaction(transaction);

            // Publish refund event
            publishPaymentRefundedEvent(payment);

            log.info("Refund processed successfully for payment: {}", paymentReference);
        } else {
            throw new PaymentException("Refund failed: " + gatewayResponse.getErrorMessage());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDto(updatedPayment);
    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }

    private String detectCardType(String cardNumber) {
        // Simple card type detection
        if (cardNumber.startsWith("4")) {
            return "VISA";
        } else if (cardNumber.startsWith("5")) {
            return "MASTERCARD";
        } else if (cardNumber.startsWith("3")) {
            return "AMEX";
        } else {
            return "UNKNOWN";
        }
    }

    private void publishPaymentCompletedEvent(Payment payment) {
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PAYMENT_COMPLETED")
                    .paymentReference(payment.getPaymentReference())
                    .orderId(payment.getOrderId())
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .userEmail(payment.getUserEmail())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .paymentMethod(PaymentEvent.PaymentMethod.valueOf(payment.getPaymentMethod().name()))
                    .status(PaymentEvent.PaymentStatus.valueOf(payment.getStatus().name()))
                    .gatewayTransactionId(payment.getGatewayTransactionId())
                    .processedAt(payment.getProcessedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment-events", eventJson);
            
            log.info("Published payment completed event for payment reference: {}", payment.getPaymentReference());
        } catch (JsonProcessingException e) {
            log.error("Error publishing payment completed event", e);
        }
    }

    private void publishPaymentFailedEvent(Payment payment) {
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PAYMENT_FAILED")
                    .paymentReference(payment.getPaymentReference())
                    .orderId(payment.getOrderId())
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .userEmail(payment.getUserEmail())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .paymentMethod(PaymentEvent.PaymentMethod.valueOf(payment.getPaymentMethod().name()))
                    .status(PaymentEvent.PaymentStatus.valueOf(payment.getStatus().name()))
                    .failureReason(payment.getFailureReason())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment-events", eventJson);
            
            log.info("Published payment failed event for payment reference: {}", payment.getPaymentReference());
        } catch (JsonProcessingException e) {
            log.error("Error publishing payment failed event", e);
        }
    }

    private void publishPaymentRefundedEvent(Payment payment) {
        try {
            PaymentEvent event = PaymentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("PAYMENT_REFUNDED")
                    .paymentReference(payment.getPaymentReference())
                    .orderId(payment.getOrderId())
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .userEmail(payment.getUserEmail())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .paymentMethod(PaymentEvent.PaymentMethod.valueOf(payment.getPaymentMethod().name()))
                    .status(PaymentEvent.PaymentStatus.valueOf(payment.getStatus().name()))
                    .refundAmount(payment.getRefundAmount())
                    .refundReason(payment.getRefundReason())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("payment-events", eventJson);
            
            log.info("Published payment refunded event for payment reference: {}", payment.getPaymentReference());
        } catch (JsonProcessingException e) {
            log.error("Error publishing payment refunded event", e);
        }
    }

    private PaymentDto convertToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .orderId(payment.getOrderId())
                .orderNumber(payment.getOrderNumber())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(PaymentDto.PaymentMethod.valueOf(payment.getPaymentMethod().name()))
                .status(PaymentDto.PaymentStatus.valueOf(payment.getStatus().name()))
                .gatewayTransactionId(payment.getGatewayTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .failureReason(payment.getFailureReason())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .processedAt(payment.getProcessedAt())
                .transactions(payment.getTransactions().stream()
                        .map(tx -> PaymentDto.PaymentTransactionDto.builder()
                                .id(tx.getId())
                                .transactionType(PaymentDto.TransactionType.valueOf(tx.getTransactionType().name()))
                                .amount(tx.getAmount())
                                .gatewayTransactionId(tx.getGatewayTransactionId())
                                .status(PaymentDto.TransactionStatus.valueOf(tx.getStatus().name()))
                                .failureReason(tx.getFailureReason())
                                .createdAt(tx.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .cards(payment.getCards().stream()
                        .map(card -> PaymentDto.PaymentCardDto.builder()
                                .id(card.getId())
                                .cardholderName(card.getCardholderName())
                                .cardNumberMasked(card.getCardNumberMasked())
                                .expiryMonth(card.getExpiryMonth())
                                .expiryYear(card.getExpiryYear())
                                .cardType(card.getCardType())
                                .lastFour(card.getLastFour())
                                .isDefault(card.getIsDefault())
                                .createdAt(card.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
