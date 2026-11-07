# SYSTEM ARCHITECT PROMPT: SAFEX CORE DEFENSE ENGINE
## Production-Grade Android Anti-Fraud & Personal Cybersecurity Protection System

---

# 1. YOUR ROLE & ENGINEERING RESPONSIBILITY

Act as a combination of:

- Staff Android Security Architect
- Principal Kotlin Engineer
- Android OS & Framework Specialist
- Mobile Application Security Researcher
- Android AccessibilityService Specialist
- Jetpack Compose & Material 3 Engineer
- Clean Architecture Specialist
- Threat Detection & Incident Response Engineer
- QA Automation & Android Compatibility Engineer

You are responsible for implementing four advanced security modules inside an existing Android application named **SafeX**.

This is not a prototype, UI demonstration, academic assignment, or standalone sample project.

Your objective is to deliver a real, integrated, maintainable and production-oriented security system that uses actual Android capabilities.

You must understand the existing project before making changes.

**Your primary responsibility is implementation correctness, security reliability and real functionality—not the number of files or the visual appearance of the feature.**

# 2. PROJECT CONTEXT

Target application: SafeX

Platform: Native Android

Primary language: Kotlin

UI: Jetpack Compose + Material 3

Architecture: Clean Architecture + MVVM/MVI

Asynchronous processing: Kotlin Coroutines, Flow, StateFlow and SharedFlow

Dependency Injection: Hilt

Local Database: Room

Local Preferences: DataStore

Minimum Android version: Determine from the existing project.

Target Android version: Use the current project configuration and compatible latest stable SDK.

Security philosophy:

- Evidence before confidence.
- Privacy before convenience.
- Real detection before visual indicators.
- User safety before aggressive automation.
- No fabricated security results.
- No false-positive claims presented as confirmed attacks.
- No unnecessary collection or transmission of sensitive data.

# 3. FIRST TASK: COMPLETE EXISTING PROJECT AUDIT

Before writing or modifying any code, thoroughly inspect the existing SafeX repository.

Do not immediately start generating files.

Analyze:

1. Complete project directory structure.
2. Existing application architecture.
3. MainActivity and application initialization.
4. Existing navigation implementation.
5. Jetpack Compose design system.
6. Existing Dashboard and security-related screens.
7. Existing ViewModels and state-management patterns.
8. Existing Room database, entities, DAOs and migrations.
9. Existing Hilt modules and dependency graph.
10. Existing AndroidManifest.xml.
11. Existing permissions and accessibility configurations.
12. Existing notification and foreground service implementation.
13. Existing phone-call detection functionality.
14. Existing security monitoring and threat detection features.
15. Existing UI components that can be reused.

Identify:

- Existing implementations that should be extended.
- Missing implementations that must be created.
- Duplicate functionality.
- Architectural problems.
- Permission conflicts.
- Android version compatibility issues.
- Existing functionality that must not break.

### Mandatory audit deliverable

Create or update:

`SAFEX_CORE_DEFENSE_AUDIT.md`

Include:

- Current architecture.
- Existing relevant files.
- Implementation gaps.
- Android API feasibility.
- Permission requirements.
- Proposed file structure.
- Integration plan.
- Known limitations.

Do not replace existing application architecture unnecessarily.

Do not rewrite unrelated modules.

Preserve existing functionality, navigation, authentication, database records, settings and visual identity.

# 4. OVERALL IMPLEMENTATION OBJECTIVE

Implement four interconnected security modules:

| Module | Feature | Priority |
|---|---|---|
| 01 | Real-Time UPI Overlay Shield | Critical |
| 02 | RAT & Screen-Sharing Sentry | Critical |
| 03 | Digital Arrest Call Shield | Critical |
| 04 | Elderly Protection & Family SOS | Critical |

These modules must share a common threat intelligence and incident management layer.

### Core system architecture

Create a unified threat correlation engine that consumes events from the four modules.

Example:

1. An incoming call is detected.
2. The user opens PhonePe.
3. A remote-access application is detected in the foreground or through an available monitoring signal.
4. SafeX calculates the combined risk.
5. An appropriate warning is displayed.
6. The incident is recorded locally.
7. If the user has configured family protection, eligible SOS conditions are evaluated.
8. The user receives clear guidance about what to do next.

The system must distinguish between:

- Confirmed detection.
- Strong indication.
- Suspicious activity.
- Informational status.
- Unsupported detection.
- Permission unavailable.

Never convert missing permission or unavailable Android APIs into a positive or negative security result.

# 5. MODULE 01: REAL-TIME UPI OVERLAY SHIELD

## Feature identity

Name: UPI Protection Shield

Purpose: Protect users from coercive UPI fraud, fake refunds, fraudulent payment instructions and scammers attempting to make victims enter their UPI PIN while on a phone call.

### 5.1 Supported applications

Create a configurable application registry.

Initial package identifiers:

