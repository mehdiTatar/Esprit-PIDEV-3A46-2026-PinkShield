@echo off
REM ===== JUST WORKS - Healthcare & Commerce Management System =====

echo.
echo ======================================
echo Healthcare & Commerce Management System
echo ======================================
echo LAUNCHING NOW...
echo.

setlocal enabledelayedexpansion

REM Set paths
set PROJECT_DIR=C:\Users\driss\IdeaProjects\Projet_java
set TARGET_DIR=%PROJECT_DIR%\target\classes
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources
set SRC_DIR=%PROJECT_DIR%\src\main\java
set M2_REPO=%USERPROFILE%\.m2\repository
set JDK_HOME=C:\Users\driss\.jdks\openjdk-ea-25+36-3489

REM Step 1: Setup
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"
if exist "%RESOURCES_DIR%" xcopy "%RESOURCES_DIR%\*.fxml" "%TARGET_DIR%\" /Y /Q 2>nul

REM Step 2: Compile all Java files
echo Compiling...
for /r "%SRC_DIR%" %%f in (*.java) do (
    set JAVA_FILES=!JAVA_FILES! "%%f"
)

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

"%JDK_HOME%\bin\javac.exe" -cp "!COMPILE_CP!" -d "%TARGET_DIR%" !JAVA_FILES! 2>nul

REM Step 3: Build runtime classpath
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

REM Step 4: RUN!
echo Starting application...
"%JDK_HOME%\bin\java.exe" ^
    --module-path "%M2_REPO%\org\openjfx\javafx-controls\21.0.2;%M2_REPO%\org\openjfx\javafx-graphics\21.0.2;%M2_REPO%\org\openjfx\javafx-base\21.0.2;%M2_REPO%\org\openjfx\javafx-fxml\21.0.2" ^
    --add-modules javafx.controls,javafx.fxml ^
    -cp "!RUNTIME_CP!" ^
    -Dfile.encoding=UTF-8 ^
    org.example.SimpleLauncher

endlocal
