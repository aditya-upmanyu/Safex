package com.safex.app.domain.usecase

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import com.safex.app.domain.model.CheckStatus
import com.safex.app.domain.model.SecurityCheck
import com.safex.app.domain.model.SecurityStatus
import com.safex.app.util.RootCheckUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetSecurityStatusUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): SecurityStatus {
        val checks = mutableListOf<SecurityCheck>()
        var score = 100

        // Root Check
        val isRooted = RootCheckUtil.isDeviceRooted()
        checks.add(
            SecurityCheck(
                name = "Root Status",
                status = if (isRooted) CheckStatus.ALERT else CheckStatus.OK,
                message = if (isRooted) "Device is rooted" else "Not rooted"
            )
        )
        if (isRooted) score -= 30

        // Screen Lock Check
        val hasScreenLock = checkScreenLock()
        checks.add(
            SecurityCheck(
                name = "Screen Lock",
                status = if (hasScreenLock) CheckStatus.OK else CheckStatus.ALERT,
                message = if (hasScreenLock) "Enabled" else "Disabled"
            )
        )
        if (!hasScreenLock) score -= 25

        // Developer Mode Check
        val developerModeEnabled = checkDeveloperMode()
        checks.add(
            SecurityCheck(
                name = "Developer Mode",
                status = if (developerModeEnabled) CheckStatus.WARNING else CheckStatus.OK,
                message = if (developerModeEnabled) "Enabled" else "Disabled"
            )
        )
        if (developerModeEnabled) score -= 10

        // Unknown Sources Check
        val unknownSourcesEnabled = checkUnknownSources()
        checks.add(
            SecurityCheck(
                name = "Unknown Sources",
                status = if (unknownSourcesEnabled) CheckStatus.ALERT else CheckStatus.OK,
                message = if (unknownSourcesEnabled) "Enabled" else "Disabled"
            )
        )
        if (unknownSourcesEnabled) score -= 20

        // Play Protect - simulated check
        val playProtectStatus = "OK"
        checks.add(
            SecurityCheck(
                name = "Play Protect",
                status = CheckStatus.OK,
                message = "Active"
            )
        )

        return SecurityStatus(
            overallScore = score.coerceIn(0, 100),
            isRooted = isRooted,
            hasScreenLock = hasScreenLock,
            playProtectStatus = playProtectStatus,
            developerModeEnabled = developerModeEnabled,
            unknownSourcesEnabled = unknownSourcesEnabled,
            checks = checks
        )
    }

    private fun checkScreenLock(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    private fun checkDeveloperMode(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun checkUnknownSources(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.packageManager.canRequestPackageInstalls()
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.INSTALL_NON_MARKET_APPS,
                    0
                ) == 1
            }
        } catch (e: Exception) {
            false
        }
    }
}