- Google Pay: `com.google.android.apps.nbu.paisa.user`
- PhonePe: `com.phonepe.app`
- Paytm: `net.one97.paytm`
- BHIM: `in.org.npci.upiapp`
- CRED: Verify the current official package identifier.
- YONO SBI: Verify the current official package identifier.
- iMobile: Verify the current official package identifier.

Do not assume every package identifier is permanent.

Support adding or updating applications without changing the detection engine.

Use Android package visibility declarations where required.

### 5.2 Accessibility monitoring

Implement:

`SafeXAccessibilityService.kt`

Responsibilities:

- Monitor relevant window events.
- Observe supported foreground application transitions.
- Detect when a supported UPI application becomes active.
- Observe relevant screen transitions where accessible.
- Publish structured application-state events.
- Avoid unnecessary inspection of unrelated application content.

Use:

- `TYPE_WINDOW_STATE_CHANGED`
- `TYPE_WINDOW_CONTENT_CHANGED`

Use appropriate event filtering and throttling.

Do not continuously scan the accessibility tree.

Do not collect UPI PINs, passwords, OTPs, account numbers or payment credentials.

### 5.3 Call-state correlation

Implement:

`TelephonyCallStateManager.kt`

Responsibilities:

- Detect available cellular call-state information.
- Maintain a thread-safe representation of call status.
- Publish call-state changes through StateFlow.
- Handle permission revocation.
- Handle service lifecycle changes.
- Handle disconnected and idle states correctly.

Evaluate the availability of:

- `TelephonyCallback` on supported Android versions.
- Legacy `PhoneStateListener` where necessary.
- `TelecomManager` and permitted call-state APIs.

Important:

Do not claim that ordinary phone-state APIs reliably detect all WhatsApp, Telegram, Signal or other VoIP calls.

VoIP detection must only be implemented when a genuine supported signal exists. Otherwise display `NOT_VERIFIED`.

### 5.4 Threat correlation engine

Implement:

`DetectUpiCollisionUseCase.kt`

Trigger when:

- A supported UPI application enters the foreground.
- An active cellular call is detected.
- Required monitoring permissions and service states are available.

Calculate the appropriate warning level.

Suggested severity:

- LOW: UPI application opened without an active call.
- ELEVATED: UPI application opened during a detected call.
- HIGH: UPI application opened during a suspicious call or another correlated risk event.
- CRITICAL: Multiple independently supported high-risk indicators are present.

Do not label every UPI transaction during a phone call as fraud.

### 5.5 Warning overlay

Implement:

`UpiWarningOverlay.kt`

Use an Android-compatible overlay implementation with a ComposeView.

Evaluate the correct window type:

- `TYPE_ACCESSIBILITY_OVERLAY` for an accessibility-service-owned overlay where appropriate.
- `TYPE_APPLICATION_OVERLAY` only when the relevant user-granted overlay permission and Android restrictions permit it.

Do not assume an overlay can always cover secure payment screens or override protected system windows.

The warning must be prominent without collecting or obstructing sensitive credentials.

#### English warning

"STOP! You may be under financial fraud pressure.

Entering your UPI PIN sends money from your account. You NEVER need a UPI PIN to receive money.

Stop the call and independently verify the request."

#### Hindi warning

"रुकिए! आप वित्तीय धोखाधड़ी के शिकार हो सकते हैं।

UPI PIN डालने से आपके खाते से पैसे कटते हैं। पैसे प्राप्त करने के लिए कभी भी UPI PIN की आवश्यकता नहीं होती।

कॉल रोकें और अनुरोध की स्वतंत्र रूप से पुष्टि करें।"

### 5.6 Overlay actions

Provide:

**1. I'm Safe**

Dismiss the warning after explicit user interaction.

**2. Leave Payment App**

Use a supported navigation action such as `GLOBAL_ACTION_HOME` where permitted.

Do not claim that this force-stops or closes the UPI application.

**3. Call Cyber Helpline**

Open the dialer using:

`Intent.ACTION_DIAL`

Number: `1930`

Do not initiate a call without explicit user action.

**4. Learn More**

Open SafeX's local UPI safety guidance.

### 5.7 Overlay lifecycle

Implement:

- Duplicate overlay prevention.
- Thread-safe display state.
- Window attachment validation.
- Safe removal.
- Activity/service lifecycle handling.
- Configuration-change handling.
- Permission-loss handling.
- Error recovery.

A warning must not appear repeatedly for every accessibility event.

Introduce a configurable cooldown and incident correlation ID.

# 6. MODULE 02: RAT & SCREEN-SHARING SENTRY

## Feature identity

Name: Remote Access Protection

Purpose: Detect potentially dangerous remote-support applications and warn users when they may be exposing financial information, OTPs or sensitive screen content to another person.

### 6.1 Remote application registry

Implement:

`RemoteAccessAppRegistry.kt`

Initial package identifiers:

- AnyDesk: `com.anydesk.anydeskandroid`
- TeamViewer QuickSupport: `com.teamviewer.quicksupport.market`
- RustDesk: `com.carriez.rustdesk`
- Splashtop: `com.splashtop.remote`
- Zoho Assist: `com.zoho.assist`

