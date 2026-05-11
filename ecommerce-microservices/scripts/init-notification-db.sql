-- Notification Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS notification_service;
USE notification_service;

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type ENUM('EMAIL', 'SMS', 'PUSH', 'WEBSOCKET') NOT NULL,
    category ENUM('ORDER', 'PAYMENT', 'ACCOUNT', 'MARKETING', 'SYSTEM') NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    content TEXT,
    status ENUM('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    recipient VARCHAR(255) NOT NULL,
    sender VARCHAR(255),
    template_name VARCHAR(100),
    template_data TEXT,
    sent_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL,
    failed_at TIMESTAMP NULL,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    next_retry_at TIMESTAMP NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Notification preferences table
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_enabled BOOLEAN DEFAULT TRUE,
    sms_enabled BOOLEAN DEFAULT FALSE,
    push_enabled BOOLEAN DEFAULT TRUE,
    websocket_enabled BOOLEAN DEFAULT TRUE,
    order_notifications BOOLEAN DEFAULT TRUE,
    payment_notifications BOOLEAN DEFAULT TRUE,
    account_notifications BOOLEAN DEFAULT TRUE,
    marketing_notifications BOOLEAN DEFAULT FALSE,
    system_notifications BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Notification templates table
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    type ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    category ENUM('ORDER', 'PAYMENT', 'ACCOUNT', 'MARKETING', 'SYSTEM') NOT NULL,
    subject VARCHAR(255),
    title VARCHAR(255),
    content TEXT NOT NULL,
    variables TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- WebSocket connections table
CREATE TABLE IF NOT EXISTS websocket_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    connection_id VARCHAR(255) NOT NULL UNIQUE,
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    user_agent TEXT,
    ip_address VARCHAR(45)
);

-- Insert sample notification preferences
INSERT INTO notification_preferences (user_id, email_enabled, sms_enabled, push_enabled, websocket_enabled, order_notifications, payment_notifications, account_notifications, marketing_notifications, system_notifications) VALUES 
(1, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, TRUE),
(2, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, TRUE);

-- Insert sample notification templates
INSERT INTO notification_templates (name, type, category, subject, title, content, variables) VALUES 
('order_confirmation', 'EMAIL', 'ORDER', 'Order Confirmation - {{orderNumber}}', 'Order Confirmation', 
'Dear {{firstName}},\n\nThank you for your order! Your order {{orderNumber}} has been confirmed.\n\nOrder Details:\n{{orderDetails}}\n\nTotal: ${{totalAmount}}\n\nWe will notify you when your order ships.', 
'{{firstName}},{{orderNumber}},{{orderDetails}},{{totalAmount}}'),

('payment_received', 'EMAIL', 'PAYMENT', 'Payment Received - Order {{orderNumber}}', 'Payment Confirmation', 
'Dear {{firstName}},\n\nWe have received your payment of ${{amount}} for order {{orderNumber}}.\n\nPayment Method: {{paymentMethod}}\nTransaction ID: {{transactionId}}\n\nThank you for your purchase!', 
'{{firstName}},{{orderNumber}},{{amount}},{{paymentMethod}},{{transactionId}}'),

('order_shipped', 'EMAIL', 'ORDER', 'Your Order Has Shipped - {{orderNumber}}', 'Order Shipped', 
'Dear {{firstName}},\n\nGreat news! Your order {{orderNumber}} has been shipped.\n\nTracking Number: {{trackingNumber}}\nEstimated Delivery: {{estimatedDelivery}}\n\nYou can track your package at: {{trackingUrl}}', 
'{{firstName}},{{orderNumber}},{{trackingNumber}},{{estimatedDelivery}},{{trackingUrl}}'),

('welcome_email', 'EMAIL', 'ACCOUNT', 'Welcome to E-Commerce Platform!', 'Welcome to E-Commerce', 
'Dear {{firstName}},\n\nWelcome to our e-commerce platform! We are excited to have you as a customer.\n\nYour account has been created successfully. You can now:\n- Browse our products\n- Create wishlists\n- Track orders\n- Manage your account\n\nThank you for joining us!', 
'{{firstName}}');

-- Insert sample notifications
INSERT INTO notifications (user_id, type, category, title, message, status, priority, recipient, reference_type, reference_id) VALUES 
(1, 'EMAIL', 'ORDER', 'Order Confirmation', 'Your order ORD-001 has been confirmed', 'SENT', 'MEDIUM', 'admin@ecommerce.com', 'ORDER', 1),
(2, 'EMAIL', 'ORDER', 'Order Confirmation', 'Your order ORD-002 has been confirmed', 'SENT', 'MEDIUM', 'user@ecommerce.com', 'ORDER', 2),
(1, 'EMAIL', 'ACCOUNT', 'Welcome Email', 'Welcome to our platform!', 'SENT', 'LOW', 'admin@ecommerce.com', 'USER', 1),
(2, 'EMAIL', 'ACCOUNT', 'Welcome Email', 'Welcome to our platform!', 'SENT', 'LOW', 'user@ecommerce.com', 'USER', 2);
