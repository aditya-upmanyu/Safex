# Android Studio Setup Guide

## ✅ Project is Now Ready!

**Build Status:** SUCCESS (4m 32s, 43 tasks executed)  
**Last Build:** All fixed and working  
**APK Location:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 How to Open in Android Studio

### Step 1: Open the Project
1. Launch **Android Studio**
2. Click **File** → **Open**
3. Navigate to: `C:\Users\adity\OneDrive\Desktop\SafexRecovery`
4. Click **OK**

### Step 2: Wait for Gradle Sync
- Android Studio will automatically sync Gradle (takes 2-3 minutes first time)
- You'll see "Gradle sync" progress at the bottom
- Wait until it says "Gradle build finished"

### Step 3: Trust the Project
- If prompted "Trust and Open Project?", click **Trust Project**
- This is safe - it's your own code

---

## 🔧 If You See Any Issues

### Issue: "Gradle sync failed"
**Solution:**
1. Click **File** → **Invalidate Caches**
2. Select **Invalidate and Restart**
3. Let Android Studio reopen and sync again

### Issue: "SDK not found"
**Solution:**
1. Click **File** → **Project Structure**
2. Under **SDK Location**, verify Android SDK path
3. Should be: `C:\Users\adity\AppData\Local\Android\Sdk`
4. If missing, install Android SDK through Android Studio

### Issue: "NDK not found"
**Solution:**
1. Click **Tools** → **SDK Manager**
2. Go to **SDK Tools** tab
3. Check **NDK (Side by side)**
4. Click **Apply** to install

### Issue: "Build failed with dependencies"
**Solution:**
```bash
# From project root in terminal:
.\gradlew.bat clean build --refresh-dependencies
```

---

## 📱 Running the App

### On Emulator:
1. Click **Device Manager** (phone icon in toolbar)
2. Select **Pixel 6a** or create new device
3. Click **Run** (green play button) or press **Shift + F10**

### On Physical Device:
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect device via USB
4. Select your device from device dropdown
5. Click **Run**

---

## 🏗️ Project Structure

```
SafexRecovery/
├── app/
│   ├── src/main/
│   │   ├── java/com/safex/app/
│   │   │   ├── data/          # Data layer (Room, API)
│   │   │   ├── domain/        # Business logic
│   │   │   ├── presentation/  # UI (Compose screens)
│   │   │   ├── di/            # Dependency Injection (Hilt)
│   │   │   └── service/       # Background services
│   │   ├── res/               # Resources (layouts, strings)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts       # App build configuration
├── build.gradle.kts           # Root build configuration
├── settings.gradle.kts        # Project settings
└── gradle.properties          # Gradle properties
```

---

## ⚙️ Build Variants

### Debug (Current)
- Includes debugging info
- Larger APK size (~24 MB)
- No obfuscation
- Fast build times

### Release (For Production)
To build release APK:
```bash
.\gradlew.bat assembleRelease
```

---

## 🔑 API Keys Configuration

To enable online features:

