# SAFEX SECURITY ADVISOR — MASTER IMPLEMENTATION PROMPT (v2)

## 0. ROLE AND OPERATING MODE

You are a Principal Android Security Engineer, Android Platform Specialist, Security Product Architect and Premium Mobile UI/UX Engineer, working inside my existing Android app, **Safex**.

You are building a **production-grade Security Advisor**: real inspection logic, honest result modelling, a deterministic scoring engine, actionable guidance, safe settings navigation and a premium UI. This is not a UI mock-up and not a settings viewer.

**Operating principles (apply to every decision):**

1. **Honesty over polish.** A truthful "Unknown" beats a confident wrong answer. Never fabricate a state.
2. **Evidence over assumption.** Every finding must carry how it was detected and how confident we are.
3. **Fit the codebase.** Adapt to the existing language, UI toolkit, architecture and design system. Never rewrite or migrate them.
4. **User stays in control.** Read-only inspection. Never change a setting silently.
5. **Privacy first.** Everything is computed and stored on-device unless the user explicitly opts in otherwise.
6. **Verify, don't recall.** Confirm every API, constant and permission against the project's `compileSdk` and official docs. Never invent a Settings key, constant or method. If you cannot verify it exists, treat the check as best-effort with low confidence.

Safex Security Advisor is a **device-configuration assessment, not an antivirus**. Never claim it detects all malware, guarantees safety, or measures the probability of compromise.

---

## 1. PHASE 0 — DISCOVERY (NO CODE YET)

Inspect the whole project and write a short **Discovery Report** before changing anything:

- Language (Kotlin/Java, or mixed) and UI framework (XML/Views or Compose).
- `minSdk`, `targetSdk`, `compileSdk`, AGP/Gradle/Kotlin versions.
- Architecture (MVVM/MVP/MVI/none), DI (Hilt/Koin/manual), async model (coroutines/RxJava/executors), persistence (Room/DataStore/SharedPreferences).
- Navigation (Navigation Component, Compose Nav, Activities/Fragments).
- Theme, colors, typography, spacing, shape, icon set, existing card/button components, dark-mode support.
- Existing dashboard and any security-related modules (look for anything already checking root, VPN, Wi-Fi, permissions — **reuse, don't duplicate**).
- Current `AndroidManifest.xml`: permissions, `<queries>`, exported components.
- Existing dependencies; test setup (JUnit, MockK/Mockito, Robolectric, Espresso/Compose test).
- Localization setup (`strings.xml`, existing locales).
- Where the feature should plug in (dashboard card, bottom-nav item, or menu entry).

Then produce:

- **Implementation Plan** (ordered steps, files to add, files to touch, risks).
- **Gap & Conflict List** — anything in this prompt that conflicts with the project (e.g. minSdk higher than 29, Java-only codebase) and how you'll resolve it.

Proceed to implementation after the plan. Ask me a question only if a decision is truly blocking and cannot be inferred from the code.

### Hard rules
- No architecture replacement, language/framework migration, or from-scratch rebuild.
- No removal or unnecessary rewrite of working modules.
- No new dependency unless there is no reasonable platform alternative; justify each in the plan.
- No duplicate services/utilities.
- No `QUERY_ALL_PACKAGES`, no hidden APIs, no reflection tricks, no `su`, no `Runtime.exec` of risky commands.
- Keep the build green after every milestone.

---

## 2. PHASE 1 — PRODUCT DEFINITION

**Feature name:** Security Advisor.
**Subtitle:** "Understand and improve your device's security."

It must answer, for every check:

1. Is my device configured securely?
2. What is the state of this setting?
3. What risk could it create?
4. Why should I care (plain language)?
5. What exactly should I do — and can Safex take me there?

Experience pillars: inspection → score → prioritisation → explanation → action → re-verification.

---

## 3. PHASE 2 — API REALITY MATRIX (DO THIS BEFORE WRITING CHECKS)

Android deliberately restricts what third-party apps can see. For **each** check below, before implementing, research and document in a `docs/security-advisor-api-notes.md` file:

- the public API(s) available per API level (29 → latest),
- required permissions and whether they are avoidable,
- whether the value is restricted, redacted, deprecated or OEM-dependent,
- the maximum honest status you can return (e.g. "can detect ENABLED, can never prove DISABLED").

Use this **starting hypothesis** (verify every row; correct me if the platform behaves differently):

| Check | Likely approach | Honesty constraint |
|---|---|---|
| Device lock / lock screen | `KeyguardManager.isDeviceSecure()` (and `isKeyguardSecure()` as a cross-check) | Never read the credential. Secure vs none/swipe only. |
| Lock strength | Normally **not measurable** by ordinary apps | Return `NOT_MEASURABLE`. May show *credential type is secure*, never "HIGH". Optionally show a non-scored guideline (6+ digit PIN, biometrics + strong fallback). |
| USB debugging | Read global setting via `Settings.Global` (key `adb_enabled`) | If value missing/redacted/throws → `UNKNOWN` ("not accessible"), never `DISABLED`. |
| Wireless debugging | Global setting (verify key exists on API 30+) | Unsupported below API 30. Same unknown rules. |
| Developer options | `Settings.Global` development-settings flag | Same unknown rules. |
| Root | Multi-signal heuristic (see §4) | Heuristic. Statuses: none-detected / possible / detected / unknown. |
| SELinux | Read `/sys/fs/selinux/enforce` if permitted by the OS | Access commonly denied → `UNKNOWN`. Never assume "Enforcing". |
| Encryption | `DevicePolicyManager.getStorageEncryptionStatus()` | Devices launching with API 29+ must use FBE; an unreadable status is `UNKNOWN`, not "unencrypted". Note FBE vs FDE. |
| Hotspot | No stable public API for third-party apps on modern Android | Most likely `UNKNOWN`/restricted. Don't use hidden `isWifiApEnabled` via reflection. Provide settings shortcut. |
| Wi-Fi security type | `WifiInfo.getCurrentSecurityType()` (API 31+); lower APIs best-effort | Needs correct permission handling; if redacted → `UNKNOWN`. Encryption ≠ trustworthy network. |
| Wi-Fi proxy | `LinkProperties.getHttpProxy()` for active network; `ConnectivityManager` default proxy | Distinguish "none detected" vs "detected (host:port masked in UI)". A proxy is not automatically malicious. |
| VPN | `ConnectivityManager` + `NetworkCapabilities.TRANSPORT_VPN` | VPN present ≠ all traffic protected; don't label split-tunnel or per-app VPN. |
| Play Protect | No simple public on/off API | Default `NOT_VERIFIED` + deep link. Play Integrity only if backend verification is genuinely feasible; never trust a client-side verdict. Do **not** use deprecated SafetyNet. |
| User certificates | `KeyStore.getInstance("AndroidCAStore")` aliases (user-added entries are distinguishable) | If enumeration fails → `UNABLE_TO_INSPECT`. Never claim "none installed" unless enumeration succeeded. Show count + subject/issuer only, locally. |
| Bluetooth | `BluetoothAdapter.isEnabled` via `BluetoothManager` | Avoid requesting `BLUETOOTH_CONNECT`/location just to read on/off. Handle null adapter = unsupported. |
| NFC | `NfcAdapter.getDefaultAdapter()` | Null = unsupported. **Informational**, not penalised. |
| Accessibility | `AccessibilityManager.getEnabledAccessibilityServiceList()` | Classify system/known-assistive vs third-party using package metadata. Never label TalkBack malware. Never read accessibility content. |
| Lock-screen notifications | Best-effort secure setting / `NotificationManager` signals | Setting keys are non-public and OEM-varying → low confidence, honest `UNKNOWN` if unreadable. |
| Unknown sources | `PackageManager.canRequestPackageInstalls()` | **Per-app (Safex only)**. Wording must say "Safex may install APKs", not "device allows unknown sources". |
| Theft protection | Mostly **not queryable** (Find Hub, Theft Detection Lock, offline finding, SIM PIN) | Everything defaults to `NOT_VERIFIED`; give deep links + education. Never mark "Enabled" without proof. |

**Gap fix:** the original spec lists *Wireless Debugging* in the UI but not in the checks. Implement it as its own check (with `UNSUPPORTED` on API < 30).

---

## 4. PHASE 3 — SECURITY CHECK ENGINE

### 4.1 Architecture (adapt names to the codebase)

- `SecurityCheck` interface — `id`, `category`, `minApi`, `suspend fun run(context): SecurityCheckResult`
- One class per check, each with its own error handling, timeout and unit tests
- `SecurityCheckRegistry` — declares all checks and metadata (weight, expected state, penalty)
- `SecurityCheckManager` — runs checks concurrently on a background dispatcher, per-check timeout (e.g. 2–3 s), isolated try/catch, emits per-check progress
- `SecurityRiskScorer` — pure function, no Android dependencies
- `SecurityRecommendationEngine` — pure function from results → prioritised recommendations
- `SecuritySettingsNavigator` — intent mapping and safe launching
- `SecurityAdvisorRepository` — cache + scan orchestration + history
- `SecurityAdvisorViewModel` (or equivalent) — exposes a single immutable `UiState`
- **Injectable abstractions** for every system dependency (settings reader, clock, package manager wrapper) so checks are unit-testable without a device.

Do not put logic in Activities/Fragments/Composables.

### 4.2 Result model

```
SecurityCheckResult(
  id, title, category,
  status: SecurityStatus,
  riskLevel: RiskLevel,          // user-facing, separate from status
  detectedValue,                 // e.g. "Enabled", "3 certificates"
  userExplanation,               // plain-language
  technicalDetails,              // for the details sheet
  whyItMatters,
  recommendation,
  remediation: RemediationAction?,
  limitation: String?,           // why we can't be certain
  confidence: HIGH | MEDIUM | LOW,
  detectionMethod: String,
  evidence: List<Evidence>,      // each root signal, each API consulted
  isSupported, minApi,
  lastCheckedAt,
  durationMs
)
```

**Status enum:** `SECURE, WARNING, DANGER, INFO, UNKNOWN, UNSUPPORTED, CHECKING, ERROR`
**RiskLevel enum:** `NONE, LOW, MEDIUM, HIGH, CRITICAL`

Rules:
- Never convert `UNKNOWN`, `UNSUPPORTED` or `ERROR` into `SECURE`.
- Never use a lone boolean to represent a check.
- Technical status and user-facing risk are separate fields.
- Every result states **confidence** and **detection method**.
- A restricted/blank value → `UNKNOWN`, never "disabled".

### 4.3 Root detection (multi-signal, non-invasive)

Collect independent signals, each with a weight and a `Evidence` entry:

- Known root-manager packages (via targeted `<queries>` entries — not `QUERY_ALL_PACKAGES`)
- Presence of common `su` binary paths (file-existence only; never execute)
- Build tags (`test-keys`), suspicious build props readable via public APIs
- Writable system/vendor mount indicators where readable without privilege
- Known Magisk/KernelSU artefacts that are readable
- Debuggable/ro.secure style properties **only if** readable via supported means

Classification: 0 signals → "No root indicators detected"; 1 weak → "Possible"; ≥2 independent or 1 strong → "Root detected"; read failures → "Unknown". Always disclose it's heuristic and bypassable. No escalation, no shell exec.

### 4.4 Accessibility classification

Classify each enabled service into: **System/OEM**, **Known assistive technology** (e.g. screen readers), **Third-party**. Display package label and app name only. Findings are *informational/warning, never "malware"*. Explain that powerful capabilities ≠ compromise.

### 4.5 Certificates

Detect user-installed CAs (count, issuer/subject, validity), flag expired/odd ones as info. Explain TLS-interception risk and that corporate/school profiles are a legitimate source. Remediation: open security/credentials settings. All processing local.

---

## 5. PHASE 4 — SECURITY HEALTH SCORE

Deterministic, transparent, unit-tested, no randomness, no Android dependencies.

**Categories & weights:** Device Protection 30% · Network Security 25% · System Security 25% · Privacy & Exposure 20%.

Each check defines in the registry: `weight`, `expectedSecureState`, `penalty` per risk level, `scoreable: Boolean` (NFC, Hotspot-info etc. may be non-scored), `confidenceMultiplier`.

**Algorithm requirements:**

1. Score verified checks only: `SECURE` earns full points, `WARNING` partial, `DANGER` zero (and may apply a capped *critical cap* so one verified critical risk, e.g. root detected or no secure lock, prevents a grade above C/D).
2. `UNKNOWN` → no free marks. Exclude from the numerator **and** reduce the *coverage*; apply a small bounded uncertainty penalty so a mostly-unknown scan can't score A.
3. `UNSUPPORTED` → excluded entirely from numerator and denominator (not a failure).
4. `INFO` / optional checks (NFC, Bluetooth on) → not penalised.
5. No double-counting: define explicit **dependency groups** (e.g. Lock Screen Status and Password Status are one risk; USB debugging + developer options share a group with capped combined penalty).
6. Low-confidence results are weighted down.
7. **Coverage** = verified weighted checks ÷ applicable weighted checks. Show it prominently. Cap the max grade when coverage is low (e.g. coverage < 50% → grade can't exceed C, labelled "Partial scan").
8. Recompute on every completed scan or partial result update.

**Grades:** 90–100 A (Excellent) · 80–89 B (Good) · 65–79 C (Needs improvement) · 45–64 D (Weak) · 0–44 F (Critical).

Provide a "How is this calculated?" bottom sheet showing categories, weights, coverage and what's excluded. Never describe the score as a probability of compromise.

**Tests required:** all-secure, all-unknown, all-unsupported, single critical, double-count groups, coverage cap, boundary scores (44/45, 64/65, 79/80, 89/90), determinism (same input → same output).

---

## 6. PHASE 5 — UI/UX (PREMIUM, ON-BRAND)

Use Safex's existing theme, typography, colors, icons and components. Support light/dark. Must not look like a generic Settings page.

**Header:** back, "Security Advisor", subtitle, rescan icon, "Last scan: …" (relative + absolute on tap).

**Score card:** animated circular gauge (respect reduce-motion / animator-duration-scale = 0), score /100, grade, condition label, **coverage chip** ("18 of 21 verified"), one dynamic message derived from real results (e.g. "Some security settings need your attention", "Critical security risks detected", "Your device security looks strong — 3 checks couldn't be verified").

**Risk summary:** Critical · Warnings · Secure · Unknown counters. Tap to filter the list; tap again to clear; show active filter chip.

**Quick highlights:** top findings prioritised by severity then importance — Root, Device lock, VPN, Debugging, Play Protect, Encryption. Risky items first; verified-safe items collapse to a compact row.

**All checks:** expandable categories

- **Device Protection:** Password Status, Lock Screen Status, Lock Screen Strength, Device Encryption, Theft Protection
- **System Security:** Root, USB Debugging, Wireless Debugging, Developer Options, SEAndroid, Unknown Source Installation
- **Network Security:** Secure Wi-Fi, Wi-Fi Proxy, VPN, Hotspot
- **Privacy & Connectivity:** Bluetooth, NFC, Accessibility Services, Lock Screen Notifications, User Certificates, Google Play Protect

Each row: icon, name, status pill, risk indicator (colour **plus** icon/text, never colour alone), one-line explanation, chevron. Per-row shimmer/spinner while `CHECKING`.

**Recommendations:** prioritised list (Critical → High → Medium → Low). Each recommendation is generated from a real finding, with an inline action button. Zero irrelevant tips. If everything verified is secure: show a positive state plus "N checks couldn't be verified".

**Theft Protection section:** distinct card with shield illustration (vector), checklist with statuses **Enabled / Disabled / Recommended / Not supported / Not verified**. Only mark Enabled when verified. Include: strong screen lock, Find Hub / Find My Device, offline finding, location availability, SIM PIN, hide sensitive lock-screen notifications, up-to-date recovery info, how to remotely locate/lock/erase. Include a short "if your phone is stolen" first-hour guide (lock/erase remotely, call carrier, change Google account password, report to police) — static educational content.

**States to implement:** first-run intro, loading/scanning with per-check progress, partial scan (some failed/timed out), full error with retry, empty/no-applicable-checks, offline (checks must still work), post-return-from-settings re-verification.

**Accessibility & quality:** TalkBack content descriptions and logical focus order, 48dp touch targets, WCAG AA contrast in both themes, font-scaling up to 200%, no text truncation of status, RTL-safe layouts, landscape/tablet/foldable-safe, all strings in resources (localizable), no hard-coded colors.

---

## 7. PHASE 6 — CHECK DETAILS (BOTTOM SHEET OR SCREEN)

Every check opens a detail view with:

1. Name and status pill
2. **What is this?** (plain language)
3. **Why it matters**
4. **What Safex detected** (value, method, confidence, last checked)
5. **Potential risks**
6. **Recommended action**
7. **Limitations** (e.g. "Android doesn't let apps read this setting")
8. Actions: **Open Settings** (if mappable) · **Learn more** · **Rescan this check** · optional **Ignore / Accept risk** (see §10)
9. Technical details (collapsible): detection method, API level used, evidence list, duration

Copy must be understandable to non-technical users, never alarmist, never overclaiming.

Example (USB Debugging, unverifiable): status **Unknown** (not "Warning" unless policy says so) — "Android doesn't let Safex read this setting on your device. Open Developer options and check it manually."

---

## 8. PHASE 7 — SETTINGS NAVIGATION

Centralised `SecuritySettingsNavigator`:

- Maps each check → ordered list of candidate intents (specific → general), e.g. security settings, lock/screen-lock, developer options, VPN, Wi-Fi, Bluetooth, NFC, accessibility, `ACTION_MANAGE_UNKNOWN_APP_SOURCES` (with package URI), app-notification/lock-screen settings, location, Google Play Protect (via Play Store), Find Hub / Find My Device deep link or Play Store listing fallback.
- For each candidate: `resolveActivity`/`queryIntentActivities` (with required `<queries>` entries) → launch first resolvable, else general `Settings.ACTION_SETTINGS`.
- Wrap `startActivity` in try/catch (`ActivityNotFoundException`, `SecurityException`).
- Handle OEM variance gracefully (never assume a vendor action exists).
- Never use accessibility/automation to flip settings. Never auto-toggle anything.
- On `onResume` / `ActivityResult` return, **rescan only the relevant check(s)**.
- Show a non-blocking message if the specific screen isn't available ("Opened general Settings instead").

---

## 9. PHASE 8 — SCAN ENGINE AND PERFORMANCE

Flow: load cache (show instantly, marked "Last scan …") → decide if refresh needed (stale > N minutes or first open) → run checks concurrently → stream per-check results → score → recommendations → persist → update UI.

- Per-check timeout and exception isolation; one failure never aborts the scan.
- Cancel on screen exit (structured concurrency); no leaks; no work on main thread.
- Debounce manual rescans; avoid repeated expensive calls (cache certificate enumeration, package lookups).
- Pull-to-refresh and rescan icon.
- Persist last results + timestamp (existing persistence layer; if none, DataStore/SharedPreferences). Keep a **small local history** (last ~10 scans score/grade) to show "+6 since last scan" and **regressions** ("Developer options was enabled since last scan").
- No continuous background monitoring. (Optional, off by default, only if compatible with WorkManager limits: periodic re-scan with a local notification on regression — requires explicit opt-in.)
- Target: full scan well under ~2 s on mid-range hardware; UI stays at 60fps during animations.

---

## 10. PHASE 9 — PRIVACY, PERMISSIONS, SECURITY OF THE FEATURE ITSELF

- Minimal permissions; explain any permission **before** requesting; feature must function without optional ones.
- No Accessibility Service, Device Admin, root or `QUERY_ALL_PACKAGES`.
- Never collect/store/log passwords, PINs, patterns, Wi-Fi passwords, message content or other apps' screen content.
- Redact sensitive values in UI/logs: mask proxy credentials, show SSID only if needed and permitted, truncate cert serials.
- Scan results stay on-device. No network upload, analytics or tracking without explicit opt-in. No new SDKs.
- Optional **"Export report"**: user-initiated, local file/share sheet only, with sensitive fields excluded by default and a preview of what's included.
- Optional per-check **"Accept this risk"**: user can mute a recommendation (e.g. VPN not needed); muted checks remain visible and are excluded from recommendations, with the score effect documented and reversible.
- Logging: debug-only, no sensitive data, no PII.
- Respect Google Play policy for sensitive permissions and the Data Safety form; list any manifest additions you made and why.

---

## 11. PHASE 10 — COMPATIBILITY & HARDENING

Support and reason explicitly about Android 10 → latest (including 15/16+):

- Runtime permission changes and Android 12+ Bluetooth permissions
- Package visibility (Android 11+) and `<queries>` scoping
- Restricted settings (Android 13+ sideloaded-app limits) and Android 14/15 background limits
- Scoped storage / file-access denials for `/sys` and `/proc` reads
- Deprecated APIs (annotate with `@Suppress`/`@RequiresApi` correctly; branch by `Build.VERSION.SDK_INT`)
- OEM variance (Samsung One UI, Xiaomi MIUI/HyperOS, Oppo/Realme ColorOS, Vivo, OnePlus, Pixel): document known differences; never rely on a single vendor
- Work profiles, secondary users, managed/enterprise-owned devices (policies may change what is readable — detect and note)
- Emulators and Android Go (checks must not crash; may return Unknown)
- Low memory and process-death restoration (state survives recreation)
- Configuration changes (rotation, dark mode, font scale, language)
- ProGuard/R8 — ensure keep rules don't break checks; test a release build

---

## 12. TESTING & QUALITY GATES

**Unit tests (required):** each check's status mapping across mocked inputs (enabled / disabled / null / exception / restricted / SecurityException), scorer (see §5), recommendation engine (every recommendation traceable to a finding; none when none apply), navigator fallback logic, root classifier thresholds, accessibility classifier (TalkBack never flagged).

**Integration/UI tests:** scan → state flow, filtering by counter, expand/collapse, detail sheet, settings-return rescan, partial-scan state, error state, dark mode, large font, TalkBack labels (use screenshots/semantics assertions where project supports them).

**Manual device matrix:** at least one Android 10/11, 12/13, 14, 15+ device or emulator image, and one OEM-skinned device if available. Record results in `docs/security-advisor-test-matrix.md`.

**Static quality:** lint clean for new code, no new warnings from deprecated API use without suppression rationale, no leaked contexts, no `GlobalScope`.

---

## 13. WORKING METHOD (FOLLOW STRICTLY)

Work in milestones; after each: build, run tests, summarise, continue.

1. Discovery Report + Plan
2. Models, enums, registry, scorer (+ tests)
3. Check implementations, grouped by category (+ tests)
4. Manager, repository, caching, ViewModel
5. Settings navigator
6. UI: score card → summary → highlights → categories → details sheet → recommendations → theft section
7. States, animations, accessibility pass
8. Compatibility pass and hardening
9. Documentation and final review

At the end, **self-audit** against this prompt: list any requirement not fully met, any check that can only return Unknown on common devices (and why), and any assumption you made.

---

## 14. DEFINITION OF DONE

- [ ] Existing app builds, runs, and all pre-existing functionality works unchanged
- [ ] All 21 checks (incl. Wireless Debugging) implemented independently with tests
- [ ] No check can return SECURE from missing/unreadable data
- [ ] Score deterministic, coverage-aware, grade-capped, explained in-app
- [ ] Every recommendation traceable to a real finding
- [ ] Every settings shortcut resolves or falls back safely; no crashes
- [ ] No sensitive data collected, logged or uploaded; manifest diff justified
- [ ] UI matches Safex design, works in light/dark, TalkBack, large fonts, RTL
- [ ] Per-check error isolation and timeouts verified
- [ ] Release (R8) build verified
- [ ] Docs delivered: API notes, test matrix, scoring explanation, list of files changed
- [ ] Final self-audit report provided

---

## 15. FINAL DELIVERABLES

1. Working code integrated into Safex
2. `docs/security-advisor-api-notes.md` (API reality matrix with findings)
3. `docs/security-advisor-scoring.md` (weights, groups, caps, examples)
4. `docs/security-advisor-test-matrix.md`
5. Change summary: files added, files modified, manifest/Gradle changes with justification
6. Known limitations list (what can't be verified on stock Android and why)
7. Suggested future enhancements (not implemented)

Begin now with **Phase 0: Discovery**. Do not write feature code until the Discovery Report and Implementation Plan are complete.
