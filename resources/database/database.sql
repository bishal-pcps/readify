CREATE DATABASE IF NOT EXISTS readify_db;
USE readify_db;

-- Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    loyalty_points INT DEFAULT 0,
    default_address TEXT,
    admin_level VARCHAR(50),
    department VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Books table
CREATE TABLE books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(100),
    publication_date DATE,
    price DECIMAL(10,2),
    stock_level INT DEFAULT 0,
    category VARCHAR(50),
    description TEXT,
    rating DECIMAL(3,2) DEFAULT 0,
    review_count INT DEFAULT 0,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shipping_address TEXT,
    total_amount DECIMAL(10,2),
    status VARCHAR(20),
    FOREIGN KEY (customer_id) REFERENCES users(user_id)
);

-- Order items table
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    book_id INT,
    quantity INT,
    price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- Payments table
CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount DECIMAL(10,2),
    payment_method VARCHAR(50),
    status VARCHAR(20),
    transaction_id VARCHAR(100),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

-- Sample data
INSERT INTO books (title, author, price, category, rating, review_count, stock_level) VALUES
('The Great Classic', 'Jane Austen', 24.99, 'Classic Literature', 4.8, 1250, 50),
('Modern Tales', 'Sarah Johnson', 19.99, 'Contemporary Fiction', 4.6, 892, 35),
('Mystery in the Shadows', 'Robert Blake', 22.99, 'Mystery & Thriller', 4.7, 1045, 28),
('Culinary Delights', 'Chef Mario', 34.50, 'Cooking & Food', 4.9, 530, 15),
('Cosmos', 'Carl Sagan', 18.00, 'Science & Education', 4.9, 2100, 42);

-- Default Admin (Password is 'admin123' hashed with PBKDF2)
-- Salt: k8D7mJ/9zL4= , Hash: R1/Y... (Simulated for this script)
-- Note: In a real app, use the register flow or a migration tool to generate this correctly.
INSERT INTO users (email, password, first_name, last_name, role) VALUES 
('admin@readify.com', 'admin_hash_placeholder', 'System', 'Admin', 'ADMIN');
