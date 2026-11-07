package com.safex.app.data.chatbot

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.safex.app.data.chatbot.dto.ScriptBundleDto
import com.safex.app.domain.chatbot.model.ChatLanguage
import com.safex.app.domain.chatbot.model.SafetyScriptBundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScriptAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val bundleCache = ConcurrentHashMap<ChatLanguage, SafetyScriptBundle>()
    private var intentKeywordsCache: Map<String, List<String>>? = null

    suspend fun getScriptBundle(language: ChatLanguage): SafetyScriptBundle {
        bundleCache[language]?.let { return it }

        return withContext(Dispatchers.IO) {
            val fileName = when (language) {
                ChatLanguage.EN -> "chat/scripts_en.json"
                ChatLanguage.HI -> "chat/scripts_hi.json"
                ChatLanguage.HINGLISH -> "chat/scripts_hinglish.json"
            }

            val bundle = try {
                context.assets.open(fileName).use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        val dto = gson.fromJson(reader, ScriptBundleDto::class.java)
                        dto.toDomain()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Return safe default empty bundle if loading fails
                SafetyScriptBundle(
                    version = 1,
                    language = language.code,
                    ui = com.safex.app.data.chatbot.dto.UiStringsDto().toDomain(),
                    topics = emptyList(),
                    checkers = emptyList()
                )
            }

            bundleCache[language] = bundle
            bundle
        }
    }

    suspend fun getIntentKeywords(): Map<String, List<String>> {
        intentKeywordsCache?.let { return it }

        return withContext(Dispatchers.IO) {
            val keywords = try {
                context.assets.open("chat/intent_keywords.json").use { inputStream ->
                    InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                        val type = object : TypeToken<Map<String, List<String>>>() {}.type
                        gson.fromJson<Map<String, List<String>>>(reader, type)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyMap()
            }
            intentKeywordsCache = keywords
            keywords
        }
    }
}
