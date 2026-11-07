package com.safex.app.domain.security.model

/**
 * User-facing risk level assessment.
 * Independent from technical SecurityStatus.
 */
enum class RiskLevel {
    /** No risk identified */
    NONE,
    
    /** Minor concern, low priority */
    LOW,
    
    /** Moderate concern, should address soon */
    MEDIUM,
    
    /** Serious concern, address immediately */
    HIGH,
    
    /** Critical security risk, urgent action required */
    CRITICAL
}
