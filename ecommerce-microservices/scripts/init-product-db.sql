-- Product Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS product_service;
USE product_service;

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    cost_price DECIMAL(10, 2),
    category_id BIGINT,
    brand VARCHAR(100),
    weight DECIMAL(8, 2),
    dimensions VARCHAR(100),
    color VARCHAR(50),
    size VARCHAR(50),
    material VARCHAR(100),
    images TEXT,
    tags TEXT,
    status ENUM('ACTIVE', 'INACTIVE', 'DISCONTINUED') DEFAULT 'ACTIVE',
    featured BOOLEAN DEFAULT FALSE,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Product attributes table
CREATE TABLE IF NOT EXISTS product_attributes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Insert sample categories
INSERT INTO categories (name, description) VALUES 
('Electronics', 'Electronic devices and accessories'),
('Clothing', 'Fashion and apparel'),
('Books', 'Books and educational materials'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports', 'Sports equipment and accessories');

-- Insert sample products
INSERT INTO products (name, description, sku, price, category_id, brand, status) VALUES 
('Laptop Pro 15"', 'High-performance laptop with 15-inch display', 'LP001', 1299.99, 1, 'TechBrand', 'ACTIVE'),
('Wireless Headphones', 'Noise-cancelling wireless headphones', 'WH001', 199.99, 1, 'AudioTech', 'ACTIVE'),
('Cotton T-Shirt', 'Comfortable 100% cotton t-shirt', 'TS001', 29.99, 2, 'FashionCo', 'ACTIVE'),
('Running Shoes', 'Professional running shoes', 'RS001', 89.99, 5, 'SportGear', 'ACTIVE'),
('Coffee Maker', 'Automatic coffee maker with timer', 'CM001', 79.99, 4, 'HomeTech', 'ACTIVE');

-- Insert sample attributes
INSERT INTO product_attributes (product_id, attribute_name, attribute_value) VALUES 
(1, 'Screen Size', '15 inches'),
(1, 'RAM', '16GB'),
(1, 'Storage', '512GB SSD'),
(2, 'Battery Life', '30 hours'),
(2, 'Connectivity', 'Bluetooth 5.0'),
(3, 'Material', '100% Cotton'),
(3, 'Size', 'Large'),
(4, 'Size', '10'),
(4, 'Color', 'Black'),
(5, 'Capacity', '12 cups'),
(5, 'Power', '1200W');
