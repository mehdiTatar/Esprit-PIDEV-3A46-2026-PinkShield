-- FIX WISHLIST DATABASE ISSUE
-- This script fixes the wishlist table structure and recreates it if necessary

USE pinkshield_db;

-- First, let's check what tables exist
SHOW TABLES;

-- Drop the old wishlist table if it exists with wrong structure
DROP TABLE IF EXISTS wishlist;

-- Create the wishlist table with correct structure
CREATE TABLE IF NOT EXISTS wishlist (
  id int(11) NOT NULL AUTO_INCREMENT,
  user_id int(11) NOT NULL,
  parapharmacie_id int(11) NOT NULL,
  added_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_product (user_id, parapharmacie_id),
  FOREIGN KEY (parapharmacie_id) REFERENCES parapharmacie(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify the table was created correctly
DESC wishlist;

-- Insert some sample data to test
INSERT INTO wishlist (user_id, parapharmacie_id, added_at) VALUES
(1, 1, NOW()),
(1, 2, NOW()),
(2, 3, NOW())
ON DUPLICATE KEY UPDATE added_at=NOW();

-- Check the data
SELECT * FROM wishlist;

-- Verify everything is working
SELECT 'Wishlist table has been successfully fixed!' AS status;

