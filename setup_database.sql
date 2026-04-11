-- Quick Database Setup for Healthcare & Commerce Management System
-- Run this in MySQL to create the required database and tables

-- Create database
CREATE DATABASE IF NOT EXISTS pinkshield_db;
USE pinkshield_db;

-- Create appointment table
CREATE TABLE IF NOT EXISTS appointment (
  id int(11) NOT NULL AUTO_INCREMENT,
  patient_email varchar(255) NOT NULL,
  patient_name varchar(255) NOT NULL,
  doctor_email varchar(255) NOT NULL,
  doctor_name varchar(255) NOT NULL,
  appointment_date datetime NOT NULL,
  status varchar(50) NOT NULL,
  notes text DEFAULT NULL,
  created_at timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create parapharmacie table
CREATE TABLE IF NOT EXISTS parapharmacie (
  id int(11) NOT NULL AUTO_INCREMENT,
  nom varchar(255) NOT NULL,
  prix double NOT NULL,
  stock int(11) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create wishlist table
CREATE TABLE IF NOT EXISTS wishlist (
  id int(11) NOT NULL AUTO_INCREMENT,
  user_id int(11) NOT NULL,
  parapharmacie_id int(11) NOT NULL,
  added_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert some sample data
INSERT INTO parapharmacie (nom, prix, stock, description) VALUES
('Aspirin 500mg', 5.99, 100, 'Pain relief medication'),
('Ibuprofen 200mg', 7.50, 75, 'Anti-inflammatory medication'),
('Paracetamol 500mg', 4.25, 120, 'Fever reducer');

INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes) VALUES
('john@example.com', 'John Doe', 'dr.smith@clinic.com', 'Dr. Smith', '2026-04-15 10:00:00', 'pending', 'Annual checkup'),
('jane@example.com', 'Jane Smith', 'dr.brown@clinic.com', 'Dr. Brown', '2026-04-16 14:30:00', 'confirmed', 'Follow-up visit');

INSERT INTO wishlist (user_id, parapharmacie_id) VALUES
(1, 1),
(1, 2),
(2, 3);

SELECT 'Database setup complete! Tables created and sample data inserted.' AS status;
