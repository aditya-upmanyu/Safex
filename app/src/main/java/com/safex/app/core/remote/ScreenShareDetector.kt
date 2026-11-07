package com.safex.app.core.remote

import android.content.Context
import android.content.pm.PackageManager
import com.safex.app.core.security.model.DetectionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects remote-access / screen-sharing risk signals.
 *
 * HONESTY CONTRACT (per spec):
 * 1. INSTALLED    — known remote-access app is installed
 * 2. FOREGROUNDED — known remote-access app is in the foreground (via accessibility service)
 * 3. NOT_VERIFIED — actual screen-capture session cannot be confirmed
 *
 * We do NOT claim ACTIVE_SCREEN_SHARE unless we have a genuine MediaProjection
 * callback from our own session — we never claim visibility into other apps'
 * MediaProjection sessions.
 */
@Singleton
class ScreenShareDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class ScreenShareStatus(
        val installedApps: List<String>,        // packageNames of installed remote apps
        val foregroundApp: String?,             // currently foregrounded remote app (if any)
        val screenShareStatus: DetectionStatus, // Always NOT_VERIFIED unless we own the session
        val notes: String
    )

    private var currentForegroundRemoteApp: String? = null

    fun updateForeground(packageName: String?) {
        currentForegroundRemoteApp = packageName?.takeIf {
            RemoteAccessAppRegistry.isRemoteAccessApp(it)
        }
    }

    fun getStatus(): ScreenShareStatus {
        val installed = getInstalledRemoteApps()
        return ScreenShareStatus(
            installedApps = installed,
            foregroundApp = currentForegroundRemoteApp,
            screenShareStatus = DetectionStatus.NOT_VERIFIED, // Honest: cannot confirm
            notes = "Actual screen-capture sessions initiated by other apps cannot be detected " +
                    "without root or MediaProjection delegation from that app."
        )
    }

    fun isRemoteAppForegrounded(): Boolean = currentForegroundRemoteApp != null

    private fun getInstalledRemoteApps(): List<String> {
        return try {
            RemoteAccessAppRegistry.packageNames.filter { pkg ->
                try {
                    context.packageManager.getPackageInfo(pkg, 0)
                    true
                } catch (_: PackageManager.NameNotFoundException) {
                    false
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
