// domain/appsentry/model/ApkInspectionResult.kt
package com.safex.app.domain.appsentry.model

/**
 * Result of static inspection and VirusTotal reputation check on an uninstalled APK file.
 */
data class ApkInspectionResult(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val sha256: String,
    val packageName: String?,
    val appName: String?,
    val versionName: String?,
    val versionCode: Long?,
    val requestedPermissions: List<String>,
    val riskScore: Int,
    val riskLevel: AppRiskLevel,
    val threatIndicators: Set<ThreatIndicator>,
    val virusTotalStats: VirusTotalStats?,
    val inspectedAt: Long,
    val isLegitimateCertificate: Boolean = true,
    val errorMessage: String? = null
)

data class VirusTotalStats(
    val harmless: Int,
    val malicious: Int,
    val suspicious: Int,
    val undetected: Int,
    val totalEngines: Int,
    val permalink: String? = null
)
