package com.safex.app.data.chatbot

import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.Checker
import com.safex.app.domain.chatbot.model.SafetyScriptBundle
import com.safex.app.domain.chatbot.model.SafetyTopic
import com.safex.app.domain.chatbot.repository.ScriptRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptRepositoryImpl @Inject constructor(
    private val scriptAssetDataSource: ScriptAssetDataSource
) : ScriptRepository {

    override suspend fun getScriptBundle(language: ChatLanguage): SafetyScriptBundle {
        return scriptAssetDataSource.getScriptBundle(language)
    }

    override suspend fun getTopic(topicId: String, language: ChatLanguage): SafetyTopic? {
        val bundle = scriptAssetDataSource.getScriptBundle(language)
        return bundle.topics.firstOrNull { it.id == topicId }
    }

    override suspend fun getChecker(checkerId: String, language: ChatLanguage): Checker? {
        val bundle = scriptAssetDataSource.getScriptBundle(language)
        return bundle.checkers.firstOrNull { it.id == checkerId }
    }

    override suspend fun getIntentKeywords(): Map<String, List<String>> {
        return scriptAssetDataSource.getIntentKeywords()
    }
}
