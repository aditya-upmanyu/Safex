// test/appsentry/CalculateAppRiskUseCaseTest.kt
package com.safex.app.appsentry

import com.google.gson.Gson
import com.safex.app.data.appsentry.RegistryModel
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ThreatIndicator
import com.safex.app.domain.appsentry.repository.AppRegistryProvider
import com.safex.app.domain.appsentry.usecase.CalculateAppRiskUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileReader
import java.util.regex.Pattern

class CalculateAppRiskUseCaseTest {

    private lateinit var useCase: CalculateAppRiskUseCase
    private val gson = Gson()

    @Before
    fun setUp() {
        val registryFile = File("src/main/assets/appsentry/fake_app_registry.json")
        val model: RegistryModel = if (registryFile.exists()) {
            FileReader(registryFile).use { gson.fromJson(it, RegistryModel::class.java) }
        } else {
            RegistryModel(
                legitimate_packages = mapOf(
                    "com.sbi.lotusintouch" to "SBI YONO",
                    "com.phonepe.app" to "PhonePe",
                    "in.gov.uidai.mAadhaarPlus" to "mAadhaar"
                ),
                known_rat_signatures = listOf("com.anydesk.anydeskandroid", "com.spynote"),
                suspicious_package_patterns = listOf(".*\\.bank.*update.*", ".*\\.kyc.*verify.*")
            )
        }

        val testProvider = object : AppRegistryProvider {
            val compiled = model.suspicious_package_patterns.mapNotNull {
                try { Pattern.compile(it, Pattern.CASE_INSENSITIVE) } catch (_: Exception) { null }
            }

            override fun isKnownRat(packageName: String): Boolean {
                return model.known_rat_signatures.any { it.equals(packageName, ignoreCase = true) }
            }

            override fun matchesSuspiciousPattern(packageName: String): Boolean {
                return compiled.any { it.matcher(packageName).matches() }
            }

            override fun checkMasquerading(packageName: String, appLabel: String): AppRegistryProvider.MasqueradeType {
                val normalizedLabel = appLabel.lowercase().replace(" ", "")
                for ((legitPkg, legitName) in model.legitimate_packages) {
                    val normalizedLegit = legitName.lowercase().replace(" ", "")
                    if (packageName.equals(legitPkg, ignoreCase = true)) {
                        return AppRegistryProvider.MasqueradeType.NONE
                    }
                    if (normalizedLabel.contains(normalizedLegit) || normalizedLegit.contains(normalizedLabel)) {
                        if (legitName.contains("Aadhaar", ignoreCase = true)) {
                            return AppRegistryProvider.MasqueradeType.GOVT
                        }
                        return AppRegistryProvider.MasqueradeType.BANKING
                    }
                }
                return AppRegistryProvider.MasqueradeType.NONE
            }
        }

        useCase = CalculateAppRiskUseCase(testProvider)
    }

    @Test
    fun testSafeApp() {
        val eval = useCase(
            packageName = "com.example.calculator",
            appName = "Calculator",
            permissions = listOf("android.permission.INTERNET"),
            isSideLoaded = false
        )

        assertEquals(AppRiskLevel.SAFE, eval.level)
        assertEquals(0, eval.score)
        assertTrue(eval.indicators.isEmpty())
    }

    @Test
    fun testFakeBankingAppMasquerading() {
        val eval = useCase(
            packageName = "com.fraud.sbiyono.reward",
            appName = "SBI YONO",
            permissions = listOf("android.permission.INTERNET", "android.permission.RECEIVE_SMS"),
            isSideLoaded = true
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.FAKE_BANKING_APP))
        assertTrue(eval.indicators.contains(ThreatIndicator.READS_SMS))
        assertTrue(eval.indicators.contains(ThreatIndicator.SIDE_LOADED))
        assertTrue("Score must be high or critical for fake banking app", eval.score >= 65)
        assertTrue(eval.level == AppRiskLevel.HIGH || eval.level == AppRiskLevel.CRITICAL)
    }

    @Test
    fun testLegitimateBankingAppNotFlaggedAsFake() {
        val eval = useCase(
            packageName = "com.sbi.lotusintouch",
            appName = "SBI YONO",
            permissions = listOf("android.permission.INTERNET"),
            isSideLoaded = false
        )

        assertFalse("Official SBI package should not be flagged as fake", eval.indicators.contains(ThreatIndicator.FAKE_BANKING_APP))
    }

    @Test
    fun testKnownRatSignature() {
        val eval = useCase(
            packageName = "com.anydesk.anydeskandroid",
            appName = "AnyDesk",
            permissions = listOf("android.permission.BIND_ACCESSIBILITY_SERVICE"),
            isSideLoaded = false
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.KNOWN_RAT_PACKAGE))
        assertTrue(eval.indicators.contains(ThreatIndicator.REQUESTS_ACCESSIBILITY))
        assertTrue("Score must be high or critical for known RAT", eval.score >= 65)
    }

    @Test
    fun testSuspiciousPackagePattern() {
        val eval = useCase(
            packageName = "org.free.bank.update.service",
            appName = "Bank Update",
            permissions = listOf("android.permission.INTERNET"),
            isSideLoaded = false
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.SUSPICIOUS_PACKAGE_PATTERN))
    }

    @Test
    fun testCriticalPermissionCombo() {
        val eval = useCase(
            packageName = "com.suspicious.tool",
            appName = "Utility Tool",
            permissions = listOf(
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
                "android.permission.BIND_DEVICE_ADMIN",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.READ_SMS",
                "android.permission.READ_CONTACTS"
            ),
            isSideLoaded = true
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.REQUESTS_ACCESSIBILITY))
        assertTrue(eval.indicators.contains(ThreatIndicator.REQUESTS_DEVICE_ADMIN))
        assertTrue(eval.indicators.contains(ThreatIndicator.REQUESTS_OVERLAY))
        assertTrue(eval.indicators.contains(ThreatIndicator.READS_SMS))
        assertTrue(eval.indicators.contains(ThreatIndicator.READS_CONTACTS))
        assertTrue(eval.indicators.contains(ThreatIndicator.SIDE_LOADED))
        assertEquals(AppRiskLevel.CRITICAL, eval.level)
    }

    @Test
    fun testCameraAndMicCombo() {
        val eval = useCase(
            packageName = "com.simple.app",
            appName = "Recorder",
            permissions = listOf("android.permission.CAMERA", "android.permission.RECORD_AUDIO"),
            isSideLoaded = false
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.USES_CAMERA_MICROPHONE))
    }

    @Test
    fun testVirusTotalHits() {
        val eval = useCase(
            packageName = "com.unverified.apk",
            appName = "Game Mod",
            permissions = listOf("android.permission.INTERNET"),
            isSideLoaded = true,
            virusTotalMalicious = 5
        )

        assertTrue(eval.indicators.contains(ThreatIndicator.VIRUSTOTAL_POSITIVE))
        assertTrue(eval.score >= 65)
        assertTrue(eval.level == AppRiskLevel.HIGH || eval.level == AppRiskLevel.CRITICAL)
    }
}
