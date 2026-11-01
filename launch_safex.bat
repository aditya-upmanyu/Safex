@echo off
setlocal
set ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe

echo ================================
echo  SAFEx Quick Launcher
echo ================================
echo.

echo Checking emulator status...
"%ADB%" devices
echo.

echo Launching SAFEx app...
"%ADB%" shell am start -n com.safex.app/.presentation.MainActivity

echo.
echo Checking if app is running...
for /f %%i in ('"%ADB%" shell pidof com.safex.app') do set APP_PID=%%i

if defined APP_PID (
    echo ✓ App launched successfully! (PID: %APP_PID%)
    echo.
    echo To view live logs:
    echo   adb logcat -s "SAFEx:*"
) else (
    echo ✗ App failed to launch. Check logs:
    echo   .\capture_crash_log.bat
)

echo.
pause
