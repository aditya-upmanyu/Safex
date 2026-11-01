# SAFEx App - Crash Fix Guide

**Date:** November 1, 2026  
**Device:** Pixel 6a Emulator  
**Issue:** App keeps stopping while loading

---

## ✅ Build Status

```
✅ BUILD SUCCESSFUL in 1m 55s
✅ 41 actionable tasks completed
✅ APK Generated: 52.07 MB
✅ Location: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔍 Common Crash Causes & Fixes

### 1. **Missing Permissions** (Most Common)

The app requires several runtime permissions that must be granted:

**Required Permissions:**
- ✅ Phone State (READ_PHONE_STATE)
- ✅ Call Log (READ_CALL_LOG)
- ✅ Record Audio (RECORD_AUDIO)
- ✅ Camera (for QR scanning)
- ✅ Notifications (POST_NOTIFICATIONS)
- ✅ SMS (SEND_SMS, READ_SMS)

**Fix:**
1. Open Settings on emulator
2. Go to Apps → SAFEx → Permissions
3. Grant ALL permissions manually
4. Restart the app

---

### 2. **Emulator Configuration**

**Check Emulator Settings:**
```
Recommended Configuration:
- API Level: 33+ (Android 13+)
- RAM: 4GB minimum
- Storage: 8GB minimum
- Graphics: Hardware - GLES 2.0
```

**Fix:**
1. Open AVD Manager
2. Edit Pixel 6a emulator
3. Increase RAM to 4096 MB
4. Set Graphics to "Hardware - GLES 2.0"
5. Wipe Data and restart

---

### 3. **Database Initialization Issue**

The app uses SQLCipher encrypted database which might fail on first launch.

**Symptoms:**
- Crash immediately after splash screen
- Error: "Failed to open database"

**Fix Method 1 - Clear App Data:**
```bash
adb shell pm clear com.safex.app
```

**Fix Method 2 - Reinstall:**
```bash
adb uninstall com.safex.app
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### 4. **Hilt Dependency Injection**

Hilt might fail to inject dependencies if there are compilation issues.

**Check:**
- All ViewModels have `@HiltViewModel` annotation
- Application class has `@HiltAndroidApp`
- Activity has `@AndroidEntryPoint`

**Verified:**
✅ SAFExApplication.kt - Has @HiltAndroidApp
✅ MainActivity.kt - Has @AndroidEntryPoint  
✅ All ViewModels - Have @HiltViewModel

---

### 5. **Navigation Graph Issue**

**Symptoms:**
- App crashes when trying to navigate
- Error: "Destination not found"

**Fix:**
All screens are properly registered in SAFExNavGraph.kt:
✅ Onboarding
✅ Home (EnhancedHomeScreen)
✅ Dashboard
✅ Emergency SOS
✅ Contact Verification
✅ Voice Analysis
✅ Call Guard
✅ URL Scanner
✅ Device Security
✅ History
✅ Report Fraud
✅ Settings

---

## 🛠️ Step-by-Step Troubleshooting

### Step 1: Install via ADB

```bash
# Navigate to project directory
cd c:\Users\adity\OneDrive\Desktop\SafexRecovery

# Uninstall old version (if exists)
adb uninstall com.safex.app

# Install new APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.safex.app/.presentation.MainActivity
```

### Step 2: Check Logcat for Errors

```bash
# Clear logcat
adb logcat -c

# Monitor app logs
adb logcat | findstr "safex\|AndroidRuntime\|FATAL"
```

### Step 3: Grant All Permissions

```bash
# Grant all permissions via ADB
adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
adb shell pm grant com.safex.app android.permission.CAMERA
adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
adb shell pm grant com.safex.app android.permission.SEND_SMS
adb shell pm grant com.safex.app android.permission.READ_SMS
```

### Step 4: Check Storage Space

```bash
# Check available storage
adb shell df /data

# Should have at least 500MB free
```

---

## 🔧 Quick Fixes

### Fix 1: Cold Boot Emulator

```
1. Close Android Studio
2. Open AVD Manager
3. Click dropdown on Pixel 6a → Cold Boot Now
4. Wait for emulator to fully start
5. Install and run app
```

### Fix 2: Disable Instant Run

```
1. Android Studio → Settings
2. Build, Execution, Deployment → Debugger
3. Uncheck "Enable Instant Run"
4. Rebuild: ./gradlew clean assembleDebug
5. Install fresh APK
```

### Fix 3: Increase Gradle Memory

Add to `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
org.gradle.daemon=true
org.gradle.parallel=true
```

---

## 📱 Testing Checklist

### Before Running on Emulator:

- [ ] Emulator is Pixel 6a or similar (API 33+)
- [ ] Emulator has 4GB+ RAM
- [ ] Graphics set to Hardware
- [ ] Cold booted (not quick boot)
- [ ] Has internet connection
- [ ] No other heavy apps running

### After Installation:

- [ ] Grant all permissions manually
- [ ] Check Settings → Apps → SAFEx → Storage (should show data)
- [ ] Open app from launcher (not from Android Studio)
- [ ] Wait 10 seconds on splash screen
- [ ] Check if onboarding appears

---

## 🐛 Debugging Commands

### Get Crash Stack Trace

```bash
adb logcat -d > crash_log.txt
```

