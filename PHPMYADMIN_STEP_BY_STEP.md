# PHPMyAdmin Step-by-Step Guide for Wishlist Fix

## Step 1: Open PHPMyAdmin

1. Open your browser
2. Go to: `http://localhost/phpmyadmin`
3. You should see the PHPMyAdmin interface

---

## Step 2: Login (if needed)

- Username: `root`
- Password: (leave blank and click Go)

---

## Step 3: Select Your Database

On the left panel, you'll see a list of databases. Click on `pinkshield_db`

You should see three tables:
- ✓ appointment
- ✓ parapharmacie
- ✓ wishlist

---

## Step 4: Go to SQL Tab

1. Click the **"SQL"** tab at the top of the page
2. You'll see a text box where you can type SQL commands

---

## Step 5: Copy and Paste the Fix SQL

Copy this entire SQL code:

```sql
DROP TABLE IF EXISTS wishlist;

CREATE TABLE IF NOT EXISTS wishlist (
  id int(11) NOT NULL AUTO_INCREMENT,
  user_id int(11) NOT NULL,
  parapharmacie_id int(11) NOT NULL,
  added_at datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (id),
  UNIQUE KEY unique_user_product (user_id, parapharmacie_id),
  FOREIGN KEY (parapharmacie_id) REFERENCES parapharmacie(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO wishlist (user_id, parapharmacie_id) VALUES (1, 1), (1, 2), (2, 3);

SELECT * FROM wishlist;
```

Then:
1. Click in the SQL text box
2. Select all (Ctrl+A)
3. Delete existing text
4. Paste the SQL code (Ctrl+V)
5. Click the **"Go"** button

---

## Step 6: Check the Results

You should see messages like:

```
Query OK, 0 rows affected - DROP TABLE IF EXISTS wishlist
Query OK, 0 rows affected, 1 warning - CREATE TABLE
Query OK, 3 rows affected - INSERT INTO wishlist
```

And a table showing:
```
| id | user_id | parapharmacie_id | added_at            |
|----|---------|------------------|---------------------|
| 1  | 1       | 1                | 2026-04-15 12:30:45 |
| 2  | 1       | 2                | 2026-04-15 12:30:45 |
| 3  | 2       | 3                | 2026-04-15 12:30:45 |
```

If you see this, ✅ **Success!**

---

## Step 7: Restart PinkShield App

1. Close PinkShield completely
2. Reopen PinkShield
3. Navigate to Parapharmacie
4. Click "Add to Wishlist" on any product
5. You should see a success message!

---

## Verification (Optional)

To verify everything worked:

1. In PHPMyAdmin, click SQL tab again
2. Paste this:

```sql
-- Check table structure
DESC wishlist;

-- Check data
SELECT * FROM wishlist;

-- Count total items
SELECT COUNT(*) as total_wishlist_items FROM wishlist;
```

3. Click Go

You should see the table structure with these columns:
- id (INT, PRIMARY KEY)
- user_id (INT)
- parapharmacie_id (INT) ← **THIS IS THE KEY ONE**
- added_at (DATETIME)

---

## Troubleshooting

### Error: "No database selected"
- Click on `pinkshield_db` in the left panel first

### Error: "Syntax Error"
- Check that you copied the entire SQL exactly
- Try copying it again carefully

### No results shown
- Make sure you clicked the "Go" button
- The page may need to refresh

### Still getting "Unknown column 'parapharmacie_id'"
1. Run this query to check the columns:
   ```sql
   SHOW COLUMNS FROM wishlist;
   ```
2. If `parapharmacie_id` doesn't appear, your table didn't get created properly
3. Try the SQL fix again from the beginning

---

## Alternative: MySQL Command Line

If you prefer command line instead of PHPMyAdmin:

1. Open Command Prompt/PowerShell
2. Type: `mysql -u root -p`
3. Press Enter
4. Type your password (usually just press Enter)
5. You should see: `mysql> `
6. Type: `USE pinkshield_db;`
7. Copy-paste the SQL code from Step 5
8. Press Enter

---

## Success Checklist

- ✅ Opened PHPMyAdmin
- ✅ Selected pinkshield_db database
- ✅ Went to SQL tab
- ✅ Pasted and executed the fix SQL
- ✅ Saw success messages
- ✅ Restarted PinkShield
- ✅ Can now add items to wishlist

**You're all set!** 🎉

