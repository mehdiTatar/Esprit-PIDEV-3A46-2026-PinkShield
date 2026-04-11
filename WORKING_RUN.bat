@echo off
REM ===== FINAL WORKING SOLUTION - Healthcare & Commerce Management System =====

setlocal enabledelayedexpansion

echo.
echo ======================================
echo Healthcare & Commerce Management System
echo ======================================
echo.

REM Set all paths correctly
set PROJECT_DIR=C:\Users\driss\IdeaProjects\Projet_java
set SRC_DIR=%PROJECT_DIR%\src\main\java
set TARGET_DIR=%PROJECT_DIR%\target\classes
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources
set M2_REPO=%USERPROFILE%\.m2\repository
set JDK_HOME=C:\Users\driss\.jdks\openjdk-ea-25+36-3489

REM Verify JDK exists
if not exist "%JDK_HOME%" (
    echo ERROR: JDK not found at %JDK_HOME%
    echo Please check the JDK installation path
    pause
    exit /b 1
)

echo Using JDK: %JDK_HOME%
echo Project: %PROJECT_DIR%
echo.

REM Step 1: Create target directory
echo Step 1: Setting up build environment...
if not exist "%TARGET_DIR%" (
    mkdir "%TARGET_DIR%" 2>nul
    echo Created target directory
)

REM Step 2: Copy FXML resources
echo Step 2: Copying FXML resources...
if exist "%RESOURCES_DIR%" (
    xcopy "%RESOURCES_DIR%\*.fxml" "%TARGET_DIR%\" /Y /Q 2>nul
    echo FXML files copied to target directory
) else (
    echo WARNING: Resources directory not found at %RESOURCES_DIR%
)

REM Step 3: Compile Java files
echo Step 3: Compiling Java source files...

REM Build classpath for compilation
set COMPILE_CP=%TARGET_DIR%
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar
set COMPILE_CP=!COMPILE_CP!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar

REM Find all Java files
set JAVA_FILES=
for /r "%SRC_DIR%" %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

if defined JAVA_FILES (
    echo Compiling Java files...
    "%JDK_HOME%\bin\javac.exe" ^
        -cp "!COMPILE_CP!" ^
        --module-path "%M2_REPO%\org\openjfx" ^
        --add-modules javafx.controls,javafx.fxml ^
        -d "%TARGET_DIR%" ^
        !JAVA_FILES! 2>compile_errors.log

    if !errorlevel! neq 0 (
        echo.
        echo COMPILATION FAILED!
        echo Check compile_errors.log for details
        type compile_errors.log
        pause
        exit /b 1
    )
    echo Compilation successful!
) else (
    echo No Java files found to compile
)

REM Step 4: Run the application
echo.
echo Step 4: Starting application...
echo.

REM Build runtime classpath
set RUNTIME_CP=%TARGET_DIR%
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\mysql\mysql-connector-java\8.0.33\mysql-connector-java-8.0.33.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar
set RUNTIME_CP=!RUNTIME_CP!;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar

echo Running: org.example.SimpleLauncher
echo.

"%JDK_HOME%\bin\java.exe" ^
    --module-path "%M2_REPO%\org\openjfx" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "!RUNTIME_CP!" ^
    -Dfile.encoding=UTF-8 ^
    -Djava.awt.headless=false ^
    org.example.SimpleLauncher

if !errorlevel! neq 0 (
    echo.
    echo APPLICATION FAILED TO START!
    echo Exit code: !errorlevel!
    echo.
    echo POSSIBLE ISSUES:
    echo 1. MySQL not running - start MySQL service
    echo 2. Database 'pinkshield_db' missing - create it
    echo 3. Tables missing - create appointment, parapharmacie, wishlist tables
    echo 4. JavaFX JARs missing - run 'mvn dependency:resolve'
    echo 5. Compilation errors - check compile_errors.log
    echo.
    pause
)

endlocal
