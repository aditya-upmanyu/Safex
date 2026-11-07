package com.safex.app.presentation.chatbot.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.safex.app.domain.chatbot.model.ChatLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsController @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        } else {
            isInitialized = false
        }
    }

    fun speak(
        headline: String,
        doNowSteps: List<String>,
        language: ChatLanguage,
        speechRate: Float = 0.9f
    ) {
        if (!isInitialized || textToSpeech == null) return

        stop()

        val locale = when (language) {
            ChatLanguage.HI, ChatLanguage.HINGLISH -> Locale("hi", "IN")
            ChatLanguage.EN -> Locale("en", "IN")
        }

        try {
            val result = textToSpeech?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.language = Locale.ENGLISH
            }
            textToSpeech?.setSpeechRate(speechRate)

            val naturalText = buildSpeechText(headline, doNowSteps)
            val chunks = chunkText(naturalText)

            for ((index, chunk) in chunks.withIndex()) {
                val utteranceId = "utterance_${System.currentTimeMillis()}_$index"
                val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                textToSpeech?.speak(chunk, queueMode, null, utteranceId)
            }
        } catch (_: Exception) {
            _isSpeaking.value = false
        }
    }

    fun stop() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {
        } finally {
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        try {
            textToSpeech?.shutdown()
        } catch (_: Exception) {
        } finally {
            textToSpeech = null
            isInitialized = false
        }
    }

    private fun buildSpeechText(headline: String, steps: List<String>): String {
        val sb = StringBuilder()
        sb.append(headline.replace("1930", "1 9 3 0"))
        sb.append(". ")

        for ((idx, step) in steps.take(4).withIndex()) {
            val formattedStep = step.replace("1930", "1 9 3 0")
                .replace(Regex("\\[.*?\\]"), "") // skip masked tokens like [CODE]
            sb.append("Step ${idx + 1}: $formattedStep. ")
        }
        return sb.toString().trim()
    }

    private fun chunkText(text: String, maxChunkLength: Int = 3900): List<String> {
        if (text.length <= maxChunkLength) return listOf(text)

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = (start + maxChunkLength).coerceAtMost(text.length)
            chunks.add(text.substring(start, end))
            start = end
        }
        return chunks
    }
}
