package com.safex.app.domain.security.model

/**
 * Security check categories for scoring and organization
 */
enum class SecurityCategory(
    val displayName: String,
    val weight: Float
) {
    DEVICE_PROTECTION("Device Protection", 0.30f),
    SYSTEM_SECURITY("System Security", 0.25f),
    NETWORK_SECURITY("Network Security", 0.25f),
    PRIVACY_EXPOSURE("Privacy & Exposure", 0.20f);
    
    companion object {
        fun allWeightsSum(): Float = entries.sumOf { it.weight.toDouble() }.toFloat()
    }
}
