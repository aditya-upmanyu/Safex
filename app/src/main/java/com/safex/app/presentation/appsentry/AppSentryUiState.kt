// presentation/appsentry/AppSentryUiState.kt
package com.safex.app.presentation.appsentry

import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ScannedApp

data class AppSentryUiState(
    val isScanning: Boolean = false,
    val isInspectingApk: Boolean = false,
    val scanProgress: Float = 0f,
    val currentScanningApp: String = "",
    val installedApps: List<ScannedApp> = emptyList(),
    val inspectedApks: List<ApkInspectionResult> = emptyList(),
    val selectedApp: ScannedApp? = null,
    val selectedApk: ApkInspectionResult? = null,
    val filterRiskLevel: AppRiskLevel? = null,
    val searchQuery: String = "",
    val selectedTab: Int = 0, // 0: Installed Apps, 1: APK Armor
    val errorMessage: String? = null,
    val lastScanTime: Long = 0L
) {
    val filteredApps: List<ScannedApp>
        get() = installedApps
            .filter { app ->
                val matchesFilter = filterRiskLevel == null || app.riskLevel == filterRiskLevel
                val matchesSearch = searchQuery.isBlank() ||
                        app.appName.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
                matchesFilter && matchesSearch
            }
            .sortedByDescending { it.riskScore }

    val safeCount: Int get() = installedApps.count { it.riskLevel == AppRiskLevel.SAFE || it.riskLevel == AppRiskLevel.LOW }
    val suspiciousCount: Int get() = installedApps.count { it.riskLevel == AppRiskLevel.MEDIUM }
    val dangerousCount: Int get() = installedApps.count { it.riskLevel == AppRiskLevel.HIGH || it.riskLevel == AppRiskLevel.CRITICAL }
}

sealed interface AppSentryEvent {
    data object ScanAllApps : AppSentryEvent
    data class ScanSingleApp(val packageName: String) : AppSentryEvent
    data class InspectApk(val uriOrPath: String) : AppSentryEvent
    data class SelectApp(val app: ScannedApp?) : AppSentryEvent
    data class SelectApk(val apk: ApkInspectionResult?) : AppSentryEvent
    data class FilterByRisk(val level: AppRiskLevel?) : AppSentryEvent
    data class SearchQueryChanged(val query: String) : AppSentryEvent
    data class SwitchTab(val tabIndex: Int) : AppSentryEvent
    data class ToggleTrustApp(val packageName: String, val trust: Boolean) : AppSentryEvent
    data class DeleteApkInspection(val filePath: String, val deleteFile: Boolean) : AppSentryEvent
    data object ClearError : AppSentryEvent
}