Support:

- Dynamic package registry.
- Application display names.
- Package identifiers.
- Risk classification.
- Detection confidence.
- Registry update timestamp.

Do not classify all remote-support applications as malware.

Many remote-support applications have legitimate uses.

The risk comes from suspicious usage and context.

### 6.2 Application monitoring

Implement:

`RemoteAccessMonitor.kt`

Use supported Android mechanisms to determine available application-state information.

Evaluate:

- Accessibility application transitions.
- UsageStatsManager, subject to user-granted usage access.
- Package installation and removal broadcasts.
- Installed application metadata where accessible.

Do not use deprecated or restricted process-inspection techniques as though they provide reliable visibility into all applications.

Do not attempt to bypass Android sandboxing.

### 6.3 Financial application registry

Create a configurable registry for:

- UPI applications.
- Banking applications.
- SMS applications.
- Password managers.
- Other sensitive applications.

Avoid maintaining a hardcoded list that becomes impossible to update.

### 6.4 Screen-sharing detection

Implement:

`ScreenShareDetector.kt`

IMPORTANT: Distinguish between:

1. A known remote-access application being installed.
2. A known remote-access application being foregrounded.
3. A known remote-access application having a running service, where verifiable.
4. An actual screen-capture session.

These are different security observations.

Use MediaProjection callbacks to monitor SafeX's own capture sessions, not to claim visibility into MediaProjection sessions created by unrelated applications.

Use additional permitted Android signals where available.

If actual screen sharing cannot be verified, return:

`NOT_VERIFIED`

Do not fabricate an ACTIVE status from the presence of an installed remote-access application.

### 6.5 Risk correlation

Implement:

`DetectRemoteAccessRiskUseCase.kt`

Evaluate:

- Remote-support application status.
- Foreground application status.
- Financial application foreground status.
- SMS application foreground status.
- Active call information.
- User permissions.
- Available screen-sharing evidence.

Example:

A known remote-support application is foregrounded while the user opens a banking application during an active phone call.

This should produce a high-priority security warning if the available evidence supports it.

### 6.6 Critical warning barrier

Create:

`RemoteAccessWarningOverlay.kt`

Warning:

"CRITICAL RISK: A remote-access application may be exposing your screen.

The person controlling your device could potentially see passwords, OTPs, banking details and private information.

Stop the remote session immediately if you did not authorize it."

Hindi:

"गंभीर खतरा: कोई रिमोट एक्सेस ऐप आपकी स्क्रीन की जानकारी किसी अन्य व्यक्ति तक पहुँचा सकता है।

आपके OTP, पासवर्ड और बैंकिंग जानकारी उजागर हो सकती है।

यदि आपने इस सेशन की अनुमति नहीं दी है, तो इसे तुरंत बंद करें।"

Actions:

1. Open Application Details.
2. Open Android App Settings.
3. Return to Safety Dashboard.
4. Open Cyber Helpline Dialer.

Use:

`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`

with the correct application package URI.

Do not silently uninstall or force-stop third-party applications.

Explain how the user can terminate or uninstall the application themselves.

# 7. MODULE 03: DIGITAL ARREST & LAW ENFORCEMENT IMPERSONATION SHIELD

## Feature identity

Name: Digital Arrest Protection

Purpose: Identify common coercion patterns used by scammers impersonating Indian law enforcement, CBI, ED, Customs and other government authorities.

### 7.1 Detection engine

Implement:

`DigitalArrestHeuristicMatcher.kt`

Create an on-device heuristic analysis engine.

Initial fraud dictionary:

- Digital arrest.
- CBI officer.
- ED investigation.
- Customs parcel.
- Aadhaar linked to drugs.
- Narcotics bureau.
- Fake Supreme Court warrant.
- Skype interrogation.
- Video interrogation.
- RBI verification account.
- Transfer money for verification.
- Money laundering allegations.
- Arrest warrant.
- Police custody threats.
- Account freeze threats.
- Do not tell your family.
- Keep the call confidential.
- Send money to prove innocence.
- Share OTP for investigation.

Support Hindi and English initially.

Design the dictionary to support future Indian languages.

### 7.2 Language processing

Implement:

- Text normalization.
- Tokenization.
- Phrase matching.
- Multi-word pattern matching.
- Keyword weighting.
- Contextual scoring.
- Duplicate phrase suppression.
- Negation handling.
- False-positive reduction.

Do not classify a call as fraudulent merely because someone mentions CBI, police or Customs.

For example:

"I am preparing for a CBI examination" must not trigger a critical fraud warning.

### 7.3 Audio capture feasibility

Before implementation, investigate Android's actual audio-capture restrictions.

Ordinary third-party Android applications cannot freely access the internal audio of cellular phone calls.

Do not use undocumented APIs, root-only techniques or deceptive workarounds.

Implement the following architecture:

**Primary detection engine**

`DigitalArrestHeuristicMatcher.kt`

Accepts text from supported and permission-authorized sources.

**Optional local speech recognition**

`OnDeviceSpeechRecognitionEngine.kt`

