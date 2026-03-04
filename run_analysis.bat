@echo off
echo ============================================================
echo PHPSTAN LEVEL 0 (Before - Baseline)
echo ============================================================
call vendor\bin\phpstan.bat analyse src --level=0 --no-progress 2>&1
echo.
echo ============================================================
echo PHPSTAN LEVEL 5 (After - Strict)
echo ============================================================
call vendor\bin\phpstan.bat analyse src --level=5 --no-progress 2>&1
echo.
echo ============================================================
echo PHPUNIT TESTS
echo ============================================================
call vendor\bin\phpunit.bat --testdox 2>&1
echo.
echo ============================================================
echo DONE
echo ============================================================
