# 🛡️ SAFEx (Secure Anti-Fraud Shield) – Complete Architecture & Technical Context

> **Version:** 1.0.0  
> **Target SDK:** Android 14 (API 34) | **Min SDK:** Android 8.0 (API 26)  
> **Architecture:** Clean Architecture + MVVM + MVI/Unidirectional Data Flow (UDF)  
> **UI Toolkit:** 100% Jetpack Compose (Material 3)  
> **Dependency Injection:** Dagger Hilt  
> **Database:** Room with TypeConverters  
> **State Management:** Kotlin Coroutines & `StateFlow` / `SharedFlow`  

---

## 📌 1. Project Overview & Problem Statement

### The Problem
India is witnessing an exponential surge in cyber-crimes and telecommunication frauds:
- **Digital Arrest Scams:** Scammers impersonating CBI, ED, Police, or Customs officials placing victims under fake digital arrests via audio/video calls.
- **KYC & Banking Fraud:** Fraudulent calls claiming electricity/SIM/bank account suspension unless personal credentials or OTPs are shared.
- **Phishing URLs & Malicious QR Codes:** Deceptive payment links or WhatsApp forwards designed to drain UPI accounts.
- **Remote Access Exploits:** Tricking seniors or unsuspecting users into installing screen-sharing software (AnyDesk, TeamViewer, RustDesk).

### The Solution: SAFEx
**SAFEx** is an intelligent, real-time cyber protection assistant built natively for Android. It operates silently in the background, monitors incoming/outgoing calls, scans suspicious links/QR codes, verifies unfamiliar contact numbers, performs offline and AI-driven voice analysis, and provides instantaneous SOS alerting to emergency contacts and Indian government cybercrime helplines (**1930 / NCRP / Chakshu**).

---

## 🏛️ 2. Architectural Blueprint

SAFEx follows **Uncle Bob’s Clean Architecture** principles combined with **Android Jetpack MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**.

```
                      ┌───────────────────────────────────────────────┐
                      │              PRESENTATION LAYER               │
                      │  Jetpack Compose Screens + ViewModels + Theme │
                      └───────────────────────▲───────────────────────┘
                                              │ StateFlow / Actions
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      │                 DOMAIN LAYER                  │
                      │     Pure Kotlin UseCases + Models + Repos     │
                      └───────────────────────▲───────────────────────┘
                                              │ Implements Repositories
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      │                  DATA LAYER                   │
                      │  Room DB + Retrofit (REST APIs) + DataStore   │
                      └───────────────────────────────────────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      │          BACKGROUND & HARDWARE ENGINE         │
                      │ BroadcastReceivers + ForegroundServices + Cam │
                      └───────────────────────────────────────────────┘
```

### Layer Breakdown

1. **Presentation Layer (`com.safex.app.presentation`):**
   - **Declarative UI:** Built completely using Jetpack Compose with Material 3 theming.
   - **Single Activity Architecture:** Driven by `MainActivity.kt` hosting a Compose `NavHost`.
   - **ViewModels:** Injected via `@HiltViewModel`. ViewModels expose UI state using immutable `StateFlow<UiState>` and handle user intents asynchronously.

2. **Domain Layer (`com.safex.app.domain`):**
   - Completely decoupled from the Android framework (Pure Kotlin).
   - Contains business entities (`CallRecord`, `FraudAnalysis`, `ScanResult`, `SecurityStatus`).
   - Repository interfaces (`CallRepository`, `ScanRepository`).
   - Granular Use Cases (`AnalyzeCallUseCase`, `OfflineFraudAnalysisUseCase`, `ScanUrlUseCase`, `GetSecurityStatusUseCase`, etc.).

3. **Data Layer (`com.safex.app.data`):**
   - **Local Storage:** Room Database (`AppDatabase`) storing analyzed calls, scan history, and active alerts.
   - **DataStore:** `SettingsDataStore` providing reactive, type-safe persistence for user preferences (theme, risk threshold, onboarding state).
   - **Remote Data Sources:** Retrofit interfaces for OpenAI GPT API, Google Cloud Speech-to-Text API, and VirusTotal API.
   - **Repository Implementations:** `CallRepositoryImpl` and `ScanRepositoryImpl` providing caching, fallback logic, and offline-first data handling.

4. **Background Services & Receivers (`com.safex.app.service`, `com.safex.app.receiver`):**
   - Hardware telephony monitoring via `CallStateReceiver`.
   - Boot auto-launch via `BootReceiver`.
   - Foreground services managing call recording, audio processing, and heads-up alert dispatch.

---

## 📦 3. Complete Tech Stack & Libraries

