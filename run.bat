@echo off
REM PinkShield User Management System - Quick Start Batch Script
REM This script will run the application with Maven

echo ============================================
echo JavaFX User Management System
echo ============================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [1] Cleaning project...
call mvn clean

echo [2] Building project...
call mvn compile

echo [3] Running application...
echo.
echo Starting JavaFX application...
call mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Application failed to run
    echo See JAVAFX_RUN_CONFIGURATION.md for troubleshooting
)

pause

