# SAFEx Testing Guide - Pixel 6a Emulator

## ✅ Current Status
- **Build:** SUCCESS (28.23 MB APK)
- **Installation:** SUCCESS
- **Launch:** SUCCESS (App running, PID: 26436)
- **16 KB Compatibility:** FIXED ✓
- **Permissions:** All granted ✓

## 🚀 Quick Start

### Launch the App
```cmd
launch_safex.bat
```

### Reinstall Fresh Copy
```cmd
install_16kb_fixed.bat
```

### View Live Logs
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" logcat -s "SAFEx:*"
```

## 🧪 Features to Test

### 1. Enhanced Home Screen
**What to test:**
- [ ] Protection status card displays
- [ ] Quick action buttons work (Scan, History, Emergency)
- [ ] Statistics carousel scrolls
- [ ] Recent threats section loads
- [ ] Navigation to other screens works

**Expected behavior:** Material 3 design with smooth animations

---

### 2. Real-time Dashboard
**Navigation:** Home → Dashboard icon
**What to test:**
- [ ] Security score displays (0-100)
- [ ] Animated statistics cards show correct counts
- [ ] Weekly trends chart renders
- [ ] Quick action buttons functional
- [ ] Live threat feed updates

**Expected behavior:** Dashboard loads within 2 seconds, animations smooth

---

### 3. Voice Analysis
**Navigation:** Home → Voice Analysis
**What to test:**
- [ ] Record button starts/stops recording
- [ ] Waveform visualization animates during recording
- [ ] Analysis completes after recording
- [ ] Risk gauge shows score (0-100)
- [ ] Detected patterns list displays
- [ ] Save recording option works

**Expected behavior:** 
- Offline mode: Keyword matching (~80% accurate)
- Online mode: OpenAI enhancement (requires API key)

---

### 4. Emergency SOS
**Navigation:** Home → Emergency SOS
**What to test:**
- [ ] Add emergency contacts (min 1, max 5)
- [ ] SOS button shows pulse animation
- [ ] Hold for 3 seconds triggers alert
- [ ] SMS sent to all contacts
- [ ] Alert saved to history
- [ ] Location included (if permission granted)

**Expected behavior:** Emergency contacts saved locally, SMS requires SIM/network

---

### 5. Contact Verification
**Navigation:** Home → Contacts
**What to test:**
- [ ] Phone contacts list loads
- [ ] Search filter works
- [ ] Trust score displays (0-100)
- [ ] Known fraudster detection
- [ ] Whitelist toggle saves
- [ ] Contact details view works

**Expected behavior:** 
- Green badge (100): Whitelisted
- Yellow badge (50): Unknown
- Red badge (0): Flagged as fraudster

---

### 6. Call History & Analysis
**Navigation:** Home → History
**What to test:**
- [ ] Recent calls list loads
- [ ] Filter by risk level works
- [ ] Call details show analysis
- [ ] Audio playback works (if recorded)
- [ ] Transcript displays (if available)
- [ ] Report fraudster option saves

**Expected behavior:** Calls stored in encrypted SQLCipher database

---

### 7. Real-time Call Detection (CRITICAL)
**What to test:**
- [ ] Enable Accessibility Service: Settings → Accessibility → SAFEx Call Detector
- [ ] Make a test call from another device
- [ ] In-call overlay appears
- [ ] Real-time risk score updates
- [ ] Recording indicator shows
- [ ] Keyword alerts trigger

**Expected behavior:** Live analysis during call, overlay requires SYSTEM_ALERT_WINDOW permission

---

### 8. Scan Features
**Navigation:** Home → Scan button
**What to test:**
- [ ] QR code scanner opens camera
- [ ] URL/text scan processes
- [ ] VirusTotal results display (requires API key)
- [ ] Phishing detection works
- [ ] Save scan to history

**Expected behavior:** 
- Offline: Pattern matching
- Online: VirusTotal API integration

---

## 🔑 API Keys Setup (Optional)

To enable online features, create `local.properties` in project root:

```properties
OPENAI_API_KEY=sk-your-key-here
GOOGLE_STT_API_KEY=your-google-api-key
VIRUSTOTAL_API_KEY=your-virustotal-key
```

Then rebuild:
```cmd
.\gradlew.bat assembleDebug
install_16kb_fixed.bat
```

**Features requiring API keys:**
- OpenAI fraud analysis enhancement
- Google Speech-to-Text transcription
- VirusTotal URL scanning

**Features working offline:**
- Keyword fraud detection (100+ patterns)
- Contact verification
- Call recording & storage
- Emergency SOS
- Dashboard analytics

---

## 🐛 Troubleshooting

### App Crashes on Launch
1. Capture logs:
   ```cmd
   capture_crash_log.bat
   ```
2. Check `crash_log.txt` for error
3. Common fixes:
   - Grant all permissions (script does this)
   - Clear app data: `adb shell pm clear com.safex.app`
   - Reinstall: `install_16kb_fixed.bat`

### Accessibility Service Not Working
1. Open emulator Settings
2. Go to Accessibility → SAFEx Call Detector
3. Enable the service
4. Grant SYSTEM_ALERT_WINDOW permission

### Recording Not Working
1. Check microphone permission granted
2. Ensure emulator has audio input configured
3. Test with built-in Voice Recorder app first

### Database Errors
1. Check if app has storage permission
2. Clear app data and reinstall
3. Check logcat for SQLCipher errors

### Network Features Not Working
1. Verify emulator has internet connection
2. Check API keys in `local.properties`
3. Rebuild after adding keys
4. Check logcat for API errors

---

## 📊 Performance Benchmarks

### Expected Performance
- App launch: < 3 seconds
- Dashboard load: < 2 seconds
- Call analysis: 5-10 seconds (offline), 15-30 seconds (online)
- QR scan: < 1 second
- Database queries: < 500ms

### Memory Usage
- Idle: ~80-120 MB
- Active recording: ~150-200 MB
- Peak (analysis): ~250-300 MB

### APK Size
- Current: 28.23 MB (46% smaller than original 52.07 MB)
- Installed: ~90 MB (includes runtime optimizations)

---

## 🔒 Security Features Verification

### Encryption
- [ ] Database encrypted (AES-256 via SQLCipher)
- [ ] Sensitive data never logged
- [ ] API keys stored securely (BuildConfig)

### Permissions
- [ ] Runtime permissions requested properly
- [ ] Dangerous permissions explained to user
- [ ] Graceful degradation if denied

### Privacy
- [ ] No analytics/tracking
- [ ] Local-first architecture
- [ ] User data never leaves device (unless API calls made)

---

## 📝 Test Results Template

Copy this to track your testing:

```markdown
## Test Session: [DATE]
**Emulator:** Pixel 6a API 33
**APK Version:** 28.23 MB (16KB-fixed)
**Mode:** [Offline/Online]

