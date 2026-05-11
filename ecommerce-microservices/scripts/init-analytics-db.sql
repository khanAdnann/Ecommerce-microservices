-- Analytics Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS analytics_service;
USE analytics_service;

-- Analytics data table (for the new AnalyticsData entity)
CREATE TABLE IF NOT EXISTS analytics_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_type VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15, 4) NOT NULL,
    metric_date TIMESTAMP NOT NULL,
    dimensions JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_metric_type (metric_type),
    INDEX idx_metric_date (metric_date),
    INDEX idx_type_date (metric_type, metric_date)
);

-- User activity logs table
CREATE TABLE IF NOT EXISTS user_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    session_id VARCHAR(255),
    activity_type ENUM('LOGIN', 'LOGOUT', 'PAGE_VIEW', 'PRODUCT_VIEW', 'SEARCH', 'ADD_TO_CART', 'REMOVE_FROM_CART', 'CHECKOUT', 'PURCHASE') NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    referrer VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_activity_type (activity_type),
    INDEX idx_created_at (created_at)
);

-- Product analytics table
CREATE TABLE IF NOT EXISTS product_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    date DATE NOT NULL,
    views_count INT DEFAULT 0,
    unique_views_count INT DEFAULT 0,
    add_to_cart_count INT DEFAULT 0,
    purchase_count INT DEFAULT 0,
    revenue DECIMAL(10, 2) DEFAULT 0.00,
    conversion_rate DECIMAL(5, 4) DEFAULT 0.0000,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_product_date (product_id, date),
    INDEX idx_date (date),
    INDEX idx_product_id (product_id)
);

-- Sales analytics table
CREATE TABLE IF NOT EXISTS sales_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(12, 2) DEFAULT 0.00,
    average_order_value DECIMAL(10, 2) DEFAULT 0.00,
    unique_customers INT DEFAULT 0,
    returning_customers INT DEFAULT 0,
    conversion_rate DECIMAL(5, 4) DEFAULT 0.0000,
    cart_abandonment_rate DECIMAL(5, 4) DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_date (date),
    INDEX idx_date (date)
);

-- Category analytics table
CREATE TABLE IF NOT EXISTS category_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    date DATE NOT NULL,
    views_count INT DEFAULT 0,
    unique_views_count INT DEFAULT 0,
    add_to_cart_count INT DEFAULT 0,
    purchase_count INT DEFAULT 0,
    revenue DECIMAL(10, 2) DEFAULT 0.00,
    product_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_category_date (category_id, date),
    INDEX idx_date (date),
    INDEX idx_category_id (category_id)
);

-- Search analytics table
CREATE TABLE IF NOT EXISTS search_analytics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_term VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    search_count INT DEFAULT 0,
    result_count INT DEFAULT 0,
    click_through_rate DECIMAL(5, 4) DEFAULT 0.0000,
    conversion_rate DECIMAL(5, 4) DEFAULT 0.0000,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_search_date (search_term, date),
    INDEX idx_date (date),
    INDEX idx_search_term (search_term)
);

-- Performance metrics table
CREATE TABLE IF NOT EXISTS performance_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15, 4) NOT NULL,
    metric_unit VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tags TEXT,
    INDEX idx_service_metric (service_name, metric_name),
    INDEX idx_timestamp (timestamp)
);

-- Dashboard configurations table
CREATE TABLE IF NOT EXISTS dashboard_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    name VARCHAR(255) NOT NULL,
    configuration JSON NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample user activity logs
INSERT INTO user_activity_logs (user_id, session_id, activity_type, resource_type, resource_id, details, ip_address, user_agent) VALUES 
(1, 'sess_001', 'LOGIN', NULL, NULL, 'User logged in', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(1, 'sess_001', 'PRODUCT_VIEW', 'PRODUCT', 1, 'Viewed Laptop Pro 15"', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(1, 'sess_001', 'ADD_TO_CART', 'PRODUCT', 1, 'Added Laptop Pro 15" to cart', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(1, 'sess_001', 'PURCHASE', 'ORDER', 1, 'Completed purchase order ORD-001', '192.168.1.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
(2, 'sess_002', 'LOGIN', NULL, NULL, 'User logged in', '192.168.1.2', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'),
(2, 'sess_002', 'SEARCH', NULL, NULL, 'Searched for "running shoes"', '192.168.1.2', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'),
(2, 'sess_002', 'PRODUCT_VIEW', 'PRODUCT', 4, 'Viewed Running Shoes', '192.168.1.2', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36');

-- Insert sample product analytics
INSERT INTO product_analytics (product_id, date, views_count, unique_views_count, add_to_cart_count, purchase_count, revenue, conversion_rate) VALUES 
(1, CURDATE(), 25, 20, 5, 1, 1299.99, 0.0400),
(2, CURDATE(), 18, 15, 3, 0, 0.00, 0.0000),
(3, CURDATE(), 32, 28, 8, 0, 0.00, 0.0000),
(4, CURDATE(), 45, 40, 12, 1, 89.99, 0.0222),
(5, CURDATE(), 15, 12, 2, 0, 0.00, 0.0000);

-- Insert sample sales analytics
INSERT INTO sales_analytics (date, total_orders, total_revenue, average_order_value, unique_customers, returning_customers, conversion_rate) VALUES 
(CURDATE(), 2, 1389.97, 694.99, 2, 0, 0.0250),
(CURDATE() - INTERVAL 1 DAY, 5, 3245.50, 649.10, 4, 1, 0.0320),
(CURDATE() - INTERVAL 2 DAY, 3, 1875.25, 625.08, 3, 0, 0.0280);

-- Insert sample category analytics
INSERT INTO category_analytics (category_id, date, views_count, unique_views_count, add_to_cart_count, purchase_count, revenue, product_count) VALUES 
(1, CURDATE(), 43, 35, 8, 1, 1299.99, 2),
(2, CURDATE(), 32, 28, 8, 0, 0.00, 1),
(5, CURDATE(), 45, 40, 12, 1, 89.99, 1),
(4, CURDATE(), 15, 12, 2, 0, 0.00, 1);

-- Insert sample search analytics
INSERT INTO search_analytics (search_term, date, search_count, result_count, click_through_rate, conversion_rate) VALUES 
('laptop', CURDATE(), 12, 8, 0.7500, 0.0833),
('running shoes', CURDATE(), 8, 5, 0.6250, 0.1250),
('t-shirt', CURDATE(), 15, 10, 0.6667, 0.0000),
('coffee maker', CURDATE(), 6, 4, 0.5000, 0.0000);

-- Insert sample performance metrics
INSERT INTO performance_metrics (service_name, metric_name, metric_value, metric_unit, tags) VALUES 
('user-service', 'response_time', 125.50, 'milliseconds', 'endpoint:/api/users/login'),
('user-service', 'error_rate', 0.02, 'percentage', 'endpoint:/api/users/login'),
('product-service', 'response_time', 89.25, 'milliseconds', 'endpoint:/api/products'),
('product-service', 'error_rate', 0.01, 'percentage', 'endpoint:/api/products'),
('order-service', 'response_time', 156.75, 'milliseconds', 'endpoint:/api/orders'),
('order-service', 'error_rate', 0.03, 'percentage', 'endpoint:/api/orders'),
('kafka', 'messages_per_second', 1250.00, 'count', 'topic:order-events'),
('database', 'connection_pool_usage', 0.75, 'percentage', 'pool:user-service');
