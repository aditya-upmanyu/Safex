package com.safex.app.core.security.model

/**
 * Snapshot of a single module's current protection state.
 * Shown on the Core Defense Dashboard.
 */
data class ModuleStatus(
    val module: ThreatModule,
    val displayName: String,
    val isEnabled: Boolean,
    val protectionStatus: ProtectionStatus,
    val detectionStatus: DetectionStatus,
    val lastEventTime: Long? = null,
    val lastEventDescription: String? = null,
    val permissionsRequired: List<String> = emptyList(),
    val currentThreatLevel: ThreatLevel = ThreatLevel.SAFE
)

enum class ProtectionStatus {
    PROTECTED,
    ACTIVE,
    WARNING,
    CRITICAL,
    PERMISSION_REQUIRED,
    NOT_VERIFIED,
    NOT_CONFIGURED,
    ERROR,
    DISABLED
}