Evaluate:

- Android SpeechRecognizer with supported on-device recognition.
- Vosk.
- Another suitable locally hosted speech-recognition model.

Only process microphone audio after explicit user consent and when the operating system permits it.

If a microphone-based monitoring foreground service is used:

- Request the required runtime permissions.
- Declare the appropriate foreground service type.
- Display the mandatory persistent notification.
- Respect microphone privacy indicators and OS restrictions.
- Stop recording immediately when monitoring is disabled.

Never claim to analyze cellular call audio if the actual implementation cannot access it.

### 7.4 Coercion confidence engine

Implement:

`CoercionRiskScorer.kt`

Return:

- Score: 0–100.
- Risk category.
- Matched indicators.
- Evidence summary.
- Detection source.
- Confidence level.

Suggested categories:

| Score | Classification |
|---|---|
| 0–24 | LOW |
| 25–49 | MODERATE |
| 50–74 | HIGH |
| 75–100 | CRITICAL |

Scoring must consider multiple indicators.

Examples of stronger indicators:

- Government impersonation combined with arrest threats.
- Forced video interrogation.
- Requests for financial transfers.
- Instructions to remain on a call.
- Threats against family members.
- Instructions to conceal the incident.

Make scoring deterministic and testable.

Do not use arbitrary random confidence values.

### 7.5 Warning interface

Implement:

`DigitalArrestWarningOverlay.kt`

Critical warning:

"LEGAL REALITY CHECK

Indian law enforcement does not conduct a legitimate arrest through a video call or ask citizens to transfer money to a so-called verification account.

You may be facing a DIGITAL ARREST SCAM.

Disconnect and independently verify the caller."

Hindi:

"कानूनी सच्चाई जानें

कोई भी वैध भारतीय कानून प्रवर्तन एजेंसी वीडियो कॉल के माध्यम से आपको गिरफ्तार नहीं करती और न ही सत्यापन के नाम पर पैसे ट्रांसफर करने को कहती है।

यह डिजिटल अरेस्ट साइबर धोखाधड़ी हो सकती है।

कॉल काटें और स्वतंत्र रूप से सत्यापन करें।"

The wording must not imply that all legitimate police communication over a call is impossible.

### 7.6 Warning actions

Provide:

- Disconnect Call guidance.
- Call 1930.
- Report Cybercrime.
- Notify Family.
- View Safety Checklist.

Official portal:

https://cybercrime.gov.in/

Open the portal through a user-initiated action.

### 7.7 Privacy

All supported analysis must run locally.

Do not upload:

- Audio recordings.
- Call transcripts.
- Phone numbers.
- Contact information.
- Private conversation content.

Store only the minimum evidence necessary for an incident record.

Allow users to disable transcript retention.

# 8. MODULE 04: ELDERLY PROTECTION & FAMILY SOS DISPATCHER

## Feature identity

Name: Family Guardian Protection

Purpose: Provide an emergency protection mechanism for elderly users and other family members who may be vulnerable to coercive financial scams.

### 8.1 Guardian management

Implement:

`GuardianEntity.kt`

Suggested fields:

- id
- guardianName
- phoneNumber
- relationship
- isVerified
- isPrimary
- isEnabled
- createdAt
- updatedAt

Create:

`GuardianDao.kt`

`GuardianRepository.kt`

`GuardianViewModel.kt`

Store phone numbers securely and avoid unnecessary contact synchronization.

### 8.2 Guardian setup UI

Create a Family Protection setup screen.

Features:

- Add guardian.
- Edit guardian.
- Delete guardian.
- Verify phone number.
- Set primary emergency contact.
- Enable or disable SOS.
- Select notification preferences.
- Test SOS message with explicit confirmation.

Require user consent before enabling automated notifications.

Explain precisely which incidents can trigger a family alert.

### 8.3 Trigger condition A: Digital arrest

Trigger evaluation when:

- A CRITICAL Digital Arrest event is detected through a supported source.
- The associated call is known to be ongoing.
- The event persists for the configured threshold, initially five minutes.
- The user has enabled Family Guardian Protection.

Do not assume a five-minute call duration alone proves fraud.

### 8.4 Trigger condition B: Suspicious APK

Implement:

`SuspiciousInstallationDetector.kt`

Evaluate available information:

- Application package.
- Installation source, where available.
- Package signing information.
- Application permissions.
- Known malicious package indicators.
- Risky accessibility capabilities.
- Suspicious installation patterns.

Important:

A sideloaded APK is not automatically malicious.

Classify it as suspicious only when sufficient indicators exist.

Use labels such as:

- UNKNOWN_SOURCE.
- SIDeloaded.
- SUSPICIOUS.
- HIGH_RISK.
- VERIFIED_MALICIOUS.

Only use VERIFIED_MALICIOUS when supported by trustworthy evidence.

### 8.5 Trigger condition C: Financial activity

Desired scenario:

A user makes a UPI transaction exceeding ₹5,000 within 15 minutes of a high-risk scam call.

