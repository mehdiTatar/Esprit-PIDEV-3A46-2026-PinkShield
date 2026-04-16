@echo off
REM ============================================
REM PinkShield - Java 17 Installer
REM ============================================
REM This script downloads and installs Java 17

setlocal enabledelayedexpansion

cls
echo.
echo ============================================
echo  Java 17 JDK Installer
echo ============================================
echo.
echo Your system has Java 8, but Java 17+ is required.
echo This script will help you install Java 17.
echo.

REM Check if Java 17 is already installed
echo Checking for Java 17...
java -version 2>&1 | findstr "17" >nul 2>&1
if %errorlevel% equ 0 (
    echo.
    echo ✓ Java 17 is already installed!
    echo.
    java -version
    echo.
    pause
    exit /b 0
)

echo.
echo ✗ Java 17 not found (you have Java 8)
echo.
echo This script will guide you through Java 17 installation.
echo.
echo OPTIONS:
echo 1. Download Java 17 automatically
echo 2. Open browser to download manually
echo 3. Cancel
echo.

set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    echo.
    echo Downloading Java 17...
    echo This is a large file (about 150 MB). Please wait...
    echo.

    set JAVA_URL=https://download.oracle.com/java/17/latest/jdk-17_windows-x64_bin.exe
    set JAVA_FILE=%TEMP%\java-17-installer.exe

    echo Downloading from Oracle...
    powershell -Command "try { $progressPreference = 'silentlyContinue'; Invoke-WebRequest -Uri '%JAVA_URL%' -OutFile '%JAVA_FILE%' -ErrorAction Stop; Write-Host 'Downloaded successfully' } catch { Write-Host 'Download failed. Manual installation required.'; exit 1 }"

    if %errorlevel% equ 0 (
        echo.
        echo Running Java 17 installer...
        start /wait "%JAVA_FILE%"

        echo.
        echo ============================================
        echo  Installation Complete!
        echo ============================================
        echo.
        echo IMPORTANT: Restart your computer!
        echo.
        echo After restart, open Command Prompt and run:
        echo   cd C:\xampp\htdocs\JavaProjSeco
        echo   install_maven.bat
        echo.
        pause
    ) else (
        goto :manual
    )
) else if "%choice%"=="2" (
    goto :manual
) else (
    echo Cancelled.
    pause
    exit /b 1
)

goto :end

:manual
echo.
echo ============================================
echo  Manual Installation
echo ============================================
echo.
echo Please download Java 17 manually:
echo.
echo 1. Go to: https://www.oracle.com/java/technologies/downloads/#java17
echo 2. Download: Windows x64 Installer
echo 3. Run the installer and follow prompts
echo 4. Accept all defaults
echo 5. Restart your computer
echo.
echo After restart, run:
echo   cd C:\xampp\htdocs\JavaProjSeco
echo   install_maven.bat
echo.
pause

:end
endlocal

