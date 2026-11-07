// data/appsentry/ApkInspectorEngine.kt
package com.safex.app.data.appsentry

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ThreatIndicator
import com.safex.app.domain.appsentry.model.VirusTotalStats
import com.safex.app.domain.appsentry.usecase.CalculateAppRiskUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkInspectorEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculateRiskUseCase: CalculateAppRiskUseCase
) {

    /**
     * Inspects an APK from a File path or a content:// URI.
     */
    fun inspect(
        fileUriOrPath: String,
        virusTotalStats: VirusTotalStats? = null
    ): ApkInspectionResult {
        var tempFile: File? = null
        try {
            val file: File = if (fileUriOrPath.startsWith("content://")) {
                val uri = Uri.parse(fileUriOrPath)
                val cacheCopy = copyUriToTempFile(uri)
                tempFile = cacheCopy
                cacheCopy
            } else {
                File(fileUriOrPath)
            }

            if (!file.exists()) {
                return createErrorResult(fileUriOrPath, "File does not exist: $fileUriOrPath")
            }

            if (!file.name.endsWith(".apk", ignoreCase = true) && !file.name.endsWith(".bin", ignoreCase = true)) {
                // If it came from a URI, we still allow inspecting if it's an APK archive
            }

            val fileSize = file.length()
            if (fileSize == 0L) {
                return createErrorResult(fileUriOrPath, "File is empty (0 bytes)")
            }

            // Calculate SHA-256
            val sha256 = calculateSha256(file)

            // Extract APK metadata
            val pm = context.packageManager
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_PERMISSIONS or PackageManager.GET_SIGNATURES
            }

            val packageInfo: PackageInfo? = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageArchiveInfo(file.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageArchiveInfo(file.absolutePath, flags)
                }
            } catch (e: Exception) {
                null
            }

            if (packageInfo == null) {
                return ApkInspectionResult(
                    filePath = fileUriOrPath,
                    fileName = file.name,
                    fileSize = fileSize,
                    sha256 = sha256,
                    packageName = null,
                    appName = null,
                    versionName = null,
                    versionCode = null,
                    requestedPermissions = emptyList(),
                    riskScore = 80,
                    riskLevel = AppRiskLevel.HIGH,
                    threatIndicators = setOf(ThreatIndicator.UNSIGNED_APK, ThreatIndicator.SIDE_LOADED),
                    virusTotalStats = virusTotalStats,
                    inspectedAt = System.currentTimeMillis(),
                    isLegitimateCertificate = false,
                    errorMessage = "Corrupted or malformed APK package."
                )
            }

            // Fix resource loading paths
            val appInfo = packageInfo.applicationInfo
            if (appInfo != null) {
                appInfo.sourceDir = file.absolutePath
                appInfo.publicSourceDir = file.absolutePath
            }

            val packageName = packageInfo.packageName ?: "unknown.package"
            val appName = try {
                appInfo?.loadLabel(pm)?.toString() ?: packageName
            } catch (e: Exception) {
                packageName
            }

            val versionName = packageInfo.versionName ?: "1.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val requestedPermissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

            // Check signature presence
            val isSigned = checkIsSigned(packageInfo)

            val evaluation = calculateRiskUseCase(
                packageName = packageName,
                appName = appName,
                permissions = requestedPermissions,
                isSideLoaded = true, // Any standalone inspected APK is uninstalled / side-loaded
                isLegitimateCertificate = isSigned,
                virusTotalMalicious = virusTotalStats?.malicious ?: 0,
                virusTotalSuspicious = virusTotalStats?.suspicious ?: 0
            )

            return ApkInspectionResult(
                filePath = fileUriOrPath,
                fileName = file.name,
                fileSize = fileSize,
                sha256 = sha256,
                packageName = packageName,
                appName = appName,
                versionName = versionName,
                versionCode = versionCode,
                requestedPermissions = requestedPermissions,
                riskScore = evaluation.score,
                riskLevel = evaluation.level,
                threatIndicators = evaluation.indicators,
                virusTotalStats = virusTotalStats,
                inspectedAt = System.currentTimeMillis(),
                isLegitimateCertificate = isSigned,
                errorMessage = null
            )
        } catch (e: Exception) {
            return createErrorResult(fileUriOrPath, e.localizedMessage ?: "Unknown inspection error")
        } finally {
            tempFile?.delete()
        }
    }

    fun calculateSha256(file: File): String {
        return FileInputStream(file).use { calculateSha256(it) }
    }

    fun calculateSha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        val hashBytes = digest.digest()
        val sb = StringBuilder()
        for (b in hashBytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun checkIsSigned(packageInfo: PackageInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo
            signingInfo != null && (signingInfo.hasMultipleSigners() || signingInfo.signingCertificateHistory != null)
        } else {
            @Suppress("DEPRECATION")
            val signatures = packageInfo.signatures
            signatures != null && signatures.isNotEmpty()
        }
    }

    private fun copyUriToTempFile(uri: Uri): File {
        val temp = File.createTempFile("inspect_apk_", ".apk", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            temp.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return temp
    }

    private fun createErrorResult(filePath: String, error: String): ApkInspectionResult {
        return ApkInspectionResult(
            filePath = filePath,
            fileName = filePath.substringAfterLast('/'),
            fileSize = 0L,
            sha256 = "",
            packageName = null,
            appName = null,
            versionName = null,
            versionCode = null,
            requestedPermissions = emptyList(),
            riskScore = 0,
            riskLevel = AppRiskLevel.SAFE,
            threatIndicators = emptySet(),
            virusTotalStats = null,
            inspectedAt = System.currentTimeMillis(),
            isLegitimateCertificate = false,
            errorMessage = error
        )
    }
}