However, ordinary Android applications cannot reliably inspect transaction details inside unrelated UPI applications.

Do not claim automatic transaction amount detection without a legitimate supported integration.

Implement a modular transaction-risk interface:

`FinancialActivitySignalProvider.kt`

Possible sources:

- Explicit user-reported transaction.
- User-confirmed transaction amount.
- Legitimate supported financial integration, if available.

When a reliable signal is unavailable, mark the trigger:

`NOT_CONFIGURED`

The risk engine may still use supported call and application correlation, but must not invent a transaction amount.

### 8.6 Family SOS dispatcher

Implement:

`FamilySosDispatcher.kt`

Responsibilities:

- Evaluate eligible SOS incidents.
- Check guardian configuration.
- Check consent.
- Prevent duplicate notifications.
- Generate unique incident IDs.
- Apply rate limiting.
- Record delivery attempts.
- Handle permission failures.
- Handle unavailable SMS service.
- Expose delivery status.

### 8.7 SMS implementation

Evaluate Android SMS permissions and current Google Play policies before choosing the implementation.

Do not silently send SMS unless the application is legally and policy-compliantly authorized to do so and the user has explicitly enabled this behavior.

Where direct SMS sending is unavailable, provide an SMS composer using `ACTION_SENDTO` and `smsto:` so the user can confirm sending.

Example message:

"SafeX SOS Alert:

A high-risk scam or suspicious financial activity indicator has been detected on your family member's device.

Please call them immediately to verify their safety.

Incident ID: [UUID]

This alert is generated by SafeX. Please independently verify the situation."

Do not send private transcripts or banking information to family contacts.

### 8.8 Rate limiting

Implement:

- Per-incident deduplication.
- Per-guardian cooldown.
- Maximum alert frequency.
- Persistent incident history.
- Retry handling.
- Delivery status tracking.

Never repeatedly dispatch SMS messages because of repeated accessibility events.

### 8.9 Incident database

Create:

`SecurityIncidentEntity.kt`

Suggested fields:

- incidentId
- incidentType
- severity
- threatScore
- detectionSource
- callerNumber, only where legitimately available
- evidenceSummary
- transcriptSnippet, optional and consent-dependent
- timestamp
- guardianNotificationStatus
- userAcknowledged
- incidentResolutionStatus

Implement database migrations.

Do not store complete call recordings.

Protect sensitive records and apply a configurable retention policy.

# 9. SHARED CORE DEFENSE ENGINE

All four modules must integrate into a unified threat management system.

Create:

`ThreatStateManager.kt`

Responsibilities:

- Maintain current security state.
- Receive module events.
- Correlate relevant indicators.
- Prevent duplicate incidents.
- Calculate combined risk.
- Publish state updates.
- Coordinate warning priorities.
- Trigger eligible SOS evaluations.
- Maintain incident lifecycle.

Use Kotlin StateFlow and SharedFlow.

Suggested states:

```kotlin
enum class ThreatLevel {
    SAFE,
    LOW,
    MODERATE,
    HIGH,
    CRITICAL,
    UNKNOWN
}
```

Suggested detection status:

```kotlin
enum class DetectionStatus {
    DETECTED,
    NOT_DETECTED,
    NOT_VERIFIED,
    NOT_CONFIGURED,
    PERMISSION_REQUIRED,
    ERROR
}
```

Suggested incident lifecycle:

```kotlin
enum class IncidentStatus {
    OBSERVED,
    EVALUATING,
    WARNING_DISPLAYED,
    USER_ACKNOWLEDGED,
    ESCALATED,
    RESOLVED
}
```

The engine must not treat UNKNOWN or NOT_VERIFIED as SAFE.

### Threat correlation example

Scenario:

- Active cellular call: DETECTED.
- PhonePe foreground: DETECTED.
- Known remote-support application foregrounded: DETECTED.
- Screen capture session: NOT_VERIFIED.
- Digital arrest indicators: HIGH.

Expected result:

The system should raise a high-priority warning based on the supported observations.

It must not claim that screen capture is confirmed.

# 10. CLEAN ARCHITECTURE & FILE STRUCTURE

Respect the existing project structure.

If the current architecture allows, organize the implementation as follows:

```text
com.safex
│
├── core
│   ├── accessibility
│   │   └── SafeXAccessibilityService.kt
│   │
│   ├── overlay
│   │   ├── OverlayManager.kt
│   │   ├── UpiWarningOverlay.kt
│   │   ├── RemoteAccessWarningOverlay.kt
│   │   └── DigitalArrestWarningOverlay.kt
│   │
│   ├── telephony
│   │   └── TelephonyCallStateManager.kt
│   │
│   ├── monitoring
│   │   ├── RemoteAccessMonitor.kt
│   │   ├── ScreenShareDetector.kt
│   │   └── SuspiciousInstallationDetector.kt
│   │
│   └── security
│       ├── ThreatStateManager.kt
│       └── ThreatCorrelationEngine.kt
│
├── data
│   ├── local
│   │   ├── SafeXDatabase.kt
│   │   ├── GuardianDao.kt
│   │   └── SecurityIncidentDao.kt
│   │
│   ├── entity
│   │   ├── GuardianEntity.kt
│   │   └── SecurityIncidentEntity.kt
│   │
│   └── repository
│       ├── GuardianRepositoryImpl.kt
│       └── SecurityIncidentRepositoryImpl.kt
│
├── domain
│   ├── model
│   │   ├── ThreatEvent.kt
│   │   ├── ThreatLevel.kt
│   │   └── SecurityIncident.kt
│   │
│   ├── usecase
│   │   ├── DetectUpiCollisionUseCase.kt
│   │   ├── DetectRemoteAccessRiskUseCase.kt
│   │   ├── EvaluateDigitalArrestUseCase.kt
│   │   ├── DetectSuspiciousInstallationUseCase.kt
│   │   └── DispatchFamilySosUseCase.kt
│   │
│   └── repository
│
├── presentation
│   ├── security
│   │   ├── SecurityAdvisorScreen.kt
│   │   ├── UpiProtectionScreen.kt
│   │   ├── RemoteAccessProtectionScreen.kt
│   │   ├── DigitalArrestScreen.kt
│   │   └── FamilyGuardianScreen.kt
│   │
│   ├── incident
│   │   └── IncidentHistoryScreen.kt
│   │
│   └── viewmodel
│       ├── SecurityViewModel.kt
│       └── FamilyGuardianViewModel.kt
│
└── di
    ├── SecurityModule.kt
    └── DatabaseModule.kt
```

This is a proposed structure, not permission to duplicate existing classes.

Reuse existing repositories, database modules, navigation and ViewModels wherever possible.

# 11. USER INTERFACE & EXPERIENCE REQUIREMENTS

The four modules must feel like native parts of SafeX.

Do not create a separate application or disconnected demonstration screens.

### Security dashboard

Create a unified Core Defense dashboard showing:

1. UPI Protection.
2. Remote Access Protection.
3. Digital Arrest Protection.
4. Family Guardian Protection.

Each module should display:

- Current status.
- Protection enabled/disabled.
- Required permissions.
- Last detection.
- Latest incident.
- Risk severity.
- Configuration action.

### Status representation

Use clear labels:

- PROTECTED.
- ACTIVE.
- WARNING.
- CRITICAL.
- PERMISSION REQUIRED.
- NOT VERIFIED.
- NOT CONFIGURED.
- ERROR.

Never show PROTECTED when the required monitoring service is disabled.

### Design requirements

- Premium SafeX visual identity.
- Material 3.
- Consistent typography.
- Professional security-oriented color hierarchy.
- Clear warning hierarchy.
- Responsive layouts.
- Accessible contrast.
- Proper loading states.
- Empty states.
- Permission explanation screens.
- Error and recovery states.
- Light and dark theme compatibility.

Avoid unnecessary animations and decorative elements that delay access to emergency controls.

Security warnings must prioritize clarity and immediate action.

# 12. ANDROID MANIFEST & PERMISSION ENGINEERING

Audit and implement the required Android configuration.

Potential permissions and components must be evaluated individually against the project's Android SDK and distribution requirements.

Evaluate:

- `READ_PHONE_STATE`
- `POST_NOTIFICATIONS`
- `SYSTEM_ALERT_WINDOW`, if applicable.
- Accessibility service declaration.
- Usage access settings.
- `RECORD_AUDIO`, only if genuinely required.
- Foreground service declarations and types.
- SMS permissions, only if policy-compliant and genuinely required.
- Package visibility declarations.

Create or update:

`AndroidManifest.xml`

`res/xml/accessibility_service_config.xml`

For AccessibilityService:

- Correct exported configuration.
- Appropriate binding permission.
- Accurate event types.
- Minimal package filtering.
- Appropriate feedback type.
- Proper description.
- User-facing accessibility disclosure.

Do not request every permission at application startup.

Use contextual permission requests.

For each permission, explain:

1. Why SafeX needs it.
2. What functionality it enables.
3. What happens when permission is denied.
4. How the user can revoke it.

Never bypass Android's permission system.

# 13. SECURITY, PRIVACY & DATA PROTECTION

This application processes extremely sensitive security information.

Implement privacy by design.

Mandatory rules:

1. All supported threat analysis happens locally.
2. No audio or transcripts are uploaded.
3. No OTP or UPI PIN collection.
4. No financial credentials are stored.
5. No unnecessary contact access.
6. No hidden recording.
7. No silent application installation.
8. No unauthorized application termination.
9. No undisclosed SMS dispatch.
10. No third-party transmission of incident evidence.

Implement:

- Secure preference storage where appropriate.
- Database access controls.
- Sensitive-data minimization.
- Log redaction.
- Configurable retention.
- Permission revocation handling.
- Safe exception handling.

Never log complete phone numbers, transcripts, OTPs or private financial details.

# 14. BATTERY & PERFORMANCE

The system must remain lightweight.

Requirements:

