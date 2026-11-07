// domain/appsentry/usecase/CalculateAppRiskUseCase.kt
package com.safex.app.domain.appsentry.usecase

import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ThreatIndicator
import com.safex.app.domain.appsentry.repository.AppRegistryProvider
import javax.inject.Inject

data class AppRiskEvaluation(
    val score: Int,
    val level: AppRiskLevel,
    val indicators: Set<ThreatIndicator>
)

class CalculateAppRiskUseCase @Inject constructor(
    private val registryProvider: AppRegistryProvider
) {
    operator fun invoke(
        packageName: String,
        appName: String,
        permissions: List<String>,
        isSideLoaded: Boolean,
        isLegitimateCertificate: Boolean = true,
        virusTotalMalicious: Int = 0,
        virusTotalSuspicious: Int = 0
    ): AppRiskEvaluation {
        val indicators = mutableSetOf<ThreatIndicator>()

        // 1. Check known RAT signatures
        if (registryProvider.isKnownRat(packageName)) {
            indicators.add(ThreatIndicator.KNOWN_RAT_PACKAGE)
        }

        // 2. Check suspicious package patterns
        if (registryProvider.matchesSuspiciousPattern(packageName)) {
            indicators.add(ThreatIndicator.SUSPICIOUS_PACKAGE_PATTERN)
        }

        // 3. Masquerading check
        when (registryProvider.checkMasquerading(packageName, appName)) {
            AppRegistryProvider.MasqueradeType.BANKING -> indicators.add(ThreatIndicator.FAKE_BANKING_APP)
            AppRegistryProvider.MasqueradeType.GOVT -> indicators.add(ThreatIndicator.FAKE_GOVT_APP)
            AppRegistryProvider.MasqueradeType.NONE -> Unit
        }

        // 4. Permissions check
        val permSet = permissions.map { it.uppercase() }.toSet()

        if (permSet.any { it.contains("BIND_ACCESSIBILITY_SERVICE") }) {
            indicators.add(ThreatIndicator.REQUESTS_ACCESSIBILITY)
        }
        if (permSet.any { it.contains("BIND_DEVICE_ADMIN") }) {
            indicators.add(ThreatIndicator.REQUESTS_DEVICE_ADMIN)
        }
        if (permSet.any { it.contains("SYSTEM_ALERT_WINDOW") }) {
            indicators.add(ThreatIndicator.REQUESTS_OVERLAY)
        }
        if (permSet.any { it.contains("RECEIVE_SMS") || it.contains("READ_SMS") }) {
            indicators.add(ThreatIndicator.READS_SMS)
        }
        if (permSet.any { it.contains("READ_CONTACTS") }) {
            indicators.add(ThreatIndicator.READS_CONTACTS)
        }
        if (permSet.any { it.contains("READ_CALL_LOG") }) {
            indicators.add(ThreatIndicator.READS_CALL_LOG)
        }

        val hasCamera = permSet.any { it.contains("CAMERA") }
        val hasMic = permSet.any { it.contains("RECORD_AUDIO") }
        if (hasCamera && hasMic) {
            indicators.add(ThreatIndicator.USES_CAMERA_MICROPHONE)
        }

        if (permSet.any { it.contains("ACCESS_BACKGROUND_LOCATION") }) {
            indicators.add(ThreatIndicator.BACKGROUND_DATA_ACCESS)
        }

        if (permissions.size > 15) {
            indicators.add(ThreatIndicator.EXCESSIVE_PERMISSIONS)
        }

        // 5. Origin check
        if (isSideLoaded) {
            indicators.add(ThreatIndicator.SIDE_LOADED)
            indicators.add(ThreatIndicator.NOT_FROM_PLAY_STORE)
        }

        // 6. Signing & VirusTotal
        if (!isLegitimateCertificate) {
            indicators.add(ThreatIndicator.UNSIGNED_APK)
        }

        if (virusTotalMalicious > 0) {
            indicators.add(ThreatIndicator.VIRUSTOTAL_POSITIVE)
        } else if (virusTotalSuspicious > 0) {
            indicators.add(ThreatIndicator.VIRUSTOTAL_SUSPICIOUS)
        }

        // Calculate weighted score (cap at 100)
        var totalScore = indicators.sumOf { it.weight }
        if (virusTotalMalicious > 2) {
            totalScore += 25
        }
        val clampedScore = totalScore.coerceIn(0, 100)
        val level = AppRiskLevel.fromScore(clampedScore)

        return AppRiskEvaluation(
            score = clampedScore,
            level = level,
            indicators = indicators
        )
    }
}
