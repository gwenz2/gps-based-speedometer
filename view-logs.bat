@echo off
echo ========================================
echo  Speedometer App - Live Logs
echo ========================================
echo.
echo Watching for banner ad events...
echo Press Ctrl+C to stop
echo.
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" logcat | findstr /i "MainActivity DragModeActivity SpeedometerApp Banner"
