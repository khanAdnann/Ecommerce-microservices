package com.ecommerce.payment.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {
    
    private boolean success;
    private String transactionId;
    private String responseData;
    private String requestData;
    private String errorMessage;
}
