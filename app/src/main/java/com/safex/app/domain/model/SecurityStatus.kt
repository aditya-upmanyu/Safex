package com.safex.app.domain.model

data class SecurityStatus(
    val overallScore: Int,
    val isRooted: Boolean,
    val hasScreenLock: Boolean,
    val playProtectStatus: String,
    val developerModeEnabled: Boolean,
    val unknownSourcesEnabled: Boolean,
    val checks: List<SecurityCheck>
)

data class SecurityCheck(
    val name: String,
    val status: CheckStatus,
    val message: String
)

enum class CheckStatus {
    OK,
    WARNING,
    ALERT
}
