@echo off
echo ================================
echo  SAFEx - Fresh Install Script
echo ================================
echo.

echo [1/5] Uninstalling old version...
adb uninstall com.safex.app
echo.

echo [2/5] Installing new APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.

echo [3/5] Granting permissions...
adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
adb shell pm grant com.safex.app android.permission.CAMERA
adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
adb shell pm grant com.safex.app android.permission.SEND_SMS
adb shell pm grant com.safex.app android.permission.READ_SMS
echo.

echo [4/5] Launching app...
adb shell am start -n com.safex.app/.presentation.MainActivity
echo.

echo [5/5] Done!
echo ================================
echo  Installation Complete!
echo ================================
echo.
echo The app should now be running on your emulator.
echo If it crashes, check CRASH_FIX_GUIDE.md for solutions.
echo.
pause
