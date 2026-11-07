package com.safex.app.core.family

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Evaluates installed apps for suspicious installation signals.
 *
 * A sideloaded APK is NOT automatically malicious.
 * We only classify SUSPICIOUS or HIGH_RISK when sufficient indicators exist.
 *
 * Labels:
 *   VERIFIED_STORE   – installed from Play Store
 *   UNKNOWN_SOURCE   – no installer recorded
 *   SIDELOADED       – explicit sideload signal
 *   SUSPICIOUS       – unknown source + risky permissions
 *   HIGH_RISK        – unknown source + accessibility + financial permissions
 */
@Singleton
class SuspiciousInstallationDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class InstallRisk { VERIFIED_STORE, UNKNOWN_SOURCE, SIDELOADED, SUSPICIOUS, HIGH_RISK }

    data class AppRiskProfile(
        val packageName: String,
        val appName: String,
        val installRisk: InstallRisk,
        val riskyPermissions: List<String>,
        val installerPackage: String?
    )

    private val riskyPermissions = listOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.READ_CALL_LOG",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.CAMERA",
        "android.permission.RECORD_AUDIO"
    )

    private val trustedInstallers = setOf(
        "com.android.vending",          // Play Store
        "com.google.android.packageinstaller",
        "com.miui.packageinstaller",
        "com.samsung.android.packageinstaller"
    )

    fun scanRecentlyInstalledApps(withinLastDays: Int = 7): List<AppRiskProfile> {
        val cutoff = System.currentTimeMillis() - (withinLastDays * 86_400_000L)
        return try {
            context.packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // user-installed only
                .mapNotNull { appInfo ->
                    try {
                        val pkgInfo = context.packageManager.getPackageInfo(
                            appInfo.packageName, PackageManager.GET_PERMISSIONS
                        )
                        if ((pkgInfo.firstInstallTime) < cutoff) return@mapNotNull null
                        buildProfile(appInfo, pkgInfo.requestedPermissions?.toList() ?: emptyList())
                    } catch (_: Exception) { null }
                }
                .filter { it.installRisk != InstallRisk.VERIFIED_STORE }
        } catch (_: Exception) { emptyList() }
    }

    fun evaluatePackage(packageName: String): AppRiskProfile? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val pkgInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            buildProfile(appInfo, pkgInfo.requestedPermissions?.toList() ?: emptyList())
        } catch (_: Exception) { null }
    }

    private fun buildProfile(appInfo: ApplicationInfo, permissions: List<String>): AppRiskProfile {
        val installer = try {
            context.packageManager.getInstallerPackageName(appInfo.packageName)
        } catch (_: Exception) { null }

        val appName = context.packageManager.getApplicationLabel(appInfo).toString()
        val matched = permissions.filter { riskyPermissions.contains(it) }

        val risk = when {
            trustedInstallers.contains(installer) -> InstallRisk.VERIFIED_STORE
            installer == null && matched.size >= 3 &&
                    matched.contains("android.permission.BIND_ACCESSIBILITY_SERVICE") -> InstallRisk.HIGH_RISK
            installer == null && matched.size >= 2 -> InstallRisk.SUSPICIOUS
            installer == null -> InstallRisk.UNKNOWN_SOURCE
            else -> InstallRisk.SIDELOADED
        }

        return AppRiskProfile(
            packageName = appInfo.packageName,
            appName = appName,
            installRisk = risk,
            riskyPermissions = matched,
            installerPackage = installer
        )
    }
}
