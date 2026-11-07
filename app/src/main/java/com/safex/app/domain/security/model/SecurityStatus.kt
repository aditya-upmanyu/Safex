package com.safex.app.domain.security.model

/**
 * Technical status of a security check result.
 * Separate from user-facing RiskLevel to maintain honesty.
 */
enum class SecurityStatus {
    /** Check passed, configuration is secure */
    SECURE,
    
    /** Potential issue detected, requires attention */
    WARNING,
    
    /** Critical security risk detected */
    DANGER,
    
    /** Informational check (not scored) */
    INFO,
    
    /** Unable to determine status (API restricted/permission denied) */
    UNKNOWN,
    
    /** Check not supported on this device/Android version */
    UNSUPPORTED,
    
    /** Check currently running */
    CHECKING,
    
    /** Check failed due to error */
    ERROR
}
