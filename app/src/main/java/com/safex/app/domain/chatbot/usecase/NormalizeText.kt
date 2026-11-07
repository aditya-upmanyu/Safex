package com.safex.app.domain.chatbot.usecase

object NormalizeText {
    private val devanagariDigits = mapOf(
        '०' to '0',
        '१' to '1',
        '२' to '2',
        '३' to '3',
        '४' to '4',
        '५' to '5',
        '६' to '6',
        '७' to '7',
        '८' to '8',
        '९' to '9'
    )

    // Punctuation characters to remove
    private val punctuationRegex = Regex("[\\p{Punct}\\p{IsPunctuation}&&[^@._\\-]]")
    private val whitespaceRegex = Regex("\\s+")

    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        // 1. Map Devanagari digits to ASCII digits
        val mappedChars = StringBuilder(input.length)
        for (ch in input) {
            mappedChars.append(devanagariDigits[ch] ?: ch)
        }

        // 2. Lowercase and replace punctuation with space (keep alphanumeric and words)
        val cleaned = mappedChars.toString().lowercase()
            .replace(punctuationRegex, " ")
            .replace(whitespaceRegex, " ")
            .trim()

        return cleaned
    }
}