| Component | Library / Version | Purpose |
|---|---|---|
| **Language** | Kotlin `1.9.22` | Core programming language |
| **Java Target** | Java 17 | Bytecode target & toolchain |
| **UI Framework** | Jetpack Compose BOM `2024.02.00` | Modern reactive UI framework |
| **Design System** | Material 3 (`androidx.compose.material3:material3`) | Material You widgets, dynamic styling |
| **Dependency Injection** | Dagger Hilt `2.50` (`hilt-android`, `hilt-navigation-compose`) | Compile-time dependency injection |
| **Local Database** | Room `2.6.1` (`room-runtime`, `room-ktx`, KSP compiler) | SQLite abstraction for call logs & alerts |
| **Key-Value Store** | Jetpack DataStore Preferences `1.0.0` | Asynchronous settings persistence |
| **Networking** | Retrofit `2.9.0` + OkHttp `4.12.0` | REST API client with logging interceptor |
| **Serialization** | Gson `2.10.1` | JSON parsing for API responses & DB converters |
| **Camera & Scanning** | CameraX `1.4.1` (`camera-core`, `camera-camera2`, `camera-view`) | QR code camera viewfinder with 16KB alignment |
| **Security & Crypto** | AndroidX Security Crypto `1.1.0-alpha06` | Secure key management |
| **Biometrics** | AndroidX Biometric `1.1.0` | Fingerprint & face authentication hooks |
| **Async & Concurrency** | Kotlinx Coroutines `1.7.3` | Non-blocking asynchronous tasks |
| **Image Loading** | Coil Compose `2.5.0` | Async image caching and rendering |
| **Animations** | Lottie Compose `6.3.0` | Smooth vector animations |
| **Permissions** | Accompanist Permissions `0.34.0` | Declarative Android runtime permissions |

---

## 🔍 4. In-Depth Features & Implementation Details

### 1. Protection Shield & Device Security Engine
- **How it works:**  
  The Home screen features a pulsing hero card displaying real-time system status. Tapping the card opens the `DeviceSecurityScreen`.
- **Under the Hood (`RootCheckUtil.kt`, `GetSecurityStatusUseCase.kt`):**
  - **Root Detection:** Checks for `su` binary existence across `/system/bin/su`, `/system/xbin/su`, `/sbin/su`, `/system/su`, and checks for superuser management packages (`eu.chainfire.supersu`, `com.topjohnwu.magisk`, `com.koushikdutta.superuser`).
  - **Screen Lock Audit:** Uses `KeyguardManager.isDeviceSecure()` to ensure the user has a PIN/Pattern/Biometric set.
  - **Developer Mode Check:** Reads `Settings.Global.DEVELOPMENT_SETTINGS_ENABLED` to alert if USB debugging is open.
  - **Unknown Sources Check:** Audits `Settings.Secure.INSTALL_NON_MARKET_APPS`.
  - **Score Engine:** Aggregates checks into a 0–100 Security Score displayed in an arc gauge.

---

### 2. Live Call Detection & Real-Time Analysis Pipeline
The background call guard monitors calls through a deterministic 5-step pipeline:

```
[ Incoming / Outgoing Call ]
            │
            ▼
┌─────────────────────────┐
│    CallStateReceiver    │  (Listens for TELEPHONY_STATE_CHANGED)
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│   CallRecorderService   │  (Foreground Service: records call audio safely)
└───────────┬─────────────┘
            ▼ (On CALL_STATE_IDLE)
┌─────────────────────────┐
│ AnalysisPipelineService │  (Coordinates transcription & heuristic analysis)
└───────────┬─────────────┘
            ├── 1. TranscribeAudioUseCase (Google Speech-to-Text API / Audio Parser)
            ├── 2. OfflineFraudAnalysisUseCase (Instant Indian scam pattern heuristics)
            └── 3. AnalyzeCallUseCase (Optional: OpenAI GPT-4 Cloud Threat Intel)
            │
            ▼
┌─────────────────────────┐
│    FraudAlertService    │  (Dispatches Heads-Up Notification if Risk > Threshold)
└───────────┬─────────────┘
            ▼
┌─────────────────────────┐
│      Room Database      │  (Persists call record in CallRecordEntity)
└─────────────────────────┘
```

---

### 3. Dual-Mode Fraud Detection Engine (Offline + Cloud AI)

#### A. Offline Rule Engine (`OfflineFraudAnalysisUseCase.kt`)
Works **100% offline without internet or API keys**, detecting Indian cyber fraud categories using rule-based NLP:
1. **Digital Arrest Scams:**  
   - Patterns: Claims of fake warrants, money laundering, customs parcels seized, narcotics, illegal passports.  
   - Targeted agencies: CBI, ED, Crime Branch, Mumbai Police, Customs, Supreme Court.  
   - Action: High risk (85+), generates bilingual advisory (English + Hindi: *"नकली सरकारी अधिकारी द्वारा कानूनी कार्रवाई का दावा"*).
