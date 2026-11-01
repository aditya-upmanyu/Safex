# SAFEx Fraud Detection System - Complete Integration Guide

## 🚀 Overview

SAFEx is now a **fully functional fraud detection system** that:
- 📞 **Detects incoming calls in real-time**
- 🎙️ **Records calls automatically**
- 🔍 **Analyzes voice for fraud patterns offline**
- 🚨 **Sends instant alerts when fraud is detected**
- 💾 **Stores all data in encrypted database**

---

## 📱 How It Works (User Journey)

### 1. **Incoming Call Detection**
```
User receives call → CallStateReceiver detects incoming call
↓
Extracts phone number automatically
↓
CallRecorderService starts recording (if enabled)
↓
Notification shows: "Recording call from +91XXXXXXXXXX..."
```

### 2. **Call Recording**
```
Call Audio → Recorded in: /data/data/com.safex.app/files/call_recordings/
↓
Format: M4A (AAC encoded)
↓
Stored securely in app's private directory
```

### 3. **Fraud Analysis (Offline)**
```
Recording stops when call ends
↓
AnalysisPipelineService extracts transcript
↓
OfflineFraudAnalysisUseCase scans for fraud keywords:
  • Digital Arrest Scams (CBI, ED, RBI impersonation)
  • KYC/Banking Fraud
  • OTP/Money Transfer Scams
  • Lottery Scams
  • Investment Scams
  • Courier Scams
↓
Generates Risk Score (0-100) + Confidence Level
```

### 4. **Instant Alert**
```
If Risk Score ≥ Threshold (default 70):
↓
🚨 High-priority notification sent
↓
Phone vibrates + Red alert color
↓
SMS to emergency contact (if configured)
↓
User can view details in app
```

### 5. **Data Storage**
```
Call Record saved to encrypted SQLCipher database:
  ✓ Phone number
  ✓ Call timestamp (IST)
  ✓ Duration
  ✓ Transcript
  ✓ Risk score
  ✓ Detected fraud type
  ✓ Recommendation
↓
Data retrieval in History screen
```

---

## 🔧 Technical Architecture

### **System Components**

```
┌─────────────────────────────────────────────────────────────┐
│                      USER INTERFACE                         │
│  (Screens: Home, CallGuard, History, Settings, etc.)       │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                  PRESENTATION LAYER                         │
│  ViewModels + Jetpack Compose + Material 3 UI               │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   DOMAIN LAYER                              │
│  ├─ OfflineFraudAnalysisUseCase (Keyword matching)         │
│  ├─ TranscribeAudioUseCase (Audio → Text)                  │
│  ├─ AnalyzeCallUseCase (API-based, optional)               │
│  └─ Other UseCases                                          │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                    DATA LAYER                               │
│  ├─ Local: SQLCipher encrypted database                    │
│  │   └─ Tables: CallRecord, ScanHistory, Alert             │
│  ├─ Remote: APIs (OpenAI, VirusTotal, Google STT)          │
│  └─ Preferences: DataStore (encrypted settings)            │
└──────────────────────────────────────────────────────────────┘
```

### **Background Services Flow**

```
┌─────────────────────────────────────────────────────────────┐
│            Real-time Call Detection (BroadcastReceiver)     │
│                  CallStateReceiver                           │
│  Listens to: android.intent.action.PHONE_STATE              │
└────────────────────┬────────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
    RINGING                   IDLE
         │                       │
         ↓                       ↓
    ┌─────────────┐         ┌──────────────┐
    │ START CALL  │         │  STOP CALL   │
    │ RECORDING   │         │  RECORDING   │
    └────┬────────┘         └──────┬───────┘
         │                         │
         ↓                         ↓
   CallRecorderService    CallRecorderService
   • MediaRecorder start     • MediaRecorder stop
   • Foreground service      • Trigger analysis
   • Notification            • Cleanup files
         │                         │
         └────────────┬────────────┘
                      │
                      ↓
          AnalysisPipelineService
          • Transcribe audio
          • Offline fraud detection
          • Save to database
                      │
                      ↓
         ┌────────────────────────────┐
         │ Risk Score ≥ Threshold?    │
         └────────┬───────────────────┘
                  │
          ┌───────┴────────┐
         YES              NO
          │                 │
          ↓                 ↓
  FraudAlertService    Safe (no alert)
  • Send notification
  • Send SMS alert
  • Update UI
```

---

## 🔍 Fraud Detection Algorithm

### **Multi-Level Detection**

#### **Level 1: High-Risk Indicators (Weight: +85)**
- **Digital Arrest Scams**
  - Keywords: "CBI", "ED", "RBI", "warrant", "arrest", "court"
  - Action: Block call, instant alert