- No busy-waiting.
- No unnecessary polling.
- No continuous accessibility-tree traversal.
- No permanent microphone capture by default.
- No unnecessary background services.
- No unbounded coroutine scopes.
- No main-thread database access.
- No duplicate event processing.
- No excessive overlay creation.

Use:

- StateFlow.
- SharedFlow.
- Structured concurrency.
- Lifecycle-aware collection.
- Dispatchers.IO.
- Event-driven Android callbacks.
- Debouncing and throttling where appropriate.

Implement correct cancellation and service cleanup.

# 15. TESTING REQUIREMENTS

Do not consider implementation complete merely because the project compiles.

Create automated tests.

### Module 1 tests

- UPI application opened without a call.
- UPI application opened during a cellular call.
- Call disconnected while UPI remains open.
- Accessibility permission revoked.
- Repeated accessibility events.
- Overlay displayed only once.
- Overlay cleanup after dismissal.

### Module 2 tests

- Known remote-support package installed.
- Remote-support application foregrounded.
- Financial application opened during a suspicious session.
- Unknown remote-support application.
- Usage access unavailable.
- Screen-sharing status unavailable.
- Application removed during monitoring.

### Module 3 tests

Test phrases:

- "I am calling from CBI. Your Aadhaar is linked to a drug parcel."
- "You are under digital arrest."
- "Transfer money to the RBI verification account."
- "Do not inform your family."
- "You have been selected for a CBI examination."

The first four should produce appropriately elevated scores when combined with relevant context.

The last example must not automatically become a critical fraud incident.

Test:

- Hindi phrases.
- English phrases.
- Mixed Hindi-English.
- Negation.
- Duplicate phrases.
- Partial phrases.
- False positives.
- Empty transcripts.

### Module 4 tests

- No guardian configured.
- Guardian configured but SOS disabled.
- Guardian verification incomplete.
- Eligible critical incident.
- Repeated incident.
- SMS permission unavailable.
- SMS service unavailable.
- Duplicate alert prevention.
- Rate limiting.
- Database persistence.
- User revokes consent.

### Integration tests

Test combined scenarios involving all four modules.

### UI tests

Test:

- Dashboard.
- Permission onboarding.
- Warning overlays.
- Guardian setup.
- Incident history.
- Error states.
- Accessibility and large font support.

# 16. DELIVERABLES

Implement the complete feature, including the following components where appropriate:

1. `SafeXAccessibilityService.kt`
2. `TelephonyCallStateManager.kt`
3. `ThreatStateManager.kt`
4. `ThreatCorrelationEngine.kt`
5. `UpiWarningOverlay.kt`
6. `RemoteAccessWarningOverlay.kt`
7. `DigitalArrestWarningOverlay.kt`
8. `RemoteAccessAppRegistry.kt`
9. `RemoteAccessMonitor.kt`
10. `ScreenShareDetector.kt`
11. `DigitalArrestHeuristicMatcher.kt`
12. `CoercionRiskScorer.kt`
13. `SuspiciousInstallationDetector.kt`
14. `GuardianEntity.kt`
15. `SecurityIncidentEntity.kt`
16. `GuardianDao.kt`
17. `SecurityIncidentDao.kt`
18. `FamilySosDispatcher.kt`
19. Required repositories.
20. Required domain use cases.
21. Required ViewModels.
22. Required Compose screens.
23. Required Hilt modules.
24. AndroidManifest.xml changes.
25. Accessibility configuration XML.
26. Database migrations.
27. Unit and integration tests.
28. Documentation.

These filenames are not mandatory if equivalent existing implementations already exist.

The deliverable is a working integrated feature, not a collection of disconnected files.

# 17. IMPLEMENTATION WORKFLOW

Follow this exact sequence.

### PHASE 1 — Repository understanding

Inspect the complete project.

Identify architecture, existing features, dependencies and conflicts.

Produce the audit document.

Do not modify unrelated files.

### PHASE 2 — Technical feasibility

For every requested capability, document:

- Available Android API.
- Required permission.
- Minimum API level.
- Android restrictions.
- Play Store policy considerations.
- Actual achievable behavior.
- Fallback behavior.

Classify each capability:

`IMPLEMENTABLE`

`IMPLEMENTABLE_WITH_PERMISSION`

`PARTIALLY_SUPPORTED`

`NOT_SUPPORTED_BY_STANDARD_ANDROID_APIS`

Do not skip this phase.

### PHASE 3 — Core architecture

Implement shared event models, threat states, correlation engine, repositories and dependency injection.

Ensure the four modules communicate through defined interfaces.

### PHASE 4 — Module implementation

Implement the four modules individually.

Do not implement only their interfaces or visual screens.

Connect every module to its real detection source.

### PHASE 5 — UI integration

Integrate all screens into the existing SafeX navigation.

Reuse the existing design system.

Make all actions functional.

### PHASE 6 — Permission and lifecycle integration

Implement permission onboarding, revocation handling, service lifecycle, foreground execution requirements and recovery states.

### PHASE 7 — Testing

Run unit tests, integration tests and available UI tests.

