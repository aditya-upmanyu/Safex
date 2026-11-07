package com.safex.app.domain.chatbot.model

enum class ChatLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    HI("hi", "हिन्दी"),
    HINGLISH("hinglish", "Hinglish");

    companion object {
        fun fromCode(code: String?): ChatLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
        }
    }
}

enum class Urgency {
    NORMAL,
    URGENT,
    EMERGENCY
}

enum class Verdict {
    LOW_RISK,
    SUSPICIOUS,
    LIKELY_SCAM,
    INFO
}

enum class ActionType {
    DIAL,
    OPEN_URL,
    OPEN_CHECKER,
    OPEN_TOPIC,
    COPY,
    NONE
}

data class ScriptAction(
    val type: ActionType,
    val value: String
)

enum class ContactType {
    DIAL,
    URL
}

data class ContactInfo(
    val label: String,
    val type: ContactType,
    val value: String
)

data class DoNowStep(
    val text: String,
    val action: ScriptAction = ScriptAction(ActionType.NONE, "")
)

data class FollowUpChip(
    val label: String,
    val topicId: String? = null,
    val askAi: String? = null
)
