# QUICK FIX FOR WISHLIST ERROR

## 🔴 Error You're Seeing
```
Failed to add to wishlist: Unknown column 'parapharmacie_id' in 'field list'
```

## ✅ Quick Fix (30 seconds)

### For PHPMyAdmin Users:
1. Go to PHPMyAdmin
2. Click on `pinkshield_db` database
3. Click "SQL" tab
4. Copy-paste this code:

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
```

5. Click "Go" or "Execute"
6. Close PHPMyAdmin
7. **Restart the PinkShield app**
8. Try adding to wishlist again ✨

### For MySQL Command Line Users:
```bash
mysql -u root -p pinkshield_db < FIX_WISHLIST_DATABASE.sql
```

Then restart the app.

## 📋 What's Happening?
The wishlist table in your database is missing the `parapharmacie_id` column. The SQL above recreates the table with the correct structure.

## ⚡ After Fix
- ✅ Add items to wishlist works
- ✅ View wishlist works
- ✅ Remove from wishlist works

---

**Still having issues?** Check the error message in the app for more details!

