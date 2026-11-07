package com.safex.app.domain.chatbot.usecase

import com.safex.app.domain.chatbot.model.IntentMatchResult
import com.safex.app.domain.chatbot.repository.ScriptRepository
import javax.inject.Inject

class MatchIntentUseCase @Inject constructor(
    private val scriptRepository: ScriptRepository
) {
    suspend operator fun invoke(inputText: String): IntentMatchResult {
        val normalized = NormalizeText.normalize(inputText)
        if (normalized.isBlank()) {
            return IntentMatchResult(bestTopicId = null, score = 0, alternatives = emptyList())
        }

        val wordCount = normalized.split(" ").filter { it.isNotBlank() }.size
        val keywordsMap = scriptRepository.getIntentKeywords()

        val scores = mutableMapOf<String, Int>()

        for ((topicId, keywords) in keywordsMap) {
            var topicScore = 0
            for (rawKeyword in keywords) {
                val kw = NormalizeText.normalize(rawKeyword)
                if (kw.isBlank()) continue

                val isMultiWord = kw.contains(" ")
                val points = if (isMultiWord) 2 else 1

                val hit = if (isMultiWord) {
                    normalized.contains(kw)
                } else {
                    // Match whole word or exact token for Indic/ASCII
                    normalized.split(" ").contains(kw) || 
                        (kw.length >= 3 && Regex("(^|\\s)${Regex.escape(kw)}(\\s|$)").containsMatchIn(normalized))
                }

                if (hit) {
                    topicScore += points
                }
            }
            if (topicScore > 0) {
                scores[topicId] = topicScore
            }
        }

        if (scores.isEmpty()) {
            return IntentMatchResult(bestTopicId = null, score = 0, alternatives = emptyList())
        }

        // Sort descending by score
        val sorted = scores.entries.sortedByDescending { it.value }
        val topEntry = sorted.first()
        val topScore = topEntry.value

        val qualifies = topScore >= 2 || (topScore >= 1 && wordCount < 5)

        return if (qualifies) {
            val alternatives = sorted.drop(1)
                .take(2)
                .map { it.key }
            IntentMatchResult(
                bestTopicId = topEntry.key,
                score = topScore,
                alternatives = alternatives
            )
        } else {
            val alternatives = sorted.take(2).map { it.key }
            IntentMatchResult(
                bestTopicId = null,
                score = topScore,
                alternatives = alternatives
            )
        }
    }
}