2. **KYC & Banking Impersonation:**  
   - Patterns: Account suspension threats, PAN card update, electricity meter disconnection, credit card reward points expiration.  
   - Action: Flags bank impersonation; warns user that banks never ask for credentials.
3. **OTP & Urgent Fund Transfers:**  
   - Patterns: "Share OTP", "Send 1 rupee for verification", "Approve UPI collect request".
4. **Remote Access Traps:**  
   - Patterns: Requesting victim to install AnyDesk, QuickSupport, TeamViewer, RustDesk.

#### B. Cloud AI Engine (`AnalyzeCallUseCase.kt` & `OpenAIApi.kt`)
When internet and API keys are configured:
- Uses OpenAI API with structured JSON output prompts.
- Evaluates context, emotional pressure, voice tone signals, and social engineering indicators.
- Returns comprehensive threat metrics: `riskScore`, `riskLevel` (`SAFE`, `LOW`, `MEDIUM`, `HIGH`, `FRAUD`), `scamType`, `keyFindings`, and `safetyAdvice`.

---

### 4. URL & QR Code Scam Scanner (`ScanUrlUseCase.kt`, `ScanRepositoryImpl.kt`)
- **How it works:**  
  Allows users to manually paste URLs, open CameraX to scan physical QR codes, or receive links shared from external apps.
- **Under the Hood:**
  - Normalizes and validates URL format.
  - Queries local Room cache (`ScanHistoryDao.getByUrl`) to prevent repeated lookups within 1 hour.
  - Sends URL to **VirusTotal API v3** (`/api/v3/urls`).
  - Evaluates multi-engine security vendor analysis (`malicious`, `suspicious`, `harmless`).
  - Classifies risk score:
    - `malicious > 5` ➔ Risk: 90 (`FRAUD`)
    - `malicious > 0 || suspicious > 2` ➔ Risk: 70 (`HIGH`)
    - `suspicious > 0` ➔ Risk: 40 (`MEDIUM`)
    - Clean ➔ Risk: 0 (`SAFE`)

---

### 5. Contact Verification & Intelligence Lookup (`ContactVerificationScreen.kt`)
- **How it works:**  
  Users enter any unknown telephone number before answering or calling back.
- **Under the Hood:**
  - Validates phone number syntax via `PhoneNumberValidator.kt` (Indian format: 10 digits starting with 6, 7, 8, or 9).
  - Fetches reputation metadata: Total user complaints, Trust Score (0-100), Categorization (e.g., Banking Fraud, Telemarketer, Safe), and Last Reported recency.
  - Generates actionable safety badge: "Limited information available. Answer with caution."
  - Provides a 1-tap **"Report"** button linked directly to the Government Reporting flow.

---

### 6. Emergency SOS & Direct Government Reporting
- **Emergency SOS (`EmergencySOSScreen.kt`, `EmergencySOSViewModel.kt`):**
  - Instant SOS button with 1-tap emergency broadcast.
  - Sends pre-formatted SMS: *"EMERGENCY ALERT: I am experiencing a fraud call. Please call me immediately. - SAFEx App"*.
  - Pre-configured emergency authority speed dials:
    - **1930:** National Cyber Crime Reporting Helpline (Citizen Financial Cyber Fraud Reporting System).
    - **155260:** Financial Fraud Helpline.
  - Supports adding, viewing, and deleting trusted family contacts.
- **Report Fraud Screen (`ReportFraudScreen.kt`):**
  - **NCRP (I4C):** Direct link to `https://cybercrime.gov.in`.
  - **Call 1930 Helpline:** Instant dialer intent `tel:1930`.
  - **Chakshu (Department of Telecommunications):** Direct portal link to Sanchar Saathi Chakshu reporting.
  - Fraud Details description field for filing incident summaries.

---

### 7. Fraud Dashboard & Analytics (`DashboardScreen.kt`, `DashboardViewModel.kt`)
- Aggregates daily telemetry:
  - Calls Analyzed
  - Fraud Blocked
  - Safe Calls Verified
  - Overall Protection Rate (%)
- Visual **Weekly Fraud Trends** bar chart tracking day-by-day scam activity (Wednesday through Tuesday).
- Recent Alerts list displaying flagged calls with time, phone number, and risk indicators.

---

### 8. Voice Analysis Detailed View (`VoiceAnalysisScreen.kt`)
- Opens per-call deep analysis by ID (`/voice_analysis/{callId}`).
- Full conversation transcript viewer with highlighted suspicious keywords.
- Audio playback waveform visualization.
- Threat breakdown cards showing scam category, detection confidence %, and immediate recommended next steps.

---

### 9. User Preferences & Settings (`SettingsScreen.kt`, `SettingsDataStore.kt`)
- **Theme Selection:** System Default, Light, Dark.
- **Risk Alert Threshold:** Interactive slider (0 to 100) allowing user customization of sensitivity (default: 70).
- **Emergency Contact:** Configurable primary contact number stored safely via DataStore Preferences.
- **Build Info:** Application version, environment details.