#### **Level 2: Financial Fraud (Weight: +75-80)**
- KYC Fraud: "account freeze", "verify", "bank officer" + sensitive info request
- OTP Fraud: "share OTP", "verify account"
- Money Transfer: "transfer money", "send payment"

#### **Level 3: Deception Tactics (Weight: +60-70)**
- Lottery Scams: "prize", "won", "claim", "reward"
- Investment Scams: "guaranteed returns", "expert trader"
- Job Scams: "work from home", "advance fee"
- Courier Scams: "parcel", "customs", "fee"

#### **Level 4: Behavior Indicators (Weight: +15-20)**
- Urgency: "immediately", "don't wait", "act now"
- Emotional Manipulation: "afraid", "worried", "trust"

### **Risk Score Calculation**

```
Risk Score = Sum of all matched patterns (0-100)

Verdict Mapping:
├─ FRAUD: Score ≥ 70 (Confidence: 0.75-0.95)
├─ SUSPICIOUS: Score 40-69 (Confidence: 0.70)
└─ SAFE: Score < 40 (Confidence: 0.90)
```

---

## 📊 Database Schema

### **CallRecord Table**

```sql
CREATE TABLE call_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone_number TEXT NOT NULL,
    timestamp LONG NOT NULL,           -- IST timestamp
    duration LONG NOT NULL,             -- ms
    transcript TEXT,                    -- Call audio transcript
    risk_score INTEGER,                 -- 0-100
    verdict TEXT,                       -- SAFE|SUSPICIOUS|FRAUD
    confidence REAL,                    -- 0.0-1.0
    detected_patterns TEXT,             -- JSON array
    keywords TEXT,                      -- JSON array
    reason TEXT,                        -- English reason
    reason_hindi TEXT,                  -- Hindi reason
    recommended_action TEXT,            -- What user should do
    status TEXT DEFAULT 'ANALYZED',     -- PENDING|ANALYZED|ARCHIVED
    created_at LONG DEFAULT CURRENT_TIMESTAMP
);
```

### **Indexed Queries**

```sql
-- Get recent calls
SELECT * FROM call_record 
WHERE timestamp > ? 
ORDER BY timestamp DESC 
LIMIT 10;

-- Get fraud calls
SELECT * FROM call_record 
WHERE verdict = 'FRAUD' 
ORDER BY timestamp DESC;

-- Search by phone number
SELECT * FROM call_record 
WHERE phone_number LIKE '%' || ? || '%';
```

---

## 🛡️ Security Features

### **Data Encryption**
- ✅ SQLCipher AES-256 database encryption
- ✅ Android Keystore for master key storage
- ✅ Encrypted SharedPreferences for settings
- ✅ HTTPS-only API calls

### **Privacy**
- ✅ Device-only processing (no cloud upload without consent)
- ✅ Automatic recording file cleanup
- ✅ No tracking or analytics by default
- ✅ User-controlled data retention

### **Permissions**
- ✅ Runtime permission checks
- ✅ Graceful fallback if permissions denied
- ✅ No silent data collection

---

## 🎯 Fraud Patterns Detected

### **Indian-Specific Scam Types**

| Scam Type | Keywords | Example |
|-----------|----------|---------|
| **Digital Arrest** | CBI, ED, warrant, arrest | "This is CBI officer. You have a case against you." |
| **KYC Fraud** | Account freeze, verify, bank | "Your bank account is blocked. Verify identity now." |
| **OTP Fraud** | Share OTP, confirm code | "Don't share your OTP with anyone" (caller asking for it) |
| **Lottery Scam** | Prize, claimed, congratulations | "You've won ₹10 lakhs in a lottery." |
| **Investment Scam** | Returns, trader, profit | "Invest ₹5000, get ₹50000 guaranteed." |
| **Courier Scam** | Parcel, customs, officer | "Your package has drugs. Pay fine or arrest." |

---

## 📋 User Settings

### **Configurable Options**

```
Settings:
├─ Call Guard
│  ├─ Enable/Disable recording
│  └─ Auto-detect incoming calls
├─ Risk Threshold (50-90)
│  └─ Alert trigger level
├─ Emergency Contact
│  └─ SMS alert recipient
├─ Notifications
│  ├─ High-risk alerts
│  ├─ Suspicious alerts
│  └─ Safe call summaries
├─ API Keys (Optional)
│  ├─ OpenAI key
│  ├─ Google STT key
│  └─ VirusTotal key
└─ Theme
   ├─ Light/Dark/System
   └─ Biometric lock
```

---

## 🧪 Testing the System

### **Manual Test Scenario**

