-- COMPLETE PINKSHIELD DATABASE SETUP
-- Run this to set up your entire database from scratch

DROP DATABASE IF EXISTS pinkshield_db;
CREATE DATABASE pinkshield_db;
USE pinkshield_db;

-- ========================================
-- 1. PARAPHARMACIE TABLE (Products)
-- ========================================
CREATE TABLE parapharmacie (
  id int(11) NOT NULL AUTO_INCREMENT,
  nom varchar(255) NOT NULL,
  prix double NOT NULL,
  stock int(11) NOT NULL,
  description text,
  created_at timestamp DEFAULT current_timestamp(),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample products
INSERT INTO parapharmacie (nom, prix, stock, description) VALUES
('Aspirin 500mg', 5.99, 100, 'Pain relief medication'),
('Ibuprofen 200mg', 7.50, 75, 'Anti-inflammatory medication'),
('Paracetamol 500mg', 4.25, 120, 'Fever reducer'),
('Vitamin C 1000mg', 8.99, 100, 'Immune booster'),
('Multivitamin Complex', 12.99, 80, 'Daily supplement'),
('Cough Syrup 200ml', 6.49, 60, 'Cough suppressant'),
('Bandages Pack 100', 3.99, 200, 'Sterile bandages'),
('First Aid Cream 50ml', 4.50, 150, 'Antiseptic cream'),
('Thermometer Digital', 9.99, 50, 'Fast digital thermometer'),
('Hand Sanitizer 500ml', 2.99, 300, 'Antibacterial sanitizer');

-- ========================================
-- 2. APPOINTMENT TABLE
-- ========================================
CREATE TABLE appointment (
  id int(11) NOT NULL AUTO_INCREMENT,
  patient_email varchar(255) NOT NULL,
  patient_name varchar(255) NOT NULL,
  doctor_email varchar(255) NOT NULL,
  doctor_name varchar(255) NOT NULL,
  appointment_date datetime NOT NULL,
  status varchar(50) NOT NULL DEFAULT 'pending',
  notes text,
  created_at timestamp DEFAULT current_timestamp(),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample appointments
INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes) VALUES
('john@example.com', 'John Doe', 'dr.smith@clinic.com', 'Dr. Smith', '2026-04-20 10:00:00', 'pending', 'Annual checkup'),
('jane@example.com', 'Jane Smith', 'dr.brown@clinic.com', 'Dr. Brown', '2026-04-21 14:30:00', 'confirmed', 'Follow-up visit'),
('bob@example.com', 'Bob Johnson', 'dr.smith@clinic.com', 'Dr. Smith', '2026-04-22 09:00:00', 'pending', 'Consultation');

-- ========================================
-- 3. WISHLIST TABLE (MOST IMPORTANT!)
-- ========================================
CREATE TABLE wishlist (
  id int(11) NOT NULL AUTO_INCREMENT,
  user_id int(11) NOT NULL,
  parapharmacie_id int(11) NOT NULL,
  added_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_product (user_id, parapharmacie_id),
  FOREIGN KEY (parapharmacie_id) REFERENCES parapharmacie(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample wishlist items
INSERT INTO wishlist (user_id, parapharmacie_id, added_at) VALUES
(1, 1, NOW()),
(1, 2, NOW()),
(1, 3, NOW()),
(2, 4, NOW()),
(2, 5, NOW());

-- ========================================
-- 4. VERIFICATION QUERIES
-- ========================================
SELECT 'Database setup complete!' AS status;
SELECT 'TABLES CREATED:' AS info;
SHOW TABLES;

SELECT '--- Parapharmacie Table ---' AS info;
DESC parapharmacie;
SELECT COUNT(*) as product_count FROM parapharmacie;

SELECT '--- Appointment Table ---' AS info;
DESC appointment;
SELECT COUNT(*) as appointment_count FROM appointment;

SELECT '--- Wishlist Table ---' AS info;
DESC wishlist;
SELECT COUNT(*) as wishlist_count FROM wishlist;

-- ========================================
-- All Done! Your database is ready to use
-- ========================================

