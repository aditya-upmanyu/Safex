package com.safex.app.util

object Constants {
    // Database
    const val DATABASE_NAME = "safex_database"
    const val DATABASE_VERSION = 1

    // DataStore
    const val DATASTORE_NAME = "safex_preferences"

    // Notification Channels
    const val FRAUD_ALERT_CHANNEL_ID = "fraud_alert_channel"
    const val FRAUD_ALERT_CHANNEL_NAME = "Fraud Alerts"
    const val CALL_RECORDING_CHANNEL_ID = "call_recording_channel"
    const val CALL_RECORDING_CHANNEL_NAME = "Call Recording"

    // Intent Actions
    const val ACTION_STOP_RECORDING = "com.safex.app.STOP_RECORDING"
    const val ACTION_START_ANALYSIS = "com.safex.app.START_ANALYSIS"

    // SharedPreferences
    const val PREF_ONBOARDING_DONE = "onboarding_done"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_EMERGENCY_CONTACT = "emergency_contact"
    const val PREF_RISK_THRESHOLD = "risk_threshold"
    const val PREF_CALL_GUARD_ENABLED = "call_guard_enabled"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_OPENAI_KEY = "openai_key"
    const val PREF_STT_KEY = "stt_key"
    const val PREF_VIRUSTOTAL_KEY = "virustotal_key"

    // Default Values
    const val DEFAULT_RISK_THRESHOLD = 70
    const val DEFAULT_ANALYSIS_DELAY_SECONDS = 3L

    // API URLs
    const val OPENAI_BASE_URL = "https://api.openai.com/"
    const val GOOGLE_STT_BASE_URL = "https://speech.googleapis.com/"
    const val VIRUSTOTAL_BASE_URL = "https://www.virustotal.com/api/v3/"

    // Indian Fraud Keywords
    val DIGITAL_ARREST_KEYWORDS = listOf(
        "CBI", "ED", "RBI", "warrant", "arrest", "giraftari",
        "court", "case darj", "video call court", "police"
    )
    val KYC_FRAUD_KEYWORDS = listOf(
        "account freeze", "KYC update", "verify karo", "bank officer"
    )
    val OTP_FRAUD_KEYWORDS = listOf(
        "OTP", "share karo", "verify", "account unlock"
    )
    val LOTTERY_SCAM_KEYWORDS = listOf(
        "prize winner", "lottery", "claim karo", "processing fee"
    )
    val COURIER_SCAM_KEYWORDS = listOf(
        "parcel", "drug", "customs", "FedEx", "officer speaking"
    )
    val INVESTMENT_SCAM_KEYWORDS = listOf(
        "guaranteed returns", "SEBI registered", "expert trader"
    )
    val JOB_SCAM_KEYWORDS = listOf(
        "work from home", "advance fee", "training deposit"
    )

    // Government Helplines
    const val CYBER_HELPLINE = "1930"
    const val WOMEN_HELPLINE = "181"
    const val NCRP_URL = "https://cybercrime.gov.in"
    const val CHAKSHU_URL = "https://sancharsaathi.gov.in/sfc"
    const val CEIR_URL = "https://ceir.gov.in"
    const val TAFCOP_URL = "https://tafcop.sancharsaathi.gov.in"
}