```
1. Install app on Android device/emulator
2. Grant permissions:
   - Phone state
   - Microphone
   - Notifications
   - SMS (for alerts)

3. Make test call with fraud keywords:
   - Say: "This is CBI officer. You have a case."
   - Or: "Your account is frozen. Verify immediately."

4. Expected flow:
   ✓ Call recording starts
   ✓ Recording file created
   ✓ Transcript generated
   ✓ Fraud detected (high risk score)
   ✓ Alert notification sent
   ✓ SMS to emergency contact (if configured)
   ✓ Record saved in History

5. Verify in History screen:
   ✓ Phone number shown
   ✓ Risk score displayed
   ✓ Fraud type identified
   ✓ Recommendation shown
```

---

## 📊 API Integration (Optional)

### **OpenAI API** (for advanced analysis)
```kotlin
// If API key configured, uses GPT-4o for detailed analysis
// Falls back to offline detection if key not available
```

### **Google Speech-to-Text** (for transcription)
```kotlin
// Converts audio to text for better analysis
// Falls back to mock transcription if not configured
```

### **VirusTotal** (for URL scanning)
```kotlin
// Scans URLs and QR codes against 72+ security engines
// Available in URL Scanner screen
```

---

## 🔄 Data Flow Example

### **Complete Call Analysis Flow**

```
T=0:00  User receives call from +91-9876-5432-10
        ↓
        CallStateReceiver fires (EXTRA_STATE_RINGING)
        ↓
T=0:01  CallRecorderService.startRecording()
        - Records audio: call_9876543210_20260914_140530.m4a
        - Shows notification: "Recording call from +91-9876-543210"
        - User is on call
        ↓
T=1:30  Caller says: "I am CBI officer. Your account has court case."
        - Audio captured
        ↓
T=2:15  User hangs up / call ends
        ↓
        CallStateReceiver fires (EXTRA_STATE_IDLE)
        ↓
        CallRecorderService.stopRecording()
        - Stops recording
        - Sends to AnalysisPipelineService
        ↓
T=2:16  AnalysisPipelineService.performAnalysis()
        - Extracts transcript: "I am CBI officer. Your account has court case."
        ↓
T=2:17  OfflineFraudAnalysisUseCase.invoke(transcript)
        - Detects keywords: "CBI", "court case"
        - Matches pattern: DIGITAL_ARREST
        - Risk score: 85 (High)
        - Confidence: 0.95
        - Verdict: FRAUD
        ↓
T=2:18  Saves to CallRecord table:
        - phone_number: "+919876543210"
        - timestamp: 1694695230000
        - risk_score: 85
        - verdict: "FRAUD"
        - reason: "Detected fake CBI authority impersonating for account threat"
        - reason_hindi: "नकली CBI अधिकारी द्वारा खाता खतरे के लिए प्रदर्शन"
        ↓
T=2:19  Risk score (85) ≥ Threshold (70)
        ↓
        FraudAlertService.sendFraudAlert()
        - Sends high-priority notification (RED)
        - Shows: "🚨 FRAUD DETECTED - DIGITAL ARREST SCAM"
        - SMS to emergency contact: "SAFEx Alert: Fraud detected (Score: 85/100)..."
        ↓
T=2:20  App notification received
        - User taps notification
        - Taken to Call Guard screen
        - Shows detailed analysis
        - User can report to cybercrime.gov.in
        ↓
T=2:21  Recording file deleted (cleanup)

TOTAL TIME: ~21 seconds from call start to alert
```

---

## ✅ Verification Checklist

- [x] Call recording working (MediaRecorder integrated)
- [x] Offline fraud detection (keyword-based rules engine)
- [x] Database storage (SQLCipher encrypted)
- [x] Real-time alerts (high-priority notifications)
- [x] SMS emergency alerts (configurable)
- [x] Call state detection (BroadcastReceiver)
- [x] Phone number extraction
- [x] Risk scoring algorithm
- [x] Hindi translations
- [x] Manifest permissions updated

---

## 🚀 Next Steps

1. **Install on device/emulator**
2. **Enable Call Guard in Settings**
3. **Set emergency contact**
4. **Make test call with fraud keywords**
5. **Verify alert system works**
6. **Check History for recorded data**

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| Calls not recording | Check microphone permission, enable in settings |
| No alerts received | Check risk threshold, set to ≥70 |
| SMS not sent | Grant SMS permission, set emergency contact |
| Database errors | Clear app data, reinstall |
| No fraud detection | Use specific keywords (see fraud patterns above) |

---

## 📞 Support

- **Report Issue**: File in app
- **Government Links**: Built into app
- **Documentation**: See README.md

---

**SAFEx v1.0 - Protecting Users from Fraud ✅**