### Home Screen
- [ ] PASS / FAIL - Notes:

### Dashboard
- [ ] PASS / FAIL - Notes:

### Voice Analysis
- [ ] PASS / FAIL - Notes:

### Emergency SOS
- [ ] PASS / FAIL - Notes:

### Contact Verification
- [ ] PASS / FAIL - Notes:

### Call History
- [ ] PASS / FAIL - Notes:

### Real-time Detection
- [ ] PASS / FAIL - Notes:

### Scan Features
- [ ] PASS / FAIL - Notes:

### Issues Found:
1. 
2. 
3. 

### Overall Status: [PASS/FAIL]
```

---

## 🎯 Critical Test Paths

### Path 1: First Launch Experience
1. Install fresh app
2. Launch app → Onboarding should appear
3. Grant permissions when requested
4. Navigate to Dashboard → Data should populate
5. **Expected:** Smooth onboarding, no crashes

### Path 2: Offline Fraud Detection
1. Disable emulator network
2. Go to Voice Analysis
3. Record sample with fraud keywords ("refund", "verify account", etc.)
4. Wait for analysis
5. **Expected:** Results within 10 seconds, ~80% accuracy

### Path 3: Emergency Flow
1. Go to Emergency SOS
2. Add 1-2 emergency contacts
3. Hold SOS button for 3 seconds
4. **Expected:** Confirmation dialog, SMS sent (if network available)

### Path 4: Real-time Call Analysis
1. Enable Accessibility Service
2. Make incoming test call
3. **Expected:** In-call overlay appears with real-time risk score

---

## 📞 Support

If you encounter issues:
1. Check `CRASH_FIX_GUIDE.md`
2. Review `16KB_PAGE_SIZE_FIX.md`
3. Capture logs: `capture_crash_log.bat`
4. Check logcat: `adb logcat -s "SAFEx:*" "*:E"`

---

**Last Updated:** [Auto-generated during installation]
**Status:** ✅ App running successfully on Pixel 6a (PID: 26436)
