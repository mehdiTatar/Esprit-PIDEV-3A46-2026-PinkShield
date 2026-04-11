@echo off
REM ===== COMPREHENSIVE FIX - Healthcare & Commerce Management System =====

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Healthcare & Commerce Management System
echo ======================================
echo.

REM Set paths
set PROJECT_DIR=C:\Users\driss\IdeaProjects\Projet_java
set TARGET_DIR=%PROJECT_DIR%\target\classes
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources
set M2_REPO=%USERPROFILE%\.m2\repository
set JDK_PATH=C:\Users\driss\.jdks\openjdk-ea-25+36-3489

REM Check if JDK exists
if not exist "%JDK_PATH%" (
    echo ERROR: JDK not found at %JDK_PATH%
    echo Please install JDK 25 or update the path in this script
    pause
    exit /b 1
)

echo Using JDK: %JDK_PATH%
echo Project: %PROJECT_DIR%
echo.

REM Step 1: Clean and create target directory
echo Step 1: Preparing build environment...
if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%" 2>nul
)

REM Step 2: Copy resources
echo Step 2: Copying FXML resources...
if exist "%RESOURCES_DIR%" (
    xcopy "%RESOURCES_DIR%\*.fxml" "%TARGET_DIR%\" /Y /Q 2>nul
    echo Resources copied successfully
) else (
    echo WARNING: Resources directory not found
)

REM Step 3: Build classpath
echo Step 3: Building classpath...
set CLASSPATH=%TARGET_DIR%

REM Add MySQL connector
set MYSQL_JAR=%M2_REPO%\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar
if exist "%MYSQL_JAR%" (
    set CLASSPATH=!CLASSPATH!;%MYSQL_JAR%
    echo MySQL connector found
) else (
    echo WARNING: MySQL connector not found
)

REM Add JavaFX libraries
set JAVAFX_BASE=%M2_REPO%\org\openjfx
set JAVAFX_JARS=%JAVAFX_BASE%\javafx-controls\21.0.2\javafx-controls-21.0.2.jar;%JAVAFX_BASE%\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;%JAVAFX_BASE%\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar;%JAVAFX_BASE%\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;%JAVAFX_BASE%\javafx-base\21.0.2\javafx-base-21.0.2.jar;%JAVAFX_BASE%\javafx-base\21.0.2\javafx-base-21.0.2-win.jar;%JAVAFX_BASE%\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar;%JAVAFX_BASE%\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar

REM Check if JavaFX JARs exist
echo Checking JavaFX dependencies...
set MISSING_JARS=0
for %%j in (javafx-controls-21.0.2.jar javafx-graphics-21.0.2.jar javafx-base-21.0.2.jar javafx-fxml-21.0.2.jar) do (
    if not exist "%JAVAFX_BASE%\%%j\21.0.2\%%j" (
        echo WARNING: %%j not found
        set MISSING_JARS=1
    )
)

if !MISSING_JARS! equ 1 (
    echo.
    echo ERROR: Some JavaFX dependencies are missing!
    echo Please run: mvn dependency:resolve
    echo Or download JavaFX 21.0.2 manually
    pause
    exit /b 1
)

REM Step 4: Compile Java files
echo Step 4: Compiling Java files...
set JAVA_FILES=
for /r "%PROJECT_DIR%\src\main\java" %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

if defined JAVA_FILES (
    echo Compiling !JAVA_FILES!
    "%JDK_PATH%\bin\javac.exe" -cp "%CLASSPATH%;%JAVAFX_JARS%" --module-path "%JAVAFX_BASE%" --add-modules javafx.controls,javafx.fxml -d "%TARGET_DIR%" !JAVA_FILES! 2>compile_errors.log
    if !errorlevel! neq 0 (
        echo.
        echo COMPILATION FAILED! Check compile_errors.log
        type compile_errors.log
        pause
        exit /b 1
    )
    echo Compilation successful
) else (
    echo No Java files found to compile
)

REM Step 5: Run the application
echo.
echo Step 5: Running application...
echo.
echo Classpath: %CLASSPATH%
echo JavaFX JARs: %JAVAFX_JARS%
echo.

"%JDK_PATH%\bin\java.exe" ^
    --module-path "%JAVAFX_BASE%" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "%CLASSPATH%" ^
    -Dfile.encoding=UTF-8 ^
    org.example.SimpleLauncher

if !errorlevel! neq 0 (
    echo.
    echo APPLICATION FAILED TO START!
    echo Exit code: !errorlevel!
    echo.
    echo TROUBLESHOOTING:
    echo 1. Check MySQL is running
    echo 2. Check database 'pinkshield_db' exists
    echo 3. Check tables: appointment, parapharmacie, wishlist
    echo 4. Check JavaFX JARs exist in .m2/repository
    echo.
    pause
)

endlocal
