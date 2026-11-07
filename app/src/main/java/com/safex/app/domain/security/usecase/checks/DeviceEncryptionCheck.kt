package com.safex.app.domain.security.usecase.checks

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import com.safex.app.domain.security.model.*
import com.safex.app.domain.security.usecase.BaseSecurityCheck

class DeviceEncryptionCheck : BaseSecurityCheck() {
    override val id = "device_encryption"
    override val title = "Device Encryption"
    override val category = SecurityCategory.DEVICE_PROTECTION
    override val minApi = 23
    override val isScoreable = true
    override val weight = 0.9f
    override val expectedSecureValue = true
    
    override suspend fun performCheck(context: Context): SecurityCheckResult {
        val startTime = System.currentTimeMillis()
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        
        if (dpm == null) {
            return buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = null,
                userExplanation = "Unable to check encryption status",
                technicalDetails = "DevicePolicyManager not available",
                whyItMatters = "Encryption protects your data if device is lost or stolen",
                recommendation = "Check Settings > Security > Encryption",
                remediation = RemediationAction("Open Security", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS, 
                    android.provider.Settings.ACTION_SECURITY_SETTINGS),
                confidence = Confidence.LOW,
                detectionMethod = "DevicePolicyManager query",
                evidence = listOf(Evidence("Service", "unavailable")),
                startTime = startTime
            )
        }
        
        val status = try {
            dpm.storageEncryptionStatus
        } catch (e: Exception) {
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED
        }
        
        val isEncrypted = status in listOf(
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER
        )
        
        val deviceLaunchedWithPie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        
        return when {
            isEncrypted || deviceLaunchedWithPie -> buildResult(
                status = SecurityStatus.SECURE,
                riskLevel = RiskLevel.NONE,
                detectedValue = if (deviceLaunchedWithPie) "File-Based Encryption" else "Encrypted",
                userExplanation = "Your device storage is encrypted",
                technicalDetails = "Devices launching with Android 10+ use mandatory file-based encryption (FBE)",
                whyItMatters = "Encrypted storage protects your data if your device is lost or stolen",
                recommendation = "Keep encryption enabled",
                remediation = null,
                confidence = Confidence.HIGH,
                detectionMethod = "StorageEncryptionStatus API",
                evidence = listOf(Evidence("Encryption Status", status.toString()), Evidence("API Level", Build.VERSION.SDK_INT.toString())),
                startTime = startTime
            )
            
            status == DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> buildResult(
                status = SecurityStatus.UNKNOWN,
                riskLevel = RiskLevel.NONE,
                detectedValue = "Not Readable",
                userExplanation = "Cannot determine encryption status",
                technicalDetails = "Encryption status API reports unsupported or access denied",
                whyItMatters = "Modern Android devices are encrypted by default",
                recommendation = "Check manually in Settings",
                remediation = RemediationAction("Open Security", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_SECURITY_SETTINGS),
                limitation = "API restricted on this device",
                confidence = Confidence.LOW,
                detectionMethod = "StorageEncryptionStatus API",
                evidence = listOf(Evidence("Status", "UNSUPPORTED")),
                startTime = startTime
            )
            
            else -> buildResult(
                status = SecurityStatus.WARNING,
                riskLevel = RiskLevel.MEDIUM,
                detectedValue = "Not Encrypted",
                userExplanation = "Your device storage may not be encrypted",
                technicalDetails = "Encryption status indicates inactive or not yet encrypted",
                whyItMatters = "Unencrypted storage can be read if device is accessed physically",
                recommendation = "Enable device encryption in Settings",
                remediation = RemediationAction("Open Security", RemediationAction.ActionType.OPEN_SPECIFIC_SETTINGS,
                    android.provider.Settings.ACTION_SECURITY_SETTINGS),
                confidence = Confidence.MEDIUM,
                detectionMethod = "StorageEncryptionStatus API",
                evidence = listOf(Evidence("Status", status.toString())),
                startTime = startTime
            )
        }
    }
}
