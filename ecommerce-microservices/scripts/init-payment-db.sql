-- Payment Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS payment_service;
USE payment_service;

-- Payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_reference VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'STRIPE', 'BANK_TRANSFER', 'CASH_ON_DELIVERY') NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED', 'PARTIALLY_REFUNDED') DEFAULT 'PENDING',
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,
    failure_reason TEXT,
    refund_amount DECIMAL(10, 2) DEFAULT 0.00,
    refund_reason TEXT,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Payment cards table
CREATE TABLE IF NOT EXISTS payment_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    card_type VARCHAR(50) NOT NULL,
    last_four_digits VARCHAR(4) NOT NULL,
    expiry_month INT NOT NULL,
    expiry_year INT NOT NULL,
    cardholder_name VARCHAR(255) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Payment transactions table
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    transaction_type ENUM('AUTHORIZATION', 'CAPTURE', 'REFUND', 'VOID') NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status ENUM('SUCCESS', 'FAILED', 'PENDING') NOT NULL,
    gateway_transaction_id VARCHAR(255),
    gateway_response TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE CASCADE
);

-- Insert sample payments
INSERT INTO payments (payment_reference, order_id, user_id, amount, payment_method, status, gateway_transaction_id, processed_at) VALUES 
('PAY-001', 1, 1, 1399.98, 'CREDIT_CARD', 'COMPLETED', 'TXN-12345', NOW()),
('PAY-002', 2, 2, 119.98, 'PAYPAL', 'COMPLETED', 'TXN-67890', NOW());

-- Insert sample payment cards
INSERT INTO payment_cards (user_id, card_type, last_four_digits, expiry_month, expiry_year, cardholder_name, is_default) VALUES 
(1, 'VISA', '1234', 12, 2024, 'John Admin', TRUE),
(2, 'MASTERCARD', '5678', 9, 2025, 'Jane User', TRUE);

-- Insert sample transactions
INSERT INTO payment_transactions (payment_id, transaction_type, amount, status, gateway_transaction_id) VALUES 
(1, 'AUTHORIZATION', 1399.98, 'SUCCESS', 'TXN-AUTH-12345'),
(1, 'CAPTURE', 1399.98, 'SUCCESS', 'TXN-CAP-12345'),
(2, 'AUTHORIZATION', 119.98, 'SUCCESS', 'TXN-AUTH-67890'),
(2, 'CAPTURE', 119.98, 'SUCCESS', 'TXN-CAP-67890');
