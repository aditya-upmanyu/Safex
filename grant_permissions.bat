@echo off
echo ================================
echo  SAFEx - Permission Grant Tool
echo ================================
echo.
echo Granting all required permissions...
echo.

adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
echo [OK] Phone State permission granted

adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
echo [OK] Call Log permission granted

adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
echo [OK] Record Audio permission granted

adb shell pm grant com.safex.app android.permission.CAMERA
echo [OK] Camera permission granted

adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
echo [OK] Notifications permission granted

adb shell pm grant com.safex.app android.permission.SEND_SMS
echo [OK] Send SMS permission granted

adb shell pm grant com.safex.app android.permission.READ_SMS
echo [OK] Read SMS permission granted

echo.
echo ================================
echo  All permissions granted!
echo ================================
echo.
pause
