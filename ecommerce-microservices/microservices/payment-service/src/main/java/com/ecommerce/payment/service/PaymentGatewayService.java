package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayService {

    public PaymentGatewayResponse processPayment(PaymentDto.ProcessPaymentRequest request, String paymentReference) {
        // Simulate payment gateway processing
        log.info("Processing payment through gateway: {} - Amount: {}", paymentReference, request.getAmount());
        
        // Mock successful payment processing
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(true);
        response.setTransactionId("TXN-" + UUID.randomUUID().toString());
        response.setResponseData("{\"status\": \"approved\", \"auth_code\": \"123456\"}");
        response.setRequestData("{\"amount\": " + request.getAmount() + ", \"currency\": \"" + request.getCurrency() + "\"}");
        
        return response;
    }

    public PaymentGatewayResponse processRefund(String gatewayTransactionId, BigDecimal refundAmount, String paymentReference) {
        // Simulate refund processing
        log.info("Processing refund through gateway: {} - Amount: {}", paymentReference, refundAmount);
        
        // Mock successful refund processing
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(true);
        response.setTransactionId("REF-" + UUID.randomUUID().toString());
        response.setResponseData("{\"status\": \"refunded\", \"refund_id\": \"REF-123456\"}");
        
        return response;
    }
}
