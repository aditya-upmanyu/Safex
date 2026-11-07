// domain/appsentry/model/ThreatIndicator.kt
package com.safex.app.domain.appsentry.model

/**
 * Individual threat signals detected during app/APK inspection.
 * Each flag carries a weight used by [CalculateAppRiskUseCase] to produce the composite score.
 */
enum class ThreatIndicator(val weight: Int, val description: String) {
    // Permission-based threats
    REQUESTS_ACCESSIBILITY(20, "Requests Accessibility Service — commonly abused by RATs"),
    REQUESTS_DEVICE_ADMIN(20, "Requests Device Administrator — can lock/wipe device"),
    REQUESTS_OVERLAY(10, "Requests Draw Over Other Apps — used in overlay/phishing attacks"),
    READS_SMS(10, "Reads SMS — can intercept OTPs"),
    READS_CONTACTS(5, "Reads all Contacts"),
    READS_CALL_LOG(5, "Reads Call Log"),
    EXCESSIVE_PERMISSIONS(15, "Requests an unusually large number of permissions (>12)"),

    // Installation origin
    NOT_FROM_PLAY_STORE(15, "Not installed from Google Play Store"),
    SIDE_LOADED(20, "Side-loaded APK — not from any known store"),

    // Package identity threats
    FAKE_BANKING_APP(40, "Package name mimics a legitimate banking or payment app"),
    FAKE_GOVT_APP(35, "Package name mimics an official government application"),
    KNOWN_RAT_PACKAGE(50, "Matches a known Remote Access Trojan package signature"),
    SUSPICIOUS_PACKAGE_PATTERN(25, "Package name matches a suspicious pattern"),

    // APK-specific
    UNSIGNED_APK(30, "APK has no valid signature"),
    MULTIPLE_SIGNATURES(10, "APK signed with multiple certificates — unusual for legit apps"),
    VIRUSTOTAL_POSITIVE(60, "Flagged as malicious by VirusTotal engines"),
    VIRUSTOTAL_SUSPICIOUS(30, "Flagged as suspicious by VirusTotal engines"),

    // Behavioural
    BACKGROUND_DATA_ACCESS(10, "Requests background location or background data"),
    USES_CAMERA_MICROPHONE(10, "Requests both Camera and Microphone — spyware risk"),
}
