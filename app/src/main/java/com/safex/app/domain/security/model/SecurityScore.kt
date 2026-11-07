package com.safex.app.domain.security.model

/**
 * Complete security score with transparency
 */
data class SecurityScore(
    val totalScore: Int, // 0-100
    val grade: Grade,
    val coverage: Float, // 0.0-1.0
    val categoryScores: Map<SecurityCategory, CategoryScore>,
    val message: String,
    val timestamp: Long
) {
    enum class Grade(val displayName: String, val description: String) {
        A("Excellent", "Your device security is excellent"),
        B("Good", "Your device security is good"),
        C("Needs Improvement", "Some security settings need attention"),
        D("Weak", "Your device has weak security"),
        F("Critical", "Critical security risks detected");
        
        companion object {
            fun fromScore(score: Int, coverage: Float): Grade {
                // Cap grade based on coverage
                val maxGrade = when {
                    coverage < 0.5f -> C
                    coverage < 0.7f -> B
                    else -> A
                }
                
                val scoreGrade = when {
                    score >= 90 -> A
                    score >= 80 -> B
                    score >= 65 -> C
                    score >= 45 -> D
                    else -> F
                }
                
                // Return the more conservative grade
                return if (scoreGrade.ordinal > maxGrade.ordinal) maxGrade else scoreGrade
            }
        }
    }
    
    data class CategoryScore(
        val category: SecurityCategory,
        val score: Float, // 0.0-1.0
        val maxPossible: Float,
        val verified: Int,
        val total: Int
    )
}
