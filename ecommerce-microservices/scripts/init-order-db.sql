-- Order Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS order_service;
USE order_service;

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) DEFAULT 0.00,
    shipping_amount DECIMAL(10, 2) DEFAULT 0.00,
    discount_amount DECIMAL(10, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_method VARCHAR(50),
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    shipping_address TEXT,
    billing_address TEXT,
    tracking_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    shipped_at TIMESTAMP NULL,
    delivered_at TIMESTAMP NULL
);

-- Order items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Order status history table
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Insert sample orders
INSERT INTO orders (order_number, user_id, status, total_amount, subtotal, tax_amount, shipping_amount, payment_method, payment_status, shipping_address) VALUES 
('ORD-001', 1, 'DELIVERED', 1399.98, 1299.99, 99.99, 0.00, 'CREDIT_CARD', 'COMPLETED', '{"street":"123 Main St","city":"New York","state":"NY","zip":"10001","country":"USA"}'),
('ORD-002', 2, 'PROCESSING', 119.98, 109.99, 9.99, 0.00, 'PAYPAL', 'COMPLETED', '{"street":"456 Oak Ave","city":"Los Angeles","state":"CA","zip":"90001","country":"USA"}');

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, product_name, product_sku, quantity, unit_price, total_price) VALUES 
(1, 1, 'Laptop Pro 15"', 'LP001', 1, 1299.99, 1299.99),
(2, 2, 'Wireless Headphones', 'WH001', 1, 199.99, 199.99);

-- Insert order status history
INSERT INTO order_status_history (order_id, status, notes) VALUES 
(1, 'PENDING', 'Order created'),
(1, 'CONFIRMED', 'Payment confirmed'),
(1, 'PROCESSING', 'Order being processed'),
(1, 'SHIPPED', 'Order shipped with tracking number TRK001'),
(1, 'DELIVERED', 'Order delivered successfully'),
(2, 'PENDING', 'Order created'),
(2, 'CONFIRMED', 'Payment confirmed'),
(2, 'PROCESSING', 'Order being processed');
