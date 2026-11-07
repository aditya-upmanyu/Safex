package com.safex.app.domain.security

import com.safex.app.domain.security.usecase.SecurityCheck
import com.safex.app.domain.security.usecase.checks.*

/**
 * Central registry of all security checks.
 * Single source of truth for check metadata and instantiation.
 */
object SecurityCheckRegistry {
    
    /**
     * Get all available security checks
     */
    fun getAllChecks(): List<SecurityCheck> = listOf(
        // Device Protection (5 checks)
        DeviceLockCheck(),
        DeviceEncryptionCheck(),
        TheftProtectionCheck(),
        LockScreenStrengthCheck(),
        LockScreenNotificationsCheck(),
        
        // System Security (6 checks)
        RootDetectionCheck(),
        USBDebuggingCheck(),
        WirelessDebuggingCheck(),
        DeveloperOptionsCheck(),
        SELinuxCheck(),
        UnknownSourcesCheck(),
        
        // Network Security (4 checks)
        WiFiSecurityCheck(),
        WiFiProxyCheck(),
        VPNCheck(),
        HotspotCheck(),
        
        // Privacy & Exposure (6 checks)
        BluetoothCheck(),
        NFCCheck(),
        AccessibilityServicesCheck(),
        UserCertificatesCheck(),
        PlayProtectCheck()
    )
    
    /**
     * Get check by ID
     */
    fun getCheckById(id: String): SecurityCheck? {
        return getAllChecks().find { it.id == id }
    }
}
