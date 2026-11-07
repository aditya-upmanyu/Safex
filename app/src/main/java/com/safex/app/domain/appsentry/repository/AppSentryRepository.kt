// domain/appsentry/repository/AppSentryRepository.kt
package com.safex.app.domain.appsentry.repository

import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.ScannedApp
import com.safex.app.domain.appsentry.model.VirusTotalStats
import kotlinx.coroutines.flow.Flow

interface AppSentryRepository {
    /** Observe stored scanned apps */
    fun observeScannedApps(): Flow<List<ScannedApp>>

    /** Perform a fresh scan of all installed applications, store results, and return */
    suspend fun scanAllInstalledApps(): List<ScannedApp>

    /** Scan a single installed package */
    suspend fun scanInstalledApp(packageName: String): ScannedApp?

    /** Perform deep inspection of an APK file (via URI or absolute path) */
    suspend fun inspectApkFile(fileUriOrPath: String): ApkInspectionResult

    /** Observe history of inspected APKs */
    fun observeApkInspections(): Flow<List<ApkInspectionResult>>

    /** Delete an inspected APK record and optionally the file */
    suspend fun deleteApkInspection(filePath: String, deleteFile: Boolean = false): Boolean

    /** Set or unset trusted status for a package */
    suspend fun setAppTrusted(packageName: String, isTrusted: Boolean)

    /** Look up VirusTotal reputation by SHA-256 (Cache first, then network) */
    suspend fun queryVirusTotal(sha256: String): Result<VirusTotalStats?>
}
