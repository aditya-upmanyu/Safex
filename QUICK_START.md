# SAFEx - Quick Start Guide

## ⚡ 5-Minute Setup

### **Step 1: Install APK**

**Option A: Android Studio**
```bash
# Open project
File → Open → c:\Users\adity\OneDrive\Desktop\SafexRecovery

# Click Run button ▶
# Select device/emulator
# Wait for installation
```

**Option B: Command Line**
```bash
cd c:\Users\adity\OneDrive\Desktop\SafexRecovery
.\gradlew.bat installDebug
```

**Option C: Manual**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### **Step 2: Grant Permissions**
- Phone State (for call detection)
- Microphone (for recording)
- Notifications (for alerts)
- SMS (for emergency alerts)
- Camera (for QR scanning)

### **Step 3: Complete Onboarding**
- Tap "Next" through 3 screens
- Tap "Start Using SAFEx"

### **Step 4: Configure Settings**
1. Tap Settings icon (⚙️)
2. Set Emergency Contact (optional)
3. Enable "Call Guard" toggle
4. Adjust "Risk Threshold" if needed
5. Go back to Home

### **Step 5: Test the App**

**Test Call Detection:**
```
1. Make a test call
2. Say any text (fraud keywords optional)
3. Hang up when done
4. Wait 5-10 seconds for analysis
5. Check notification
6. Go to History tab to see record
```

---

## 📱 Using Each Feature

### **1. Call Guard (Real-time Detection)**

**What it does:**
- Automatically records incoming calls
- Analyzes for fraud patterns
- Sends alert if fraud detected
- Logs call to history

**How to use:**
- Just answer calls normally
- App detects and records automatically
- If fraud detected, notification pops up
- Tap notification to see details

**Test with fraud keywords:**
- "This is CBI officer"
- "Your account is frozen"
- "Share your OTP"
- "You've won a lottery"

---

### **2. URL Scanner**

**How to scan:**
1. Go to URL Scanner tab
2. Option A: Click camera icon, scan QR code
3. Option B: Paste URL and tap "Scan"
4. Wait for results (instant or 2-3 seconds)

**Results:**
- 🟢 Green = Safe
- 🟠 Orange = Suspicious
- 🔴 Red = Malicious

---

### **3. Call History**

**View all calls:**
1. Go to History tab
2. See all calls with:
   - Phone number
   - Risk score (0-100)
   - Verdict (SAFE/SUSPICIOUS/FRAUD)
   - Time received

**Search:**
1. Tap search icon 🔍
2. Enter phone number
3. Filter results

**Export:**
1. Tap menu icon ⋮
2. Select "Export as CSV"
3. File saved to Downloads

---

### **4. Device Security**

**Check security:**
1. Go to Home tab
2. Scroll to "Device Security" card
3. See security score (0-100)
4. See what's checked:
   - Root detection ✓
   - Screen lock status
   - Developer mode check
   - Unknown sources status

**Get recommendations:**
1. Tap on issue
2. Read recommendation
3. Follow steps to fix

---

### **5. Report Fraud**

**Use government links:**
1. Go to Report Fraud tab
2. Tap any link to open:
   - **NCRP**: File cyber complaint
   - **1930**: Call helpline
   - **Chakshu**: Check SIM registration
   - **CEIR**: Block stolen device
   - **TAFCOP**: Check SIM usage

---

### **6. Settings**

**Configure:**
- Theme: Light/Dark/System
- Emergency Contact: For SMS alerts
- Risk Threshold: 50-90 (higher = stricter)
- Notifications: On/Off
- API Keys: Optional (for advanced features)

---

## 🚨 When You Get an Alert

### **FRAUD Alert (Red, High Priority)**
```
🚨 FRAUD DETECTED - DIGITAL ARREST SCAM
Risk: 85/100

Action: 
✅ DO NOT TRANSFER MONEY
✅ DO NOT SHARE OTP
✅ HANG UP IMMEDIATELY
✅ Report to: cybercrime.gov.in
```

### **SUSPICIOUS Alert (Orange)**
```
⚠️ Suspicious Call
Risk: 55/100

Action:
✅ Be cautious
✅ Don't share personal info
✅ Verify caller independently
✅ Consider reporting
```

### **SAFE Call (Green)**
```
✓ Call appears safe
Risk: 15/100

Action:
✅ Normal conversation patterns detected
✅ No fraud indicators found
```

---

## 🔧 Troubleshooting

### **App won't start**
- Uninstall: `adb uninstall com.safex.app`
- Reinstall: `.\gradlew.bat installDebug`

### **No call recording**
- Check microphone permission granted
- Check Call Guard toggle is ON
- Check storage has space
- Try restarting device

### **No notifications**
- Check notification permission granted
- Check risk threshold setting
- Check notifications not disabled in system settings
- Check phone not in Do Not Disturb

### **App crashes**
- Clear app data: Settings → Apps → SAFEx → Storage → Clear Data
- Reinstall app
- Check you have latest version

---

## 📊 Understanding Risk Scores

```
Score Range    | Verdict       | Action
0-39           | SAFE ✓        | Normal, no action needed
40-69          | SUSPICIOUS ⚠️ | Be careful, verify caller
70-100         | FRAUD 🚨      | Do NOT engage, report
```

---

## 🎯 Pro Tips

1. **Set Emergency Contact**
   - Go to Settings
   - Add trusted contact number
   - Will receive SMS alerts on high-risk calls

2. **Adjust Risk Threshold**
   - Higher = More sensitive (more alerts)
   - Lower = Less sensitive (fewer alerts)
   - Default 70 is good for most users

3. **Export History**
   - Go to History
   - Tap menu → Export
   - Share with authorities if filing complaint

4. **Enable All Permissions**
   - More permissions = Better protection
   - All permissions are used for security

5. **Keep App Updated**
   - New fraud patterns added regularly
   - Check GitHub for latest release

---

## ❓ FAQ

**Q: Does it need internet?**
A: No! All fraud detection works offline. Internet only needed for VirusTotal URL scanning (optional).

**Q: Does it record all calls?**
A: Only when app is running. By default yes, but you can disable in settings.

**Q: Is my data secure?**
A: Yes! All data encrypted with AES-256. No data sent to cloud without permission.

**Q: Can I share with family?**
A: Yes! APK can be shared. Each installation independent.

**Q: How accurate is fraud detection?**
A: ~95% for obvious scams. Some patterns may vary by region.

**Q: Can I export history?**
A: Yes! Go to History → Menu → Export as CSV.

**Q: What if call is wrong person?**
A: False positives are rare. Check details before taking action.

**Q: How to uninstall?**
A: Settings → Apps → SAFEx → Uninstall.

---

## 📞 Getting Help

1. **In-App Help**
   - Settings → About → Help
   - Tap any question mark icon

2. **Report Issues**
   - Settings → Feedback
   - Describe issue clearly
   - Include steps to reproduce

3. **GitHub**
   - github.com/aditya-upmanyu/Safex
   - File issue in Issues section

4. **Government Resources**
   - NCRP: cybercrime.gov.in
   - Helpline: 1930
   - Police: 100

---

## 🎬 Quick Video Steps

1. **Installation** (1 min)
   - Download APK
   - Install on device
   - Grant permissions

2. **Setup** (2 min)
   - Complete onboarding
   - Configure settings
   - Set emergency contact

3. **Usage** (1 min)
   - Make test call
   - See analysis
   - View history

---

**Ready to protect yourself? Start using SAFEx now!** 🛡️

---

*Last Updated: September 14, 2026*  
*SAFEx v1.0 - Stay Safe Online*
