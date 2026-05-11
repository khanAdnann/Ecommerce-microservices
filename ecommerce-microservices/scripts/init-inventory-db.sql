-- Inventory Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS inventory_service;
USE inventory_service;

-- Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    quantity_on_order INT NOT NULL DEFAULT 0,
    reorder_level INT DEFAULT 10,
    reorder_quantity INT DEFAULT 50,
    max_stock_level INT DEFAULT 1000,
    warehouse_location VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory movements table
CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type ENUM('STOCK_IN', 'STOCK_OUT', 'RESERVATION', 'UNRESERVATION', 'ADJUSTMENT', 'RETURN') NOT NULL,
    quantity INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reason VARCHAR(255),
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES inventory(product_id) ON DELETE CASCADE
);

-- Low stock alerts table
CREATE TABLE IF NOT EXISTS low_stock_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    current_quantity INT NOT NULL,
    reorder_level INT NOT NULL,
    alert_status ENUM('ACTIVE', 'RESOLVED', 'IGNORED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (product_id) REFERENCES inventory(product_id) ON DELETE CASCADE
);

-- Insert sample inventory data
INSERT INTO inventory (product_id, sku, quantity_available, quantity_reserved, reorder_level, reorder_quantity, warehouse_location) VALUES 
(1, 'LP001', 50, 5, 10, 25, 'WH-A-01-01'),
(2, 'WH001', 100, 10, 20, 50, 'WH-A-01-02'),
(3, 'TS001', 200, 15, 50, 100, 'WH-B-02-01'),
(4, 'RS001', 75, 8, 15, 40, 'WH-B-02-02'),
(5, 'CM001', 30, 3, 10, 20, 'WH-C-03-01');

-- Insert sample inventory movements
INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_type, reference_id, reason, created_by) VALUES 
(1, 'STOCK_IN', 100, 'PURCHASE', 1, 'Initial stock', 'system'),
(1, 'RESERVATION', 5, 'ORDER', 1, 'Order reservation', 'system'),
(2, 'STOCK_IN', 150, 'PURCHASE', 2, 'Initial stock', 'system'),
(2, 'RESERVATION', 10, 'ORDER', 2, 'Order reservation', 'system'),
(3, 'STOCK_IN', 250, 'PURCHASE', 3, 'Initial stock', 'system'),
(3, 'RESERVATION', 15, 'ORDER', 3, 'Order reservation', 'system'),
(4, 'STOCK_IN', 100, 'PURCHASE', 4, 'Initial stock', 'system'),
(4, 'RESERVATION', 8, 'ORDER', 4, 'Order reservation', 'system'),
(5, 'STOCK_IN', 50, 'PURCHASE', 5, 'Initial stock', 'system'),
(5, 'RESERVATION', 3, 'ORDER', 5, 'Order reservation', 'system');

-- Insert low stock alerts for products with low stock
INSERT INTO low_stock_alerts (product_id, current_quantity, reorder_level) VALUES 
(5, 30, 10);
