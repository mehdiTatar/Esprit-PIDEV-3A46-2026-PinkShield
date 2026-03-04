-- Seed users for PinkShield
USE pinkshield_db;

-- Clear existing users (optional, but good for a fresh start)
-- DELETE FROM `user`;
-- DELETE FROM `doctor`;
-- DELETE FROM `admin`;

-- Insert Admin
INSERT INTO `admin` (`email`, `roles`, `password`) 
VALUES ('admin@pinkshield.com', '["ROLE_ADMIN"]', '$2y$10$j/stWNQh09zqqYZBJjze8e/9veZz.DYB3yU3azjYXwgjQMWzGPtNS')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- Insert Doctor
INSERT INTO `doctor` (`email`, `roles`, `password`, `full_name`, `speciality`) 
VALUES ('doctor@pinkshield.com', '["ROLE_DOCTOR"]', '$2y$10$j/stWNQh09zqqYZBJjze8e/9veZz.DYB3yU3azjYXwgjQMWzGPtNS', 'Dr. Smith', 'Cardiology')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- Insert Patient
INSERT INTO `user` (`email`, `roles`, `password`, `full_name`, `phone`) 
VALUES ('patient@pinkshield.com', '["ROLE_USER"]', '$2y$10$j/stWNQh09zqqYZBJjze8e/9veZz.DYB3yU3azjYXwgjQMWzGPtNS', 'John Doe', '123456789')
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);
