@echo off
REM PinkShield Application Launcher
REM This script runs the PinkShield JavaFX application with proper JavaFX configuration

setlocal enabledelayedexpansion

REM Set Java Home to OpenJDK-25
set JAVA_HOME=C:\Users\driss\.jdks\openjdk-ea-25+36-3489
set MAVEN_HOME=C:\maven

REM Add to PATH
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

REM Change to project directory
cd /d C:\Users\driss\IdeaProjects\Projet_java

REM Set JavaFX module path
set JAVAFX_PATH=C:\Users\driss\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar

REM Run the application
echo.
echo ==========================================
echo   PinkShield - Healthcare System
echo   Starting application...
echo ==========================================
echo.

"%JAVA_HOME%\bin\java.exe" --enable-native-access=javafx.graphics --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "target\classes;C:\Users\driss\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" org.example.MainApp

endlocal

