@echo off
REM Database Setup Script for PinkShield User Management System
REM This script will create the database and import the schema

echo ============================================
echo PinkShield Database Setup
echo ============================================
echo.

REM Check if MySQL is installed
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: MySQL is not found in PATH
    echo Please ensure XAMPP MySQL is running and in your system PATH
    echo.
    echo Quick Fix:
    echo 1. Start XAMPP Control Panel
    echo 2. Click "Start" next to MySQL
    echo 3. Then run this script again
    echo.
    pause
    exit /b 1
)

echo [1] Checking MySQL connection...
mysql -u root -e "SELECT 1;" >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Cannot connect to MySQL
    echo.
    echo Solutions:
    echo 1. Start XAMPP Control Panel and click "Start" for MySQL
    echo 2. If MySQL has a password, edit this script and add: -p"your_password"
    echo 3. Check that MySQL is running on port 3306
    echo.
    pause
    exit /b 1
)

echo [OK] MySQL is running
echo.

echo [2] Creating database and tables...
mysql -u root < database_setup.sql
if %errorlevel% neq 0 (
    echo ERROR: Failed to execute database setup script
    pause
    exit /b 1
)

echo [OK] Database created successfully
echo.

echo [3] Verifying database...
mysql -u root -e "USE pinkshield_db; SELECT COUNT(*) as 'User Count' FROM user; SELECT COUNT(*) as 'Admin Count' FROM admin; SELECT COUNT(*) as 'Doctor Count' FROM doctor;"

echo.
echo ============================================
echo Database Setup Complete!
echo ============================================
echo.
echo Test Credentials:
echo   Admin: admin@pinkshield.com / admin123
echo   Doctor: doctor@pinkshield.com / doctor123
echo   User: patient@pinkshield.com / user123
echo.
echo Next: Run the application with: mvn javafx:run
echo.
pause