### Monitor Real-time Logs

```bash
adb logcat -s SAFEx:V AndroidRuntime:E
```

### Check if App is Running

```bash
adb shell ps | findstr safex
```

### Force Stop App

```bash
adb shell am force-stop com.safex.app
```

### Clear App Data

```bash
adb shell pm clear com.safex.app
```

---

## ⚠️ Known Issues & Workarounds

### Issue 1: "App keeps stopping" immediately

**Cause:** Missing permissions or database init failure

**Workaround:**
1. Uninstall app
2. Reinstall APK
3. Grant all permissions via ADB (commands above)
4. Launch app

### Issue 2: Black screen after splash

**Cause:** Navigation not initialized or ViewModel crash

**Workaround:**
1. Check logcat for specific error
2. Clear app data
3. Restart emulator
4. Try again

### Issue 3: "Process system isn't responding"

**Cause:** Emulator overloaded

**Workaround:**
1. Close all other apps in emulator
2. Restart emulator with cold boot
3. Increase emulator RAM to 6GB
4. Install app again

---

## 🎯 Most Likely Solutions

### Solution 1: Permission Grant Script (RECOMMENDED)

Create a file `grant_permissions.bat`:

```batch
@echo off
echo Granting all permissions...
adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
adb shell pm grant com.safex.app android.permission.CAMERA
adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
adb shell pm grant com.safex.app android.permission.SEND_SMS
adb shell pm grant com.safex.app android.permission.READ_SMS
echo Done! All permissions granted.
pause
```

Run this script after installing the APK.

### Solution 2: Fresh Install Script

Create a file `fresh_install.bat`:

```batch
@echo off
echo Performing fresh install...
adb uninstall com.safex.app
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
adb shell pm grant com.safex.app android.permission.CAMERA
adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
adb shell pm grant com.safex.app android.permission.SEND_SMS
adb shell pm grant com.safex.app android.permission.READ_SMS
echo Installation complete!
echo Launching app...
adb shell am start -n com.safex.app/.presentation.MainActivity
pause
```

---

## 📊 Crash Log Analysis

If the app still crashes, get the crash log:

```bash
adb logcat -d > crash_log.txt
```

Look for these keywords in crash_log.txt:
- `FATAL EXCEPTION`
- `AndroidRuntime`
- `java.lang.RuntimeException`
- `SQLiteException`
- `NullPointerException`

Common crash patterns:

| Error Message | Cause | Fix |
|---------------|-------|-----|
| `Failed to open database` | SQLCipher initialization | Clear app data, reinstall |
| `Permission denied` | Missing permissions | Grant via ADB or Settings |
| `ClassNotFoundException` | Hilt/DI issue | Clean build, rebuild |
| `ActivityNotFoundException` | Navigation issue | Check manifest registration |
| `OutOfMemoryError` | Emulator RAM | Increase emulator RAM |

---

## ✅ Final Checklist

Before reporting the crash as unfixable:

1. [ ] Emulator meets minimum requirements (API 33+, 4GB RAM)
2. [ ] Cold booted emulator (not quick boot)
3. [ ] Fresh APK install (uninstall first)
4. [ ] All permissions granted via ADB
5. [ ] Logcat captured during crash
6. [ ] Tried on different emulator configuration
7. [ ] Checked available storage space (500MB+)
8. [ ] No other apps causing conflicts

---

## 🚀 Quick Start (For Immediate Testing)

**Option 1: Automated Install & Grant Permissions**

```bash
cd c:\Users\adity\OneDrive\Desktop\SafexRecovery
adb uninstall com.safex.app
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.safex.app android.permission.READ_PHONE_STATE
adb shell pm grant com.safex.app android.permission.READ_CALL_LOG
adb shell pm grant com.safex.app android.permission.RECORD_AUDIO
adb shell pm grant com.safex.app android.permission.CAMERA
adb shell pm grant com.safex.app android.permission.POST_NOTIFICATIONS
adb shell pm grant com.safex.app android.permission.SEND_SMS
adb shell pm grant com.safex.app android.permission.READ_SMS
adb shell am start -n com.safex.app/.presentation.MainActivity
```

**Option 2: Manual Install**

1. Drag `app-debug.apk` to emulator
2. Click "Install" in emulator
3. Open Settings → Apps → SAFEx → Permissions
4. Grant ALL permissions
5. Launch app from home screen

---

## 📞 Still Crashing?

If the app still crashes after following all steps:

1. **Capture the crash log:**
   ```bash
   adb logcat -d > crash_log.txt
   ```

2. **Share the crash log** - Look for lines containing:
   - `FATAL EXCEPTION`
   - `at com.safex.app`
   - Stack trace

3. **Try a different emulator:**
   - Create new Pixel 5 emulator (API 33)
   - Or Pixel 4 emulator (API 31)

4. **Last resort - Disable new features temporarily:**
   - Comment out new ViewModels in navigation
   - Test with basic screens only

---

## 🎉 Success Indicators

App is working correctly when you see:

✅ Onboarding screen appears (or Home if already completed)
✅ No crash dialog
✅ Can navigate between screens
✅ Protection status shows "Protected"
✅ No errors in logcat

---

**Need Help?** Check the logcat output - it will tell you exactly what's wrong!

