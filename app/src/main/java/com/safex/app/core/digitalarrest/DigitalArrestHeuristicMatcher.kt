package com.safex.app.core.digitalarrest

/**
 * On-device heuristic text analysis engine for Digital Arrest scam detection.
 *
 * IMPORTANT DESIGN RULES:
 * - A keyword match alone does NOT make a call fraudulent.
 * - Context + multiple indicators are required for elevated scores.
 * - "I am preparing for a CBI examination" must NOT trigger a critical warning.
 * - Negation handling: "not arrested", "not from CBI" reduces score.
 * - Scoring is deterministic and testable — no randomness.
 *
 * Supports: English, Hindi, Hinglish (mixed).
 */
object DigitalArrestHeuristicMatcher {

    data class MatchResult(
        val score: Int,                     // 0–100
        val matchedIndicators: List<Indicator>,
        val negationDetected: Boolean,
        val dominantCategory: IndicatorCategory?
    )

    data class Indicator(
        val phrase: String,
        val category: IndicatorCategory,
        val weight: Int,
        val language: String
    )

    enum class IndicatorCategory {
        IMPERSONATION,      // Claiming to be govt/law enforcement
        ARREST_THREAT,      // Threatening arrest / legal action
        FINANCIAL_COERCION, // Asking to transfer money
        SECRECY_DEMAND,     // "Don't tell family", "keep confidential"
        PROCEDURE_FRAUD,    // Fake procedures (video interrogation, verification account)
        LEGITIMATE_CONTEXT  // Negative weight — reduces score (e.g. "studying for CBI exam")
    }

    // Each entry: (phrase, category, weight, language)
    // weight: positive = suspicious, negative = legitimate context reducer
    private val indicators = listOf(
        // ── IMPERSONATION (English) ──
        Indicator("cbi officer",            IndicatorCategory.IMPERSONATION, 20, "en"),
        Indicator("i am from cbi",          IndicatorCategory.IMPERSONATION, 25, "en"),
        Indicator("ed investigation",       IndicatorCategory.IMPERSONATION, 20, "en"),
        Indicator("enforcement directorate",IndicatorCategory.IMPERSONATION, 20, "en"),
        Indicator("customs officer",        IndicatorCategory.IMPERSONATION, 18, "en"),
        Indicator("narcotics bureau",       IndicatorCategory.IMPERSONATION, 20, "en"),
        Indicator("supreme court warrant",  IndicatorCategory.IMPERSONATION, 25, "en"),
        Indicator("rbi officer",            IndicatorCategory.IMPERSONATION, 22, "en"),
        Indicator("cybercrime department",  IndicatorCategory.IMPERSONATION, 20, "en"),

        // ── ARREST THREAT (English) ──
        Indicator("digital arrest",         IndicatorCategory.ARREST_THREAT, 35, "en"),
        Indicator("you are arrested",       IndicatorCategory.ARREST_THREAT, 30, "en"),
        Indicator("arrest warrant",         IndicatorCategory.ARREST_THREAT, 28, "en"),
        Indicator("police custody",         IndicatorCategory.ARREST_THREAT, 25, "en"),
        Indicator("account freeze",         IndicatorCategory.ARREST_THREAT, 20, "en"),
        Indicator("will be arrested",       IndicatorCategory.ARREST_THREAT, 28, "en"),
        Indicator("money laundering",       IndicatorCategory.ARREST_THREAT, 20, "en"),
        Indicator("aadhaar linked to drug", IndicatorCategory.ARREST_THREAT, 30, "en"),
        Indicator("drug parcel",            IndicatorCategory.ARREST_THREAT, 25, "en"),

        // ── FINANCIAL COERCION (English) ──
        Indicator("transfer money",         IndicatorCategory.FINANCIAL_COERCION, 30, "en"),
        Indicator("send money to verify",   IndicatorCategory.FINANCIAL_COERCION, 35, "en"),
        Indicator("verification account",   IndicatorCategory.FINANCIAL_COERCION, 30, "en"),
        Indicator("rbi verification",       IndicatorCategory.FINANCIAL_COERCION, 35, "en"),
        Indicator("pay to prove innocence", IndicatorCategory.FINANCIAL_COERCION, 40, "en"),
        Indicator("share otp",              IndicatorCategory.FINANCIAL_COERCION, 30, "en"),
        Indicator("share your otp",         IndicatorCategory.FINANCIAL_COERCION, 35, "en"),
        Indicator("refund process",         IndicatorCategory.FINANCIAL_COERCION, 15, "en"),

        // ── SECRECY DEMAND (English) ──
        Indicator("do not tell your family",IndicatorCategory.SECRECY_DEMAND, 35, "en"),
        Indicator("don't tell anyone",      IndicatorCategory.SECRECY_DEMAND, 35, "en"),
        Indicator("keep this confidential", IndicatorCategory.SECRECY_DEMAND, 30, "en"),
        Indicator("do not hang up",         IndicatorCategory.SECRECY_DEMAND, 25, "en"),
        Indicator("stay on the call",       IndicatorCategory.SECRECY_DEMAND, 25, "en"),
        Indicator("do not inform",          IndicatorCategory.SECRECY_DEMAND, 30, "en"),

        // ── PROCEDURE FRAUD (English) ──
        Indicator("skype interrogation",    IndicatorCategory.PROCEDURE_FRAUD, 35, "en"),
        Indicator("video interrogation",    IndicatorCategory.PROCEDURE_FRAUD, 35, "en"),
        Indicator("online interrogation",   IndicatorCategory.PROCEDURE_FRAUD, 30, "en"),
        Indicator("video call hearing",     IndicatorCategory.PROCEDURE_FRAUD, 30, "en"),

        // ── HINDI indicators ──
        Indicator("डिजिटल अरेस्ट",          IndicatorCategory.ARREST_THREAT,   35, "hi"),
        Indicator("cbi अधिकारी",             IndicatorCategory.IMPERSONATION,   25, "hi"),
        Indicator("गिरफ्तार किया जाएगा",      IndicatorCategory.ARREST_THREAT,   30, "hi"),
        Indicator("पैसे ट्रांसफर करें",        IndicatorCategory.FINANCIAL_COERCION, 30, "hi"),
        Indicator("परिवार को मत बताओ",        IndicatorCategory.SECRECY_DEMAND,  35, "hi"),
        Indicator("खाता फ्रीज",              IndicatorCategory.ARREST_THREAT,   20, "hi"),
        Indicator("वीडियो पूछताछ",            IndicatorCategory.PROCEDURE_FRAUD, 35, "hi"),
        Indicator("otp शेयर करें",            IndicatorCategory.FINANCIAL_COERCION, 30, "hi"),
        Indicator("ड्रग्स पार्सल",            IndicatorCategory.ARREST_THREAT,   25, "hi"),
        Indicator("सत्यापन खाता",             IndicatorCategory.FINANCIAL_COERCION, 30, "hi"),

        // ── LEGITIMATE CONTEXT REDUCERS (negative weight) ──
        Indicator("preparing for cbi",      IndicatorCategory.LEGITIMATE_CONTEXT, -30, "en"),
        Indicator("cbi exam",               IndicatorCategory.LEGITIMATE_CONTEXT, -30, "en"),
        Indicator("studying cbi",           IndicatorCategory.LEGITIMATE_CONTEXT, -25, "en"),
        Indicator("read about ed",          IndicatorCategory.LEGITIMATE_CONTEXT, -20, "en"),
        Indicator("news about cbi",         IndicatorCategory.LEGITIMATE_CONTEXT, -20, "en"),
        Indicator("cbi की परीक्षा",          IndicatorCategory.LEGITIMATE_CONTEXT, -25, "hi")
    )