---

## 🛠️ 5. Key Bugs Fixed & Architectural Optimizations

During audit and hardening, the following critical issues were diagnosed and resolved:

| Issue | Root Cause | Fix Applied |
|---|---|---|
| **Fatal App Crash on Boot (`UnsatisfiedLinkError`)** | `SAFExApplication.kt` called `System.loadLibrary("sqlcipher")`, but the project uses standard Room SQLite with 16KB alignment, not legacy SQLCipher native binaries. | Removed `loadLibrary("sqlcipher")`. Room works natively across ARM64 & x86_64. |
| **Black Screen on App Startup** | In `MainActivity.kt`, `setContent { ... }` was wrapped inside a `lifecycleScope.launch` coroutine block. This delayed/blocked Compose view hierarchy attachment. | Moved `setContent { ... }` to `onCreate()` root. Replaced blocking coroutine with reactive `themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)`. |
| **Foreground Service Crash on Android O+** | `CallStateReceiver.kt` invoked `context.startService()`. On Android 8.0+, starting background services when app is in background throws `IllegalStateException`. | Updated to `ContextCompat.startForegroundService(context, intent)` with proper foreground notification. |
| **Missing Navigation & Unwired Buttons** | Screens like `DashboardScreen`, `EmergencySOSScreen`, `ContactVerificationScreen`, and `VoiceAnalysisScreen` lacked back button callbacks; Settings gear and Notification bell icons were inactive. | Fully updated `SAFExNavGraph.kt` and all screen composables. Added `onNavigateBack`, `onNavigateToVoiceAnalysis`, `onNavigateToReport`, `onNavigateToSettings`, and `onShieldClick`. |
| **Compilation Error in Emergency SOS** | `EmergencyContactItem` directly called `viewModel.removeContact()`, but `viewModel` was out of scope in the child composable. | Refactored `EmergencyContactItem` to accept `onRemoveClick: () -> Unit` lambda following UDF principles. |

---

## 🚀 6. How to Run the Project (Android Studio)

1. **Prerequisites:**
   - Android Studio Hedgehog / Iguana / Jellyfish or newer.
   - JDK 17 configured in Android Studio (`Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`).
   - Android SDK 34 installed.

2. **Run Steps:**
   - Open folder `C:\Users\adity\OneDrive\Desktop\SafexRecovery` in Android Studio.
   - Let Gradle sync complete.
   - Connect a physical phone (USB Debugging ON) or start the `Pixel_6a` emulator.
   - Click the green **Run ▶** button (`Shift + F10`).

3. **CLI Build & Install:**
   ```powershell
   # Assemble Debug APK
   .\gradlew.bat assembleDebug

   # Install on connected device/emulator
   adb install -r app\build\outputs\apk\debug\app-debug.apk
   ```

---

## 🎓 7. Teacher / Evaluator Demo Presentation Script

When demonstrating this project to professors, examiners, or evaluators:

1. **Introduction (1 min):**
   > *"Good morning/afternoon. SAFEx is a real-time anti-fraud Android application designed to protect citizens from emerging cyber-crimes like Digital Arrests, fake KYC warnings, and malicious phishing links. It is built using modern Android Clean Architecture, Jetpack Compose, and Dagger Hilt."*

2. **Architecture Walkthrough (1 min):**
   > *"The app follows Clean Architecture with strict separation between Presentation, Domain, and Data layers. The UI is 100% declarative Compose with Material 3. Data is managed via Room SQLite and DataStore Preferences, with zero third-party crash dependencies."*

3. **Live Feature Demo (3 mins):**
   - **Hero Shield:** Show the animated Home screen protection score. Tap the shield to demonstrate `DeviceSecurityScreen` (Root check, Play Protect, Developer options audit).
   - **Contact Verification:** Tap **Verify**, enter `9876543210`, tap **Verify Contact**. Show the real-time trust score (67/100) and complaint history.
   - **Government Reporting:** On the verified card, tap **Report** to show seamless navigation to the Indian Cyber Helpline (1930, NCRP, Chakshu).
   - **Emergency SOS:** Show the quick SOS screen with pre-configured 1930 / 155260 cyber crime helplines and emergency contact management.
   - **Scam Scanner:** Show the URL / QR code scanner powered by VirusTotal threat intelligence.
   - **Dashboard:** Show the weekly fraud trend visualizer and analytics.

4. **Offline Intelligence Highlight (1 min):**
   > *"A major technical highlight is that SAFEx doesn't just rely on external APIs; it includes an offline rule-based NLP engine specifically tailored for Indian fraud patterns, providing immediate protection even without active internet."*