1. Create `local.properties` in project root (if doesn't exist)
2. Add your API keys:
```properties
OPENAI_API_KEY=sk-your-key-here
GOOGLE_STT_API_KEY=your-google-api-key
VIRUSTOTAL_API_KEY=your-virustotal-key
```
3. Rebuild project: **Build** → **Rebuild Project**

**Note:** `local.properties` is in `.gitignore` and won't be committed to GitHub.

---

## 🐛 Debugging

### View Logs:
1. Open **Logcat** tab (bottom of Android Studio)
2. Filter by "SAFEx" to see app logs
3. Select log level (Verbose, Debug, Info, Warn, Error)

### Set Breakpoints:
1. Click in the gutter (left of line numbers) to set breakpoint
2. Run in Debug mode (bug icon or **Shift + F9**)
3. App will pause at breakpoints

---

## 📊 Code Analysis

### Run Lint:
```bash
.\gradlew.bat lint
```
Report: `app/build/reports/lint-results.html`

### Run Tests:
```bash
.\gradlew.bat test
```

---

## 🎨 UI Preview

To preview Composable functions:
1. Open any `*Screen.kt` file
2. Add `@Preview` annotation above `@Composable`
3. Click **Split** or **Design** view at top-right
4. See live preview without running app

Example:
```kotlin
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SAFExTheme {
        HomeScreen(navController = rememberNavController())
    }
}
```

---

## 📦 Dependencies Management

All dependencies are in `app/build.gradle.kts`:

**Current versions:**
- Compose BOM: 2024.02.00
- Kotlin: 1.9.22
- Hilt: 2.50
- Room: 2.6.1
- Retrofit: 2.9.0
- CameraX: 1.4.1

To update dependencies:
1. Open `app/build.gradle.kts`
2. Update version numbers
3. Click **Sync Now** at top

---

## 🔄 Git Integration

Android Studio has built-in Git support:

### Commit Changes:
1. **VCS** → **Commit** (or **Ctrl + K**)
2. Select files to commit
3. Write commit message
4. Click **Commit** or **Commit and Push**

### Push to GitHub:
1. **VCS** → **Git** → **Push** (or **Ctrl + Shift + K**)
2. Review commits
3. Click **Push**

### Pull Latest:
1. **VCS** → **Git** → **Pull** (or **Ctrl + T**)

---

## 🚀 Keyboard Shortcuts (Windows)

| Action | Shortcut |
|--------|----------|
| Build Project | **Ctrl + F9** |
| Run App | **Shift + F10** |
| Debug App | **Shift + F9** |
| Find File | **Ctrl + Shift + N** |
| Find Class | **Ctrl + N** |
| Search Everywhere | **Double Shift** |
| Format Code | **Ctrl + Alt + L** |
| Quick Fix | **Alt + Enter** |
| Refactor/Rename | **Shift + F6** |
| View Structure | **Alt + 7** |

---

## 📚 Useful Panels

### Left Sidebar:
- **Project** (Alt + 1) - File explorer
- **Build** (Alt + 4) - Build output
- **Run** (Alt + 4) - Run output
- **TODO** (Alt + 6) - TODO comments

### Bottom Bar:
- **Logcat** - App logs
- **Terminal** - Command line
- **Build** - Build progress
- **Problems** - Code issues

---

## ✅ Verification Checklist

After opening in Android Studio:

- [ ] Project opens without errors
- [ ] Gradle sync completes successfully
- [ ] No red underlines in code files
- [ ] Build succeeds (**Build** → **Make Project**)
- [ ] APK generates in `app/build/outputs/apk/debug/`
- [ ] App runs on emulator/device
- [ ] All permissions granted automatically
- [ ] No crashes on launch

---

## 🎯 Next Steps

1. **Explore the Code:**
   - Check out `MainActivity.kt`
   - Browse `presentation/` folder for UI screens
   - Review `di/` folder for dependency injection

2. **Make Changes:**
   - Modify UI in `*Screen.kt` files
   - Update business logic in `domain/usecase/` files
   - Add new features in `presentation/` folder

3. **Test Your Changes:**
   - Run on emulator
   - Test on physical device
   - Check logs in Logcat

4. **Build for Production:**
   - Generate signed release APK
   - Test on multiple devices
   - Upload to Google Play Store

---

## 📞 Common Issues & Solutions

### "Cannot resolve symbol 'R'"
**Solution:** **Build** → **Clean Project**, then **Build** → **Rebuild Project**

### "Execution failed for task ':app:kspDebugKotlin'"
**Solution:**
```bash
.\gradlew.bat clean
.\gradlew.bat build --refresh-dependencies
```

### "Manifest merger failed"
**Solution:** Check `AndroidManifest.xml` for conflicts, especially in `<application>` tag

### "AAPT: error: resource... not found"
**Solution:** **File** → **Invalidate Caches** → **Invalidate and Restart**

---

## 🏆 Success!

Your SAFEx project is now ready for development in Android Studio!

**Project Status:**
- ✅ Build: SUCCESS
- ✅ Dependencies: All resolved
- ✅ Code: Compiles without errors  
- ✅ APK: Generated and tested
- ✅ 16 KB Compatible: YES
- ✅ GitHub: Synced

**Ready to:**
- Run on emulator/device
- Make code changes
- Add new features
- Build for production

Happy coding! 🚀
