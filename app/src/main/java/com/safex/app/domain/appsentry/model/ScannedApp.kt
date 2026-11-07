// domain/appsentry/model/ScannedApp.kt
package com.safex.app.domain.appsentry.model

import android.graphics.drawable.Drawable

/**
 * Domain model representing a scanned installed application.
 */
data class ScannedApp(
    val packageName: String,
    val appName: String,
    /** May be null when running unit tests or if icon loading fails. */
    val icon: Drawable?,
    val installedAt: Long,
    val updatedAt: Long,
    val versionName: String,
    val versionCode: Long,
    /** All declared permissions (uses-permission entries). */
    val permissions: List<String>,
    /** True if installed from an unknown source (not Play Store). */
    val isSideLoaded: Boolean,
    /** Installer package name, e.g. "com.android.vending" for Play Store. */
    val installerPackage: String?,
    /** Computed risk score 0-100. */
    val riskScore: Int,
    val riskLevel: AppRiskLevel,
    /** Set of threat signals found. */
    val threatIndicators: Set<ThreatIndicator>,
    /** When this scan was performed (epoch ms). */
    val scannedAt: Long,
    /** True if the user has explicitly marked this app as trusted. */
    val isTrusted: Boolean = false,
)
