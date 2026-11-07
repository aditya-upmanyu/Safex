# SafeX Guard - Installation Instructions

## ✅ APK Built Successfully!

**Location:** `app\build\outputs\apk\debug\app-debug.apk`  
**Size:** 22.6 MB  
**Build Date:** November 8, 2026, 1:47 AM

---

## 📱 How to Install on Your Android Device

### Method 1: USB Cable (Recommended)

1. **Connect your Android device** to your computer via USB cable

2. **Enable USB Debugging** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times to enable Developer Options
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

3. **Install using ADB** (if Android SDK is installed):
   ```powershell
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```

4. **Or drag and drop**:
   - Open File Explorer
   - Navigate to your phone's storage
   - Copy `app-debug.apk` to your phone's Downloads folder

---

### Method 2: Transfer and Install

1. **Copy the APK file** from:
   ```
   C:\Users\adity\OneDrive\Desktop\SafexRecovery\app\build\outputs\apk\debug\app-debug.apk
   ```

2. **Transfer to phone** using one of these methods:
   - USB cable → copy to Downloads folder
   - Email it to yourself
   - Upload to Google Drive and download on phone
   - Use Bluetooth
   - Use a file transfer app

3. **Install on phone**:
   - Open the APK file from Downloads
   - Allow "Install from Unknown Sources" if prompted
   - Tap "Install"
   - Wait for installation to complete
   - Tap "Open" to launch SafeX Guard

---

### Method 3: Android Studio (If Available)

1. **Open Android Studio**
2. **Open the project** from:
   ```
   C:\Users\adity\OneDrive\Desktop\SafexRecovery
   ```
3. **Connect your device** or start an emulator
4. **Click the Run button** (▶️) or press `Shift + F10`

---

## 🔧 Troubleshooting

### "Install Blocked" Error
- Go to Settings → Security
- Enable "Unknown Sources" or "Install Unknown Apps"
- Try installing again

### "App Not Installed" Error
- Make sure you have enough storage space (at least 50 MB)
- Uninstall any previous version of SafeX Guard
- Restart your phone and try again

### Device Not Detected
- Make sure USB Debugging is enabled
- Try a different USB cable or port
- Install phone drivers if on Windows

---

## 📋 App Features to Test

Once installed, check out these features:

### 🏠 Home Screen
- Device Protected status card
- Quick Actions grid (12 features)
- Recent alerts
- Fraud awareness tips

### 🛡️ Core Defense Engine
- UPI Protection Shield
- Remote Access Sentry
- Digital Arrest Shield
- Family Guardian

### 🔍 URL/QR Scanner
- Paste URL to scan
- **NEW:** QR code scanner button
- Safety analysis results

### 💬 AI Chatbot
- Safety guidance
- Multi-language support (EN/HI/Hinglish)
- Emergency scripts

### 🔐 **NEW: Authentication**
- Login/Signup screen
- Profile setup wizard
- Emergency contacts

### 📱 Other Features
- Call Guard (scam detection)
- Device Security audit
- App Sentry (fake app detection)
- Contact verification
- History tracking
- Report fraud

---

## 🔒 Permissions Required

The app will request these permissions:
- **Phone**: Monitor calls for scam detection
- **Contacts**: Emergency contact management
- **SMS**: Read messages for fraud detection (optional)
- **Camera**: QR code scanning (optional)
- **Accessibility**: App behavior monitoring

---

## 📊 Build Information

**App Name:** SafeX Guard  
**Package:** com.safex.app  
**Version Code:** 1  
**Version Name:** 1.0  
**Min SDK:** 24 (Android 7.0)  
**Target SDK:** 34 (Android 14)  
**Build Type:** Debug  
**Database Version:** 5

---

## 🆘 Need Help?

If you encounter any issues:
1. Check the troubleshooting section above
2. Make sure your Android version is 7.0 or higher
3. Try rebuilding: `.\gradlew clean assembleDebug`
4. Check logcat for errors: `adb logcat | Select-String "SafeX"`

---

**Repository:** https://github.com/aditya-upmanyu/Safex.git  
**Last Build:** November 8, 2026, 1:47 AM  
**Status:** ✅ Ready to Install
