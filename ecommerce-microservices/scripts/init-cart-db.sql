-- Cart Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS cart_service;
USE cart_service;

-- Shopping carts table
CREATE TABLE IF NOT EXISTS shopping_carts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    session_id VARCHAR(255),
    status ENUM('ACTIVE', 'ABANDONED', 'CHECKED_OUT') DEFAULT 'ACTIVE',
    total_amount DECIMAL(10, 2) DEFAULT 0.00,
    total_items INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL
);

-- Cart items table
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_id, product_id)
);

-- Cart activity log table
CREATE TABLE IF NOT EXISTS cart_activity_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    activity_type ENUM('CREATED', 'ITEM_ADDED', 'ITEM_UPDATED', 'ITEM_REMOVED', 'CART_ABANDONED', 'CHECKED_OUT') NOT NULL,
    product_id BIGINT,
    quantity INT,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_id) REFERENCES shopping_carts(id) ON DELETE CASCADE
);

-- Insert sample carts
INSERT INTO shopping_carts (user_id, status, total_amount, total_items) VALUES 
(1, 'ACTIVE', 0.00, 0),
(2, 'ACTIVE', 0.00, 0);

-- Insert sample cart items
INSERT INTO cart_items (cart_id, product_id, product_name, product_sku, quantity, unit_price, total_price) VALUES 
(2, 3, 'Cotton T-Shirt', 'TS001', 2, 29.99, 59.98),
(2, 4, 'Running Shoes', 'RS001', 1, 89.99, 89.99);

-- Update cart totals
UPDATE shopping_carts SET total_amount = 149.97, total_items = 3 WHERE user_id = 2;

-- Insert cart activity logs
INSERT INTO cart_activity_log (cart_id, activity_type, product_id, quantity, details) VALUES 
(1, 'CREATED', NULL, NULL, 'Cart created for user'),
(2, 'CREATED', NULL, NULL, 'Cart created for user'),
(2, 'ITEM_ADDED', 3, 2, 'Added 2 x Cotton T-Shirt'),
(2, 'ITEM_ADDED', 4, 1, 'Added 1 x Running Shoes');
