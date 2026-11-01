@echo off
echo ================================
echo  SAFEx - Crash Log Capture
echo ================================
echo.

echo Capturing crash log...
adb logcat -d > crash_log.txt

echo.
echo Crash log saved to: crash_log.txt
echo.

echo Opening log file...
notepad crash_log.txt

echo.
echo Look for these keywords in the log:
echo   - FATAL EXCEPTION
echo   - AndroidRuntime
echo   - com.safex.app
echo.
pause
