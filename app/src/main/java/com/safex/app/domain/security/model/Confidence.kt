package com.safex.app.domain.security.model

/**
 * Confidence level in the detection result
 */
enum class Confidence(val multiplier: Float) {
    /** High confidence in the result */
    HIGH(1.0f),
    
    /** Medium confidence, some uncertainty */
    MEDIUM(0.75f),
    
    /** Low confidence, heuristic-based */
    LOW(0.5f)
}
