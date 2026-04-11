@echo off
REM ===== Healthcare & Commerce Management System Build & Run Script =====

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Healthcare & Commerce Management System
echo ======================================
echo.

REM Set project directories
set PROJECT_DIR=C:\Users\driss\IdeaProjects\Projet_java
set TARGET_DIR=%PROJECT_DIR%\target\classes
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources
set M2_REPO=%USERPROFILE%\.m2\repository

REM Set Java paths
set JAVA_HOME=C:\Users\driss\.jdks\openjdk-ea-25+36-3489
set JAVAFX_SDK=C:\javafx-sdk-21.0.2

if not exist "%JAVA_HOME%" (
    echo Error: Java not found at %JAVA_HOME%
    echo Please install JDK 25 or update the path
    pause
    exit /b 1
)

REM Build classpath
set CLASSPATH=%TARGET_DIR%
set CLASSPATH=!CLASSPATH!;%M2_REPO%\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\com\google\protobuf\protobuf-java\3.21.9\protobuf-java-3.21.9.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar

echo Step 1: Building project...
cd /d %PROJECT_DIR%

REM Try to use Maven if available
where mvn >nul 2>&1
if !errorlevel! equ 0 (
    echo Using Maven to build...
    call mvn clean compile -DskipTests
) else (
    echo Maven not found, skipping build step
    echo Make sure target\classes contains compiled classes
)

echo.
echo Step 2: Ensuring resources are copied...
if exist %RESOURCES_DIR% (
    xcopy "%RESOURCES_DIR%\*" "%TARGET_DIR%" /Y /Q 2>nul
)

echo.
echo Step 3: Running application...
echo.
echo Classpath entries:
echo - Target: %TARGET_DIR%
echo - MySQL: mysql-connector-java-8.0.33.jar
echo - JavaFX: javafx-*.jar files
echo.

REM Run Java with proper module path
"%JAVA_HOME%\bin\java.exe" ^
    --module-path "%JAVAFX_SDK%\lib" ^
    --add-modules javafx.controls,javafx.fxml ^
    -Dfile.encoding=UTF-8 ^
    -cp "%CLASSPATH%" ^
    org.example.MainApp

if !errorlevel! neq 0 (
    echo.
    echo Error running application. Exit code: !errorlevel!
    echo.
    echo Troubleshooting:
    echo 1. Check Java is installed: "%JAVA_HOME%\bin\java.exe" -version
    echo 2. Check project compiled: look in %TARGET_DIR%
    echo 3. Check resources copied: look for .fxml files in %TARGET_DIR%
    echo 4. Check MySQL is running
    pause
)

endlocal

