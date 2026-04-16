@echo off
REM ============================================
REM PinkShield - Automatic Maven Installer
REM ============================================
REM This script will automatically:
REM 1. Download Maven
REM 2. Install it
REM 3. Add to PATH
REM 4. Verify installation
REM 5. Run the application

setlocal enabledelayedexpansion

cls
echo.
echo ============================================
echo  PinkShield - Maven Installer
echo ============================================
echo.

REM Set Maven version and installation path
set MAVEN_VERSION=3.9.0
set MAVEN_INSTALL_DIR=C:\apache-maven
set MAVEN_HOME=%MAVEN_INSTALL_DIR%\apache-maven-%MAVEN_VERSION%

echo [1/5] Checking if Maven is already installed...
if exist "%MAVEN_HOME%" (
    echo     ✓ Maven found at %MAVEN_HOME%
    goto :verify
) else (
    echo     ✗ Maven not found. Installing...
)

echo.
echo [2/5] Creating installation directory...
if not exist "%MAVEN_INSTALL_DIR%" (
    mkdir "%MAVEN_INSTALL_DIR%"
    echo     ✓ Created %MAVEN_INSTALL_DIR%
) else (
    echo     ✓ Directory already exists
)

echo.
echo [3/5] Downloading Maven 3.9.0...
echo     This will download approximately 9 MB...
echo.
powershell -Command "try { $progressPreference = 'silentlyContinue'; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%MAVEN_INSTALL_DIR%\maven.zip' -ErrorAction Stop; Write-Host '     ✓ Downloaded successfully' } catch { Write-Host '     ✗ Download failed. Please download manually from:'; Write-Host '       https://maven.apache.org/download.cgi'; exit 1 }"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to download Maven
    echo Please download manually from: https://maven.apache.org/download.cgi
    echo Then extract to: %MAVEN_INSTALL_DIR%
    pause
    exit /b 1
)

echo.
echo [4/5] Extracting Maven...
powershell -Command "Expand-Archive -Path '%MAVEN_INSTALL_DIR%\maven.zip' -DestinationPath '%MAVEN_INSTALL_DIR%' -Force; Write-Host '     ✓ Extracted successfully'"

if %errorlevel% neq 0 (
    echo     ✗ Extraction failed
    pause
    exit /b 1
)

REM Clean up zip file
del "%MAVEN_INSTALL_DIR%\maven.zip"
echo     ✓ Cleaned up temporary files

:verify
echo.
echo [5/5] Verifying Maven installation...
call "%MAVEN_HOME%\bin\mvn.cmd" -version >nul 2>&1

if %errorlevel% equ 0 (
    echo     ✓ Maven installed successfully!
    echo.
    echo ============================================
    echo  Maven Installation Complete!
    echo ============================================
    echo.
    for /f "tokens=*" %%i in ('call "%MAVEN_HOME%\bin\mvn.cmd" -version ^| findstr "Apache Maven"') do set VERSION_INFO=%%i
    echo %VERSION_INFO%
    echo.
    echo Maven location: %MAVEN_HOME%
    echo.

    REM Add Maven to PATH
    echo [+] Setting up environment variables...
    setx MAVEN_HOME "%MAVEN_HOME%" >nul 2>&1

    REM Add to PATH if not already present
    for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v Path /f "%MAVEN_HOME%\bin" 2^>nul') do (
        set PATHSET=1
    )

    if not defined PATHSET (
        for /f "tokens=2*" %%A in ('reg query "HKCU\Environment" /v Path') do set CURRENT_PATH=%%B
        setx Path "%CURRENT_PATH%;%MAVEN_HOME%\bin" >nul 2>&1
        echo     ✓ Added Maven to PATH
    ) else (
        echo     ✓ Maven already in PATH
    )

    echo.
    echo ============================================
    echo  Next Steps
    echo ============================================
    echo.
    echo 1. RESTART your computer (important!)
    echo.
    echo 2. After restart, open Command Prompt and run:
    echo    cd C:\xampp\htdocs\JavaProjSeco
    echo    mvn clean javafx:run
    echo.
    echo 3. The PinkShield application will start!
    echo.
    pause

) else (
    echo     ✗ Maven verification failed
    echo.
    echo Please ensure:
    echo 1. Java 17 or higher is installed
    echo 2. JAVA_HOME environment variable is set
    echo 3. Restart your computer after installation
    echo.
    pause
    exit /b 1
)

endlocal

