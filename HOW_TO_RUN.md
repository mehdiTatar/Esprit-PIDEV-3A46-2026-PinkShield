# ⚡ QUICK START - RUN THE APPLICATION

## EASIEST METHOD: Use IntelliJ IDEA

### Step 1: Open the Project
- Your project is already open in IntelliJ
- Let the IDE index everything (wait for green checkmark at bottom)

### Step 2: Run MainApp
1. In the IDE, find `MainApp.java` in the left panel
   - `src/main/java/org/example/MainApp.java`

2. Right-click on `MainApp.java`

3. Select **"Run 'MainApp.main()'"** (green play button)

4. **That's it!** The application will launch in seconds

---

## WHAT TO EXPECT

When the app runs, you should see:

```
Window Title: "Healthcare & Commerce Management System"

Three Tabs:
  ✓ Appointments tab (with form + table)
  ✓ Parapharmacie tab (with form + table)
  ✓ Wishlist tab (with form + table)
```

---

## IF IT DOESN'T WORK IMMEDIATELY

### Issue 1: "Cannot find database"
**Fix:** Make sure MySQL is running with `pinkshield_db` database created

### Issue 2: FXML files not found
**Fix:** IntelliJ will automatically compile resources. If it doesn't:
1. Right-click on project → "Build" → "Build Project"
2. Then try running again

### Issue 3: ClassNotFoundException
**Fix:**
1. Click `File` → `Invalidate Caches` → Click "Invalidate"
2. Wait for IntelliJ to restart and reindex
3. Try again

---

## TESTING THE APP

Once running, follow this:

### 📋 Test Appointments
1. Click "Appointments" tab
2. Fill in:
   - Patient Email: `john@test.com`
   - Patient Name: `John Doe`
   - Doctor Email: `dr.smith@clinic.com`
   - Doctor Name: `Dr. Smith`
   - Date: Today
   - Time: `14:30`
   - Status: pending
   - Notes: Test
3. Click **Ajouter** button
4. ✅ Appointment should appear in table below

### 💊 Test Parapharmacie
1. Click "Parapharmacie" tab
2. Fill in:
   - Product Name: `Aspirin`
   - Price: `5.99`
   - Stock: `100`
3. Click **Ajouter** button
4. ✅ Product should appear in table

### ❤️ Test Wishlist
1. Click "Wishlist" tab
2. Fill in:
   - User ID: `1`
   - Product ID: `1` (must exist from Parapharmacie)
3. Click **Ajouter** button
4. ✅ Wishlist item should appear in table

### 🔍 Test Search
1. In any tab, type in the search bar
2. ✅ Table should filter in real-time

### 🗑️ Test Delete
1. Click on any row in any table
2. Click **Supprimer** button
3. ✅ Item should be deleted

---

## IF YOU STILL WANT TO USE COMMAND LINE

Install Maven manually by following the IDE prompts, or download from:
https://maven.apache.org/download.cgi

Then extract to: `C:\apache-maven-3.9.5`

Add to PATH:
1. Windows Search: "Environment Variables"
2. Click "Edit the system environment variables"
3. Click "Environment Variables" button
4. Under "System variables", click "New"
5. Variable name: `MAVEN_HOME`
6. Variable value: `C:\apache-maven-3.9.5`
7. Click OK
8. Find "Path" in System variables, click Edit
9. Click "New"
10. Add: `%MAVEN_HOME%\bin`
11. Click OK and restart PowerShell

Then run: `mvn clean javafx:run`

---

## SIMPLEST APPROACH (RECOMMENDED)

**Just use the IDE's Run button - it's built for this!**

Trust me, it's the easiest way. 😊

