@echo off
echo.
echo Checking for vendor/scheb...
if exist c:\xampp\htdocs\Pink\vendor\scheb (
    echo [FOUND] vendor/scheb exists.
) else (
    echo [MISSING] vendor/scheb does not exist.
)
echo.
echo Attempting composer install in debug mode...
composer install --no-interaction --no-scripts --no-plugins -vvv
