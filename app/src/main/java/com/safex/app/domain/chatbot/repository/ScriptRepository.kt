package com.safex.app.domain.chatbot.repository

import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.Checker
import com.safex.app.domain.chatbot.model.SafetyScriptBundle
import com.safex.app.domain.chatbot.model.SafetyTopic

interface ScriptRepository {
    suspend fun getScriptBundle(language: ChatLanguage): SafetyScriptBundle
    suspend fun getTopic(topicId: String, language: ChatLanguage): SafetyTopic?
    suspend fun getChecker(checkerId: String, language: ChatLanguage): Checker?
    suspend fun getIntentKeywords(): Map<String, List<String>>
}
