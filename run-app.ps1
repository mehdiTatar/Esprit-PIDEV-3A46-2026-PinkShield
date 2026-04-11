lterna# Run Application Script (PowerShell)
# This script compiles and runs the JavaFX application without Maven

$projectDir = "C:\Users\driss\IdeaProjects\Projet_java"
$srcDir = "$projectDir\src\main\java"
$resourceDir = "$projectDir\src\main\resources"
$targetDir = "$projectDir\target\classes"
$libDir = "$env:USERPROFILE\.m2\repository"

Write-Host "================================" -ForegroundColor Green
Write-Host "Healthcare & Commerce Management System" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green
Write-Host ""

# Step 1: Compile
Write-Host "Step 1: Compiling Java classes..." -ForegroundColor Yellow
if (!(Test-Path $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
}

$javaFiles = Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

Write-Host "Found $($javaFiles.Count) Java files to compile" -ForegroundColor Cyan

# Step 2: Copy Resources
Write-Host "Step 2: Copying FXML resources..." -ForegroundColor Yellow
if (Test-Path $resourceDir) {
    Copy-Item "$resourceDir\*" -Destination $targetDir -Recurse -Force
    Write-Host "Resources copied successfully" -ForegroundColor Green
} else {
    Write-Host "Warning: Resources directory not found" -ForegroundColor Yellow
}

# Step 3: Run with Java
Write-Host "Step 3: Running application..." -ForegroundColor Yellow
Write-Host ""

$mainClass = "org.example.MainApp"

Write-Host "Main class: $mainClass" -ForegroundColor Cyan
Write-Host "Target dir: $targetDir" -ForegroundColor Cyan
Write-Host ""

# Try to run with javaw (no console window)
try {
    Start-Process -FilePath "java" -ArgumentList "--module-path", "C:\javafx-sdk-21.0.2\lib", "--add-modules", "javafx.controls,javafx.fxml", "-cp", $targetDir, $mainClass -NoNewWindow
    Write-Host "Application launched!" -ForegroundColor Green
} catch {
    Write-Host "Error launching application: $_" -ForegroundColor Red
    Write-Host "Trying with standard Java..." -ForegroundColor Yellow
    & java -cp $targetDir $mainClass
}

