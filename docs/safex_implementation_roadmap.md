# SAFEx — Implementation Roadmap

> Based on [SAFEx_GOD_LEVEL_PROMPT_v2.md](file:///C:/Users/adity/OneDrive/Desktop/SafeX/SAFEx_GOD_LEVEL_PROMPT_v2.md)

## Current State Analysis

The project currently exists as **`com.fraudguardian.app`** with an older architecture. The prompt requires a **full rebuild** under `com.safex.app` with a different folder structure, design system, and feature set. This means we need to:

1. **Replace** the entire source tree under `app/src/main/java/` from `com.fraudguardian.app` → `com.safex.app`
2. **Rewrite** `build.gradle.kts` (both project and app level) per the locked spec
3. **Rebuild** every layer from scratch following the prompt's exact templates and blueprints

> [!IMPORTANT]
> The existing `com.fraudguardian.app` code will be **completely replaced**, not incrementally modified. The prompt is the single source of truth.

---

## Phase 1 — Foundation (Section 15, Steps 1–4)

**What:** Build configuration, manifest, design system, and core utilities.

| # | File | Location | Notes |
|---|------|----------|-------|
| 1 | `build.gradle.kts` | Project root | Update root project name, plugin versions |
| 2 | `build.gradle.kts` | `app/` | Full rewrite per Section 13 — namespace `com.safex.app`, all locked dependencies |
| 3 | `settings.gradle.kts` | Project root | `rootProject.name = "SAFEx"` |
| 4 | `AndroidManifest.xml` | `app/src/main/` | Full manifest per Section 12 |
| 5 | `Color.kt` | `presentation/theme/` | Section 4.1 — exact colors |
| 6 | `Spacing.kt` | `presentation/theme/` | Section 4.2 — 8dp grid |
| 7 | `Shape.kt` | `presentation/theme/` | Section 4.3 |
| 8 | `Type.kt` | `presentation/theme/` | Section 4.4 — Inter + JetBrains Mono families |
| 9 | `Theme.kt` | `presentation/theme/` | Section 4.5 — dark-first |
| 10 | `Constants.kt` | `util/` | App-wide constants (animation durations, API base URLs) |
| 11 | `UiState.kt` | `util/` | Section 5.1 — sealed class |

**Dependencies:** Font files (`inter_regular`, `inter_medium`, `inter_bold`, `jetbrains_mono_regular`) in `res/font/`, drawable resources (`ic_shield`, `ic_alert`).

---

## Phase 2 — Data Layer (Section 15, Steps 5–19)

**What:** Room entities, DAOs, database, DTOs, Retrofit API interfaces, DI modules.

| # | File | Notes |
|---|------|-------|
| 12 | `CallRecordEntity.kt` | Section 9 — 14 fields |
| 13 | `ScanHistoryEntity.kt` | Section 9 |
| 14 | `AlertEntity.kt` | Section 9 |
| 15 | `CallRecordDao.kt` | Stamp Template 5.3 for `call_records` |
| 16 | `ScanHistoryDao.kt` | Stamp Template 5.3 for `scan_history` |
| 17 | `AlertDao.kt` | Stamp Template 5.3 for `alerts` |
| 18 | `AppDatabase.kt` | Section 9 |
| 19 | `Converters.kt` | Type converters for Room |
| 20 | `OpenAIDto.kt` | Section 8 |
| 21 | `SttDto.kt` | Section 8 |
| 22 | `VirusTotalDto.kt` | Section 8 |
| 23 | `OpenAIApi.kt` | Section 8 |
| 24 | `GoogleSttApi.kt` | Section 8 |
| 25 | `VirusTotalApi.kt` | Section 8 |
| 26 | `TruecallerApi.kt` | Section 8 |
| 27 | `NetworkModule.kt` | Hilt DI — Retrofit instances for OpenAI, Google STT, VirusTotal |
| 28 | `DatabaseModule.kt` | Hilt DI — Room database + DAO providers |

---

## Phase 3 — Domain Layer (Section 15, Steps 20–33)

**What:** Domain models, repository interfaces, use cases.

| # | File | Notes |
|---|------|-------|
| 29 | `CallRecord.kt` | Domain model |
| 30 | `FraudAnalysis.kt` | Domain model |
| 31 | `ScanResult.kt` | Domain model |
| 32 | `SecurityStatus.kt` | Domain model |
| 33 | `RiskLevel.kt` | Enum: SAFE, SUSPICIOUS, FRAUD, MALICIOUS |
| 34 | `CallRepository.kt` | Interface — Template 5.4 |
| 35 | `ScanRepository.kt` | Interface — Template 5.4 |
| 36 | `SecurityRepository.kt` | Interface — Template 5.4 |
| 37 | `AnalyzeCallUseCase.kt` | Template 5.5 |
| 38 | `ScanUrlUseCase.kt` | Includes VirusTotal risk thresholds |
| 39 | `ScanQrCodeUseCase.kt` | Template 5.5 |
| 40 | `GetSecurityStatusUseCase.kt` | Template 5.5 |
| 41 | `GetCallHistoryUseCase.kt` | Template 5.5 |
| 42 | `SendAlertUseCase.kt` | Template 5.5 |

---

## Phase 4 — Services (Section 15, Steps 34–39)

**What:** Background services for call detection, recording, analysis pipeline, alerts.

| # | File | Notes |
|---|------|-------|
| 43 | `accessibility_service_config.xml` | Section 7.1 |
| 44 | `CallDetectorService.kt` | Section 7.1 — Accessibility service |
| 45 | `CallRecorderService.kt` | Section 7.2 — Hybrid 3-layer recording |
| 46 | `AnalysisPipelineService.kt` | Section 7.3 — STT → GPT-4o → alert |
| 47 | `FraudAlertService.kt` | Section 7.4 — SMS + notification |
| 48 | `BootReceiver.kt` | Section 12 |

---

## Phase 5 — Repository Impls + DI (Section 15, Steps 40–43)

**What:** Concrete repository implementations, remaining DI modules.

| # | File | Notes |
|---|------|-------|
| 49 | `CallRepositoryImpl.kt` | Template 5.4 impl |
| 50 | `ScanRepositoryImpl.kt` | Template 5.4 impl |
| 51 | `SecurityRepositoryImpl.kt` | Template 5.4 impl |
| 52 | `RepositoryModule.kt` | Hilt binds interfaces → impls |
| 53 | `AppModule.kt` | Hilt provides SettingsDataStore, etc. |

---

## Phase 6 — Presentation (Section 15, Steps 44–68)

**What:** Navigation, reusable components, all 8 screens with ViewModels, MainActivity, DataStore.

| # | File | Notes |
|---|------|-------|
| 54 | `Screen.kt` | Sealed class for navigation routes |
| 55 | `SAFExNavGraph.kt` | NavHost with all routes |
| 56 | `SafexCard.kt` | Reusable card component |
| 57 | `RiskBadge.kt` | Risk level badge |
| 58 | `SecurityScoreGauge.kt` | Circular arc gauge 0–100 |
| 59 | `CallRecordItem.kt` | Call history list item |
| 60 | `AlertBanner.kt` | Alert banner component |
| 61 | `BottomNavBar.kt` | Bottom navigation |
| 62–63 | `OnboardingScreen.kt` + `PermissionSetupScreen.kt` | Section 6.1 |
| 64–65 | `HomeScreen.kt` + `HomeViewModel.kt` | Section 6.2 |
| 66–67 | `CallGuardScreen.kt` + `CallGuardViewModel.kt` | Section 6.3 |
| 68–69 | `UrlScannerScreen.kt` + `UrlScannerViewModel.kt` | Section 6.4 |
| 70–71 | `DeviceSecurityScreen.kt` + `DeviceSecurityViewModel.kt` | Section 6.5 |
| 72–73 | `HistoryScreen.kt` + `HistoryViewModel.kt` | Section 6.6 |
| 74–75 | `ReportFraudScreen.kt` + `ReportFraudViewModel.kt` | Section 6.7 |
| 76–77 | `SettingsScreen.kt` + `SettingsViewModel.kt` | Section 6.8 |
| 78 | `MainActivity.kt` | Hilt entry point, SAFExTheme wrapper |
| 79 | `SettingsDataStore.kt` | Encrypted key storage |

---

## Phase 7 — Resources & Polish (Section 15, Steps 69–71)

**What:** String resources, Application class, ProGuard rules.

| # | File | Notes |
|---|------|-------|
| 80 | `strings.xml` | Section 11 — bilingual Hindi/English |
| 81 | `SAFExApplication.kt` | `@HiltAndroidApp` |
| 82 | `proguard-rules.pro` | Retrofit/Gson/Room keep rules |

---

## Key Architectural Decisions (from Prompt)

- **Package:** `com.safex.app` (replaces `com.fraudguardian.app`)
- **Architecture:** Clean Architecture (data → domain → presentation)
- **DI:** Hilt with `kapt` (not KSP — per prompt's `build.gradle.kts`)
- **Database:** Room (unencrypted — prompt doesn't specify SQLCipher)
- **API keys:** via `local.properties` → `BuildConfig` + encrypted DataStore for runtime updates
- **Dark-first theme:** `SafexNavy` background, `SafexCyan` accents
- **Fonts:** Inter (regular/medium/bold) + JetBrains Mono (regular)
- **No Firebase:** Prompt doesn't include Firebase (existing code had it)
- **No KSP:** Prompt uses `kapt` for Hilt and Room

> [!WARNING]
> The prompt specifies `kapt` for annotation processing (not KSP). The existing project uses KSP. We must switch to `kapt` as specified.
