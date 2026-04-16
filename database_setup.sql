-- PinkShield Database Setup Script
-- This script creates the database and tables for user management

-- Create Database
CREATE DATABASE IF NOT EXISTS pinkshield_db;
USE pinkshield_db;

SET FOREIGN_KEY_CHECKS = 0;

-- Create Admin Table
DROP TABLE IF EXISTS admin;
CREATE TABLE admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Doctor Table
DROP TABLE IF EXISTS doctor;
CREATE TABLE doctor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    speciality VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create User Table
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Test Data - Admin
INSERT INTO admin (first_name, last_name, email, password)
VALUES ('Admin', 'User', 'admin@pinkshield.com', 'admin123');

-- Insert Test Data - Doctor
INSERT INTO doctor (first_name, last_name, email, password, speciality)
VALUES ('Dr.', 'Smith', 'doctor@pinkshield.com', 'doctor123', 'Cardiology');

-- Insert Test Data - User
INSERT INTO user (full_name, email, password, phone, address)
VALUES ('John Doe', 'patient@pinkshield.com', 'user123', '555-0123', '123 Main Street');

-- Blog Posts
DROP TABLE IF EXISTS blog_post;
CREATE TABLE blog_post (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    author_role VARCHAR(50) DEFAULT 'user',
    image_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comments
DROP TABLE IF EXISTS comment;
CREATE TABLE comment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    author_name VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES blog_post(id) ON DELETE CASCADE
);

-- Appointments
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    doctor_name VARCHAR(100) NOT NULL,
    appointment_date DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_doctor_appointment (doctor_id, appointment_date)
);

-- Parapharmacy Products
DROP TABLE IF EXISTS parapharmacy;
CREATE TABLE parapharmacy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(100),
    image_path VARCHAR(500),
    UNIQUE KEY uq_product_name_category (name, category),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Test Data - Blog
INSERT INTO blog_post (title, content, author_name, author_role) 
VALUES ('Heart Health Tips', 'Mantaining a healthy heart is crucial...', 'Dr. Smith', 'doctor');

-- Insert Test Data - Parapharmacy
INSERT INTO parapharmacy (name, description, price, stock, category, image_path) 
VALUES ('Vitamin C', 'High quality vitamin C supplement', 15.50, 20, 'Supplements', '');

-- Verify data
SELECT 'Admin Records:' AS info;
SELECT * FROM admin;

SELECT 'Doctor Records:' AS info;
SELECT * FROM doctor;

SELECT 'User Records:' AS info;
SELECT * FROM user;

SELECT 'Blog Records:' AS info;
SELECT * FROM blog_post;

SELECT 'Parapharmacy Records:' AS info;
SELECT * FROM parapharmacy;

SET FOREIGN_KEY_CHECKS = 1;

