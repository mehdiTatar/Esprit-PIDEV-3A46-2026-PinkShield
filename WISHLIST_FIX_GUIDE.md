# HOW TO FIX THE WISHLIST DATABASE ERROR

## The Problem
When you try to add items to the wishlist, you get this error:
```
Database needs to be reset, please restart the application
(Unknown column 'parapharmacie_id' in 'field list')
```

This happens because the wishlist table in your database doesn't have the `parapharmacie_id` column or the table structure is incorrect.

## Solution - Three Easy Steps

### Step 1: Open MySQL/PhpMyAdmin
- Go to your database management tool (PHPMyAdmin, MySQL Workbench, or MySQL Command Line)
- Make sure you can connect to `pinkshield_db`

### Step 2: Run the Fix SQL Script
Copy and paste this SQL into your database query editor:

```sql
USE pinkshield_db;

-- Drop the old wishlist table if it exists
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

-- Insert sample data
INSERT INTO wishlist (user_id, parapharmacie_id, added_at) VALUES
(1, 1, NOW()),
(1, 2, NOW()),
(2, 3, NOW());

-- Verify
SELECT * FROM wishlist;
```

### Step 3: Restart the Application
- Close the PinkShield application
- Reopen it
- Try adding an item to the wishlist again

## Expected Result
✅ You should now be able to add items to the wishlist without errors
✅ The item will be saved in the database
✅ You'll see a success message

## If It Still Doesn't Work

### Check if parapharmacie table has data:
```sql
SELECT * FROM parapharmacie;
```

If it's empty, insert test data:
```sql
INSERT INTO parapharmacie (nom, prix, stock, description) VALUES
('Aspirin 500mg', 5.99, 100, 'Pain relief medication'),
('Ibuprofen 200mg', 7.50, 75, 'Anti-inflammatory medication'),
('Paracetamol 500mg', 4.25, 120, 'Fever reducer'),
('Vitamin C 1000mg', 8.99, 100, 'Immune booster'),
('Multivitamin Complex', 12.99, 80, 'Daily supplement'),
('Cough Syrup 200ml', 6.49, 60, 'Cough suppressant');
```

### Verify all tables:
```sql
USE pinkshield_db;
SHOW TABLES;
DESC wishlist;
DESC parapharmacie;
DESC appointment;
```

## Complete Database Reset (Nuclear Option)
If everything is broken, run `setup_database.sql` to reset everything:
```sql
-- This will recreate ALL tables from scratch
-- WARNING: This will delete all data!
```

---

**Need Help?** Check the error message in the application output for more details.