    // Negation phrases that reduce nearby indicator weight
    private val negationPhrases = listOf(
        "not from", "not a", "i am not", "i'm not", "नहीं हूं", "नहीं है",
        "no arrest", "no warrant", "do not", "don't", "never"
    )

    fun analyze(text: String): MatchResult {
        if (text.isBlank()) return MatchResult(0, emptyList(), false, null)

        val normalized = text.lowercase().trim()
        val matched = mutableListOf<Indicator>()
        val seenCategories = mutableSetOf<String>()
        var totalScore = 0
        var negationDetected = false

        // Detect negation context
        val hasNegation = negationPhrases.any { normalized.contains(it) }
        if (hasNegation) negationDetected = true

        for (indicator in indicators) {
            val phrase = indicator.phrase.lowercase()
            if (normalized.contains(phrase)) {
                // Deduplicate: only count each unique phrase once
                val key = "${indicator.category}:$phrase"
                if (seenCategories.contains(key)) continue
                seenCategories.add(key)

                var w = indicator.weight
                // Apply negation penalty: reduce suspicious weight by 50% if negation nearby
                if (hasNegation && w > 0) w = (w * 0.5).toInt()
                // Legitimate context reducers always apply fully
                matched.add(indicator.copy(weight = w))
                totalScore += w
            }
        }

        // Clamp 0–100
        val finalScore = totalScore.coerceIn(0, 100)

        val dominant = matched
            .filter { it.weight > 0 }
            .groupBy { it.category }
            .maxByOrNull { it.value.sumOf { i -> i.weight } }
            ?.key

        return MatchResult(
            score = finalScore,
            matchedIndicators = matched,
            negationDetected = negationDetected,
            dominantCategory = dominant
        )
    }
}
