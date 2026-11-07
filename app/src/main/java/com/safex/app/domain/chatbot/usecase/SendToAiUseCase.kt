package com.safex.app.domain.chatbot.usecase

import com.safex.app.data.chatbot.AiRateLimiter
import com.safex.app.data.chatbot.GuardPrompts
import com.safex.app.data.chatbot.RateLimitStatus
import com.safex.app.domain.chatbot.model.AiReply
import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.repository.AiGuardRepository
import com.safex.app.domain.chatbot.repository.AiRequest
import com.safex.app.domain.chatbot.repository.AiResult
import com.safex.app.domain.chatbot.repository.ScriptRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SendToAiUseCase @Inject constructor(
    private val aiGuardRepository: AiGuardRepository,
    private val maskSensitiveDataUseCase: MaskSensitiveDataUseCase,
    private val rateLimiter: AiRateLimiter,
    private val scriptRepository: ScriptRepository
) {

    suspend operator fun invoke(
        userText: String,
        language: ChatLanguage,
        history: List<Pair<String, String>>,
        currentTopicId: String? = null,
        topicSummary: String? = null
    ): AiResult {
        // Step 1: Mask sensitive data on device
        val maskResult = maskSensitiveDataUseCase(userText)
        val maskedText = maskResult.maskedText

        // Step 2: Rate limit check
        when (val rateCheck = rateLimiter.checkLimit()) {
            is RateLimitStatus.HourlyExceeded -> {
                return AiResult.QuotaExceeded(rateCheck.minutesLeft)
            }
            is RateLimitStatus.DailyExceeded -> {
                return AiResult.QuotaExceeded(rateCheck.minutesLeft)
            }
            is RateLimitStatus.Throttled -> {
                return AiResult.QuotaExceeded(1)
            }
            is RateLimitStatus.Allowed -> {}
        }

        // Step 3: Call AI
        val request = AiRequest(
            language = language,
            currentTopicId = currentTopicId,
            topicSummary = topicSummary,
            history = history.takeLast(6),
            userText = maskedText
        )

        val result = aiGuardRepository.ask(request)

        // Step 4: Validate and sanitize on device
        if (result is AiResult.Success) {
            rateLimiter.recordCall()
            val sanitized = sanitizeAiOutput(result.reply, language)
            return AiResult.Success(sanitized)
        }

        return result
    }

    private suspend fun sanitizeAiOutput(rawReply: AiReply, language: ChatLanguage): AiReply {
        val bundle = scriptRepository.getScriptBundle(language)
        val validTopicIds = bundle.topics.map { it.id }.toSet()

        // 1. Headline max 15 words
        val words = rawReply.headline.split(Regex("\\s+"))
        val trimmedHeadline = if (words.size > 15) {
            words.take(15).joinToString(" ") + "..."
        } else {
            rawReply.headline
        }

        // 2. Fallback for do_now if empty
        val sanitizedDoNow = if (rawReply.doNow.isEmpty()) {
            listOf("Call 1930 Cyber Crime Helpline immediately if you lost money or feel threatened.")
        } else {
            rawReply.doNow.take(5).map { sanitizeSensitiveNumbers(it) }
        }

        // 3. Trim lists
        val sanitizedWhy = rawReply.why.take(4).map { sanitizeSensitiveNumbers(it) }
        val sanitizedDont = rawReply.dont.take(4).map { sanitizeSensitiveNumbers(it) }
        val sanitizedFollowUps = rawReply.followUpQuestions.take(3)

        // 4. Filter allowed report_to
        val allowedReport = rawReply.reportTo.filter { item ->
            GuardPrompts.ALLOWED_REPORT_TO.any { allowed -> item.contains(allowed, ignoreCase = true) }
        }

        // 5. Filter suggested topics
        val allowedTopicIds = rawReply.suggestedTopicIds.filter { validTopicIds.contains(it) }

        return rawReply.copy(
            headline = sanitizeSensitiveNumbers(trimmedHeadline),
            why = sanitizedWhy,
            doNow = sanitizedDoNow,
            dont = sanitizedDont,
            reportTo = allowedReport,
            suggestedTopicIds = allowedTopicIds,
            followUpQuestions = sanitizedFollowUps
        )
    }

    private fun sanitizeSensitiveNumbers(input: String): String {
        // If the reply contains a 4-8 digit code after otp/pin or 12/16 digit card/aadhaar, mask with ****
        val codeMaskRegex = Regex("""(?i)\b(otp|pin|cvv)\b[\s:=-]{1,15}?([0-9]{4,8})\b""")
        var res = codeMaskRegex.replace(input) { match ->
            "${match.groupValues[1]} ****"
        }
        val cardNumberRegex = Regex("""\b[0-9]{12,19}\b""")
        res = cardNumberRegex.replace(res, "****")
        return res
    }
}
