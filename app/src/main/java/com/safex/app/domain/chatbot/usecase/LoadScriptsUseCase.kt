package com.safex.app.domain.chatbot.usecase

import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.SafetyScriptBundle
import com.safex.app.domain.chatbot.repository.ScriptRepository
import javax.inject.Inject

class LoadScriptsUseCase @Inject constructor(
    private val scriptRepository: ScriptRepository
) {
    suspend operator fun invoke(language: ChatLanguage): SafetyScriptBundle {
        return scriptRepository.getScriptBundle(language)
    }
}
