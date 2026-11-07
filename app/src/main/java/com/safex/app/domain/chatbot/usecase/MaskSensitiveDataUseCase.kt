package com.safex.app.domain.chatbot.usecase

data class MaskReplacement(
    val type: String,
    val count: Int
)

data class MaskResult(
    val maskedText: String,
    val replacements: List<MaskReplacement>
)

interface MaskSensitiveDataUseCase {
    operator fun invoke(rawText: String): MaskResult
}
