-- Review Service Database Initialization

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS review_service;
USE review_service;

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    review_text TEXT,
    verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_count INT DEFAULT 0,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    moderation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_product_user_review (product_id, user_id)
);

-- Review images table
CREATE TABLE IF NOT EXISTS review_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- Review helpful votes table
CREATE TABLE IF NOT EXISTS review_helpful_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    vote_type ENUM('HELPFUL', 'NOT_HELPFUL') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_review_user_vote (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- Review reports table
CREATE TABLE IF NOT EXISTS review_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reason ENUM('INAPPROPRIATE_CONTENT', 'SPAM', 'FAKE_REVIEW', 'OFF_TOPIC', 'OTHER') NOT NULL,
    description TEXT,
    status ENUM('PENDING', 'REVIEWED', 'RESOLVED', 'DISMISSED') DEFAULT 'PENDING',
    moderator_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- Product rating summary table
CREATE TABLE IF NOT EXISTS product_rating_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    average_rating DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    rating_1_count INT DEFAULT 0,
    rating_2_count INT DEFAULT 0,
    rating_3_count INT DEFAULT 0,
    rating_4_count INT DEFAULT 0,
    rating_5_count INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample reviews
INSERT INTO reviews (product_id, user_id, order_id, rating, title, review_text, verified_purchase, status) VALUES 
(1, 1, 1, 5, 'Excellent Laptop!', 'This laptop exceeded my expectations. Fast performance, great display, and excellent battery life. Highly recommended!', TRUE, 'APPROVED'),
(2, 1, 1, 4, 'Good Headphones', 'Great sound quality and comfortable to wear. The noise cancellation works well. Only downside is the price.', TRUE, 'APPROVED'),
(3, 2, 2, 3, 'Average T-Shirt', 'The t-shirt is okay but the material could be better. It shrunk a bit after washing.', TRUE, 'APPROVED'),
(4, 2, 2, 5, 'Perfect Running Shoes!', 'These are the most comfortable running shoes I have ever owned. Great support and lightweight.', TRUE, 'APPROVED');

-- Insert sample review images
INSERT INTO review_images (review_id, image_url, alt_text) VALUES 
(1, 'https://example.com/images/laptop-review-1.jpg', 'Laptop front view'),
(1, 'https://example.com/images/laptop-review-2.jpg', 'Laptop keyboard'),
(4, 'https://example.com/images/shoes-review-1.jpg', 'Running shoes side view');

-- Insert helpful votes
INSERT INTO review_helpful_votes (review_id, user_id, vote_type) VALUES 
(1, 2, 'HELPFUL'),
(2, 2, 'HELPFUL'),
(4, 1, 'HELPFUL');

-- Calculate and insert rating summaries
INSERT INTO product_rating_summary (product_id, average_rating, total_reviews, rating_1_count, rating_2_count, rating_3_count, rating_4_count, rating_5_count) VALUES 
(1, 5.00, 1, 0, 0, 0, 0, 1),
(2, 4.00, 1, 0, 0, 0, 1, 0),
(3, 3.00, 1, 0, 0, 1, 0, 0),
(4, 5.00, 1, 0, 0, 0, 0, 1);
