# TESTING GUIDE - Healthcare & Commerce Management System

## Overview
You now have a fully integrated JavaFX application with 3 modules:
1. **Appointments** - Healthcare appointment management
2. **Parapharmacie** - Product/pharmaceutical inventory management
3. **Wishlist** - User wishlist for products

---

## BEFORE RUNNING THE APP

### Step 1: Ensure MySQL Database is Ready
```sql
-- Make sure pinkshield_db database exists with these tables:

CREATE TABLE `appointment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_email` varchar(255) NOT NULL,
  `patient_name` varchar(255) NOT NULL,
  `doctor_email` varchar(255) NOT NULL,
  `doctor_name` varchar(255) NOT NULL,
  `appointment_date` datetime NOT NULL,
  `status` varchar(50) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `parapharmacie` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `prix` double NOT NULL,
  `stock` int(11) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `wishlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `parapharmacie_id` int(11) NOT NULL,
  `added_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Step 2: Compile the Project
From your IDE or terminal:
```bash
mvn clean compile
```

### Step 3: Run the Application
Run the `MainApp.java` class:
```bash
mvn javafx:run
```

Or right-click on `MainApp.java` → Run

---

## HOW TO TEST EACH MODULE

### 📋 APPOINTMENTS TAB

**Testing Add Appointment:**
1. Click on the "Appointments" tab
2. Fill in the form:
   - Patient Email: `john@example.com`
   - Patient Name: `John Doe`
   - Doctor Email: `dr.smith@clinic.com`
   - Doctor Name: `Dr. Smith`
   - Date: Select any date
   - Time: `14:30`
   - Status: Select "pending", "confirmed", or "cancelled"
   - Notes: `Annual checkup`
3. Click "Ajouter" button
4. Success! The appointment appears in the table

**Testing Search:**
1. Type in the search bar: `john` or `smith`
2. The table filters in real-time to show matching appointments

**Testing Delete:**
1. Click on any row in the table to select it
2. Click "Supprimer" button
3. The appointment is deleted and table updates

**Testing Validation:**
1. Try to add without filling all fields → Warning Alert
2. Enter invalid email → Warning Alert
3. Enter time in wrong format (e.g., `25:00`) → Error Alert
4. Try to book same doctor at same time → Error Alert (if doctor already booked)

---

### 💊 PARAPHARMACIE TAB

**Testing Add Product:**
1. Click on the "Parapharmacie" tab
2. Fill in the form:
   - Product Name: `Aspirin 500mg`
   - Price: `5.99`
   - Stock: `100`
3. Click "Ajouter" button
4. Success! The product appears in the table

**Testing Search:**
1. Type in the search bar: `aspirin`
2. The table filters to show only matching products

**Testing Delete:**
1. Select a product row from the table
2. Click "Supprimer" button
3. The product is deleted

**Testing Validation:**
1. Try to add duplicate product name → Error Alert
2. Enter non-numeric price → Warning Alert
3. Enter non-integer stock → Warning Alert

---

### ❤️ WISHLIST TAB

**Testing Add to Wishlist:**
1. Click on the "Wishlist" tab
2. Fill in the form:
   - User ID: `1`
   - Product ID: `1` (must exist in Parapharmacie)
3. Click "Ajouter" button
4. Success! The wishlist item appears in the table

**Testing Search:**
1. Type in the search bar: `1`
2. The table filters to show wishlist items with that ID

**Testing Delete:**
1. Select a wishlist item row
2. Click "Supprimer" button
3. The item is removed from wishlist

**Testing Validation:**
1. Try to add same product for same user twice → Error Alert (uniqueness check)
2. Enter non-integer IDs → Warning Alert

---

## KEY FEATURES TO VERIFY

✅ **Database Connectivity:**
- All data persists after closing and reopening the app
- Connection uses plain JDBC with `pinkshield_db` database

✅ **Input Validation:**
- Empty field checks
- Email validation (contains @)
- Numeric type validation (Double for prices, Integer for IDs)
- Try/catch error handling

✅ **Uniqueness Rules:**
- Parapharmacie: Can't add products with same name
- Wishlist: Can't add same product for same user twice

✅ **Real-Time Search:**
- FilteredList updates as you type
- Searches by relevant fields (name for products, IDs for wishlist)

✅ **TableView Operations:**
- Select rows to delete
- Data loads and updates dynamically
- VBox.vgrow allows table to expand

✅ **Error Handling:**
- All exceptions caught
- User-friendly alert messages
- No null pointer exceptions

---

## TROUBLESHOOTING

**If app won't start:**
1. Check MySQL is running
2. Check database tables exist
3. Verify JDBC URL: `jdbc:mysql://localhost:3306/pinkshield_db`
4. Verify credentials: user=root, password=empty

**If search doesn't work:**
- Ensure you're typing in the search box
- Data must be loaded first

**If delete doesn't work:**
- Make sure a row is selected (highlighted in blue)
- Check database permissions

**If add doesn't work:**
- Check all fields are filled
- Check validation errors in alerts
- Verify database connection

---

## TEST DATA TO INSERT

```sql
-- Insert test products
INSERT INTO parapharmacie (nom, prix, stock) VALUES ('Ibuprofen', 3.99, 50);
INSERT INTO parapharmacie (nom, prix, stock) VALUES ('Paracetamol', 2.99, 75);
INSERT INTO parapharmacie (nom, prix, stock) VALUES ('Antibiotics', 8.99, 20);

-- Insert test appointments
INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes)
VALUES ('patient1@test.com', 'Alice Johnson', 'dr.brown@clinic.com', 'Dr. Brown', '2026-04-20 10:00:00', 'pending', 'Initial consultation');

-- Insert test wishlist
INSERT INTO wishlist (user_id, parapharmacie_id) VALUES (1, 1);
INSERT INTO wishlist (user_id, parapharmacie_id) VALUES (2, 2);
```

---

## SUCCESS INDICATORS

When everything works correctly, you should see:
1. ✅ Tabbed interface with 3 tabs (Appointments, Parapharmacie, Wishlist)
2. ✅ Data loads from database on startup
3. ✅ Add/Delete buttons work without errors
4. ✅ Search filters results in real-time
5. ✅ Validation alerts appear for invalid input
6. ✅ No console errors or exceptions

**Congratulations! Your application is fully functional!** 🎉