Fix failures before proceeding.

### PHASE 8 — Build verification

Execute the appropriate Gradle tasks.

At minimum, evaluate:

- `assembleDebug`
- `testDebugUnitTest`
- `lint`

Run additional instrumented tests when the environment supports them.

Resolve all compilation errors introduced by your changes.

### PHASE 9 — Final integration audit

Check:

- No broken navigation.
- No duplicate services.
- No duplicate database entities.
- No manifest conflicts.
- No missing Hilt bindings.
- No incorrect permission assumptions.
- No hardcoded fake detection results.
- No placeholder buttons.
- No misleading security statuses.
- No memory leaks.
- No unhandled service lifecycle problems.

# 18. STRICT ENGINEERING RULES

These rules are non-negotiable.

**RULE 1 — NO FAKE IMPLEMENTATIONS**

Never create a UI that claims to detect an attack when no real detection mechanism exists.

**RULE 2 — NO PLACEHOLDER FUNCTIONALITY**

Every delivered button, switch, action and configuration control must work.

**RULE 3 — NO ARCHITECTURE DESTRUCTION**

Do not unnecessarily replace existing SafeX architecture.

**RULE 4 — NO SECURITY THEATRE**

An installed remote-support app does not automatically mean an active remote session.

An active phone call does not automatically mean fraud.

A sideloaded application does not automatically mean malware.

A keyword match does not automatically mean a digital arrest scam.

**RULE 5 — NO ANDROID RESTRICTION BYPASS**

Do not use undocumented APIs, root access, hidden permissions or unauthorized monitoring.

**RULE 6 — HONEST DETECTION STATES**

Use NOT_VERIFIED, NOT_CONFIGURED and PERMISSION_REQUIRED whenever appropriate.

**RULE 7 — REAL INTEGRATION**

The modules must work together inside the existing SafeX application.

**RULE 8 — PRESERVE USER CONTROL**

The user must understand and control sensitive permissions, monitoring, family alerts and data retention.

**RULE 9 — HANDLE FAILURES**

Every monitoring service must handle permission revocation, unavailable APIs, lifecycle changes and runtime exceptions.

**RULE 10 — VERIFY BEFORE CLAIMING COMPLETION**

Do not claim that a module is complete unless its implementation, integration and available tests support that claim.

# 19. PROGRESS TRACKING

Create:

`SAFEX_CORE_DEFENSE_PROGRESS.md`

Maintain it throughout development.

For each task, record:

- Task name.
- Files created.
- Files modified.
- Implementation status.
- Android API used.
- Permissions required.
- Tests performed.
- Build status.
- Known limitations.

Use the following status values:

- NOT_STARTED
- IN_PROGRESS
- IMPLEMENTED
- TESTED
- BLOCKED
- PARTIALLY_SUPPORTED

Do not mark a task TESTED merely because code was written.

# 20. FINAL ACCEPTANCE CRITERIA

The implementation will only be accepted when:

1. All four modules exist within SafeX.
2. Each module has an actual detection or monitoring mechanism.
3. Android permission requirements are implemented correctly.
4. Threat correlation works through a shared engine.
5. Warning overlays have functional lifecycle management.
6. Digital arrest detection has deterministic, tested scoring.
7. Family Guardian setup works with persistent storage.
8. SOS dispatch respects consent and Android restrictions.
9. Incidents are persisted locally.
10. Security states accurately reflect detection limitations.
11. Existing SafeX features continue working.
12. Navigation and UI integration are complete.
13. Automated tests cover critical scenarios.
14. Gradle build and available static checks pass.
15. Documentation describes what is genuinely supported and what is not.

# 21. FINAL RESPONSE FORMAT

When development is complete, provide a detailed engineering report containing:

**A. Implementation Summary**
- What was implemented in each module.

**B. Files Created**
- Complete file paths and purposes.

**C. Files Modified**
- Existing files changed and reasons.

**D. Android Permissions**
- Required permissions.
- Their purpose.
- Manual user actions required.

**E. Detection Capability Matrix**
For every feature, show:

- Actual detection mechanism.
- Android API used.
- Detection limitations.
- Implementation status.

**F. Testing Report**
- Unit tests.
- Integration tests.
- UI tests.
- Build results.
- Lint results.

**G. Manual Testing Instructions**
Explain exactly how to test each module on a physical Android device.

**H. Known Limitations**
Clearly identify anything Android does not permit or that still requires additional integration.

**I. Remaining Work**
List anything not implemented or not verified.

---

# FINAL INSTRUCTION

Think like a security architect, implement like a senior Android engineer, and validate like a security auditor.

Do not rush into writing code.

Do not sacrifice correctness to satisfy the requested feature list.

Do not make the dashboard appear more capable than the underlying implementation.

Your mission is to transform SafeX into a genuinely useful, privacy-first Android anti-fraud protection system that can identify suspicious activity, warn users at the right moment, guide them toward safety and help protect vulnerable family members.

**BUILD THE REAL SECURITY ENGINE. NOT A SIMULATION OF ONE.**