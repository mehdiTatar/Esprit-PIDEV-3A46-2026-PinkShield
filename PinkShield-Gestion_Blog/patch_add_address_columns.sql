-- Run this in phpMyAdmin on your pinkshield database
-- Adds missing columns required by PinkShield entities
-- Run each statement separately; skip any that error (column may already exist)

-- 1. Add address to user table
ALTER TABLE `user` ADD COLUMN `address` VARCHAR(255) DEFAULT NULL;

-- 2. Add full_name to user table (if you have first_name/last_name, run this after:
--    UPDATE `user` SET full_name = TRIM(CONCAT(COALESCE(first_name,''), ' ', COALESCE(last_name,''))) WHERE full_name IS NULL OR full_name = '')
ALTER TABLE `user` ADD COLUMN `full_name` VARCHAR(255) DEFAULT NULL;

-- 3. Add address to doctor table (run only if doctor table exists)
ALTER TABLE `doctor` ADD COLUMN `address` VARCHAR(255) DEFAULT NULL;

-- 4. Add phone to doctor table
ALTER TABLE `doctor` ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL;
