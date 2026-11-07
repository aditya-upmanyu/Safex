package com.safex.app.domain.security.model

/**
 * Evidence supporting a security check result
 */
data class Evidence(
    val description: String,
    val value: String?,
    val weight: Float = 1.0f
)
