package com.safex.app.domain.chatbot.usecase

import com.safex.app.domain.chatbot.model.EmergencyDetectionResult
import com.safex.app.domain.chatbot.model.EmergencyLevel
import javax.inject.Inject

class DetectEmergencyUseCase @Inject constructor() {

    private data class EmergencyRule(
        val keywords: List<String>,
        val topicId: String,
        val level: EmergencyLevel
    )

    private val rules = listOf(
        EmergencyRule(
            keywords = listOf(
                "digital arrest", "arrest warrant", "warrant", "arrest", "giraftar", "giraftari",
                "डिजिटल अरेस्ट", "गिरफ्तार", "गिरफ्तारी", "वारंट", "cbi arrest", "police arrest"
            ),
            topicId = "digital_arrest",
            level = EmergencyLevel.EMERGENCY
        ),
        EmergencyRule(
            keywords = listOf(
                "paise kat gaye", "paisa kat gaya", "paisa transfer kar diya", "money deducted",
                "money debited", "unauthorized debit", "fraud transaction", "पैसे कट गए", "पैसे कट",
                "पैसा चला गया", "खाता खाली", "एकाउंट खाली", "account empty", "stolen money"
            ),
            topicId = "money_lost",
            level = EmergencyLevel.EMERGENCY
        ),
        EmergencyRule(
            keywords = listOf(
                "blackmail", "dhamki", "video viral", "photo viral", "ब्लैकमेल", "धमकी",
                "वीडियो वायरल", "फोटो वायरल", "sextortion", "nude video", "morphed photo"
            ),
            topicId = "blackmail",
            level = EmergencyLevel.EMERGENCY
        ),
        EmergencyRule(
            keywords = listOf(
                "hack ho gaya", "account hacked", "whatsapp hack", "whatsapp hacked",
                "हैक हो गया", "खाता हैक", "अकाउंट हैक", "instagram hack"
            ),
            topicId = "account_hacked",
            level = EmergencyLevel.URGENT
        ),
        EmergencyRule(
            keywords = listOf(
                "otp share kar diya", "pin share kar diya", "otp bata diya", "pin bata diya",
                "ओटीपी बता दिया", "पिन बता दिया"
            ),
            topicId = "money_lost",
            level = EmergencyLevel.EMERGENCY
        )
    )

    operator fun invoke(text: String): EmergencyDetectionResult {
        val normalized = NormalizeText.normalize(text)
        if (normalized.isBlank()) {
            return EmergencyDetectionResult(EmergencyLevel.NONE, null, false)
        }

        for (rule in rules) {
            for (keyword in rule.keywords) {
                val normalizedKw = NormalizeText.normalize(keyword)
                if (normalized.contains(normalizedKw)) {
                    return EmergencyDetectionResult(
                        level = rule.level,
                        topicId = rule.topicId,
                        isEmergency = true,
                        matchedKeyword = keyword
                    )
                }
            }
        }

        return EmergencyDetectionResult(EmergencyLevel.NONE, null, false)
    }
}
