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

    // Indian Fraud Keywords - Comprehensive Pattern Database
    val DIGITAL_ARREST_KEYWORDS = listOf(
        // CBI Keywords
        "CBI", "central bureau investigation", "federal investigation",
        // ED Keywords
        "ED", "enforcement directorate", "money laundering",
        // RBI Keywords
        "RBI", "reserve bank", "banking regulator",
        // ITD Keywords
        "income tax", "IT", "tax department", "tax investigation",
        // Police Keywords
        "police", "FIR", "case registered", "arrest warrant",
        // Legal Keywords
        "warrant", "court case", "case filed", "legal action",
        "court order", "bail", "custody", "giraftari",
        // Hindi Mixed
        "giraftari", "girftaar", "pakda jayega", "case darj",
        "video call court", "virtual court", "e-court",
        // Threat Keywords
        "serious action", "immediate action", "urgent action",
        "freeze account", "block account", "suspend account"
    )
    
    val KYC_FRAUD_KEYWORDS = listOf(
        // Account Freeze/Verification
        "account freeze", "frozen account", "account suspended",
        "account locked", "verify account", "account verification",
        // KYC
        "KYC update", "KYC verification", "KYC compliance",
        "complete KYC", "update KYC immediately",
        // Bank Identity
        "bank officer", "bank staff", "banking official",
        "RBI official", "banking authority", "bank representative",
        // Personal Info Request
        "verify karo", "confirm karo", "identity verification",
        "account details", "personal details", "confidential information",
        // Urgency
        "update immediately", "verify now", "confirm details quickly"
    )
    
    val OTP_FRAUD_KEYWORDS = listOf(
        // OTP Related
        "OTP", "one time password", "one-time code",
        "verification code", "security code", "PIN code",
        // Share Request
        "share OTP", "share code", "tell me OTP", "read OTP",
        "provide code", "enter OTP", "give password",
        // Account Actions
        "unlock account", "verify account", "confirm payment",
        "authorize transaction", "confirm identity",
        // Threats
        "OTP timeout", "OTP expiry", "code expiring",
        "urgent verification", "immediate confirmation"
    )
    
    val LOTTERY_SCAM_KEYWORDS = listOf(
        // Win Claims
        "prize winner", "congratulations won", "you have won",
        "claim prize", "claim reward", "winning amount",
        "lottery won", "drawn winner", "selected winner",
        // Lottery Types
        "lottery", "lucky draw", "prize draw", "raffle",
        "sweepstake", "contest winner", "lucky person",
        // Amount Claims
        "lakhs", "crores", "lakh rupees", "crore rupees",
        "huge amount", "big money", "jackpot",
        // Process
        "claim karo", "processing fee", "transfer fee",
        "claim fee", "document fee", "tax payment required",
        // Suspicious
        "you didn't participate", "automatic entry", "lucky selection"
    )
    
    val COURIER_SCAM_KEYWORDS = listOf(
        // Package Claims
        "parcel", "package", "delivery", "shipment",
        "box", "cargo", "consignment", "customs",
        // Illegal Claims
        "drug", "drugs", "narcotics", "illegal items",
        "prohibited", "contraband", "seized package",
        // Authority Impersonation
        "customs officer", "courier officer", "customs authority",
        "FedEx", "DHL", "Amazon", "official speaking",
        // Threats
        "arrest warrant", "legal action", "court case",
        "fine amount", "penalty", "deposit required",
        // Action Required
        "pay fine", "deposit money", "verify details immediately"
    )
    
    val INVESTMENT_SCAM_KEYWORDS = listOf(
        // Returns Promise
        "guaranteed returns", "guaranteed profit", "assured returns",
        "100% return", "fixed income", "guaranteed income",
        "risk-free investment", "safe investment",
        // Authority Claims
        "SEBI registered", "SEBI approved", "regulatory approved",
        "government approved", "certified", "verified investment",
        // Expert Claims
        "expert trader", "professional manager", "fund manager",
        "investment expert", "portfolio manager", "financial expert",
        // Attractive Terms
        "high returns", "quick returns", "easy money",
        "passive income", "minimum investment", "no risk",
        // Company Names
        "investment group", "trading company", "forex trader",
        "crypto currency", "bitcoin", "international company"
    )
    
    val JOB_SCAM_KEYWORDS = listOf(
        // Job Offers
        "work from home", "remote job", "part-time job",
        "urgent hiring", "recruitment", "job offer",
        "position available", "hiring now", "positions open",
        // Easy Work
        "easy money", "simple work", "no experience needed",
        "no qualification needed", "easy process", "quick cash",
        // Suspicious Terms
        "advance fee", "registration fee", "training fee",
        "processing fee", "deposit required", "upfront payment",
        // Company Claims
        "multinational", "international company", "US company",
        "UK company", "reputed company", "established firm",
        // Contact
        "work for us", "join our team", "be our agent",
        "become distributor", "franchise opportunity"
    )
    
    // UPI & Payment Fraud (NEW)
    val UPI_FRAUD_KEYWORDS = listOf(
        // UPI Claims
        "UPI", "google pay", "paytm", "phonepe", "whatsapp pay",
        "payment app", "digital payment", "online transfer",
        // Verification Tricks
        "verify UPI", "confirm UPI", "link UPI",
        "UPI registration", "update UPI", "register payment",
        // Reversal Scams
        "money reversed", "payment failed", "transaction failed",
        "refund pending", "claim refund", "get money back",
        // Threats
        "account blocked", "UPI locked", "payment suspended",
        "transaction limit exceeded", "compliance issue"
    )
    
    // Technical Support Scam (NEW)
    val TECH_SUPPORT_KEYWORDS = listOf(
        // Claims
        "tech support", "technical support", "customer support",
        "microsoft", "apple", "google", "antivirus",
        "virus detected", "malware found", "security issue",
        // Actions
        "allow remote access", "give access", "remote desktop",
        "click link", "download software", "run command",
        // Threats
        "system compromised", "data at risk", "immediate action",
        "fix now", "solve immediately", "urgent problem"
    )

    // Government Helplines
    const val CYBER_HELPLINE = "1930"
    const val WOMEN_HELPLINE = "181"
    const val NCRP_URL = "https://cybercrime.gov.in"
    const val CHAKSHU_URL = "https://sancharsaathi.gov.in/sfc"
    const val CEIR_URL = "https://ceir.gov.in"
    const val TAFCOP_URL = "https://tafcop.sancharsaathi.gov.in"
}
