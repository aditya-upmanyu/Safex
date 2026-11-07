package com.safex.app.presentation.chatbot.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.safex.app.domain.chatbot.model.ChatLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SpeechState {
    data object Idle : SpeechState
    data object Preparing : SpeechState
    data class Listening(val rmsLevel: Float) : SpeechState
    data object Processing : SpeechState
    data class Result(val text: String) : SpeechState
    data class Error(val message: String, val canRetry: Boolean = true) : SpeechState
}

@Singleton
class SpeechInputController @Inject constructor(
    @ApplicationContext private val context: Context
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private var smoothedRms: Float = 0f

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(language: ChatLanguage) {
        if (!isRecognitionAvailable()) {
            _speechState.value = SpeechState.Error("Speech recognition is not available on this device", false)
            return
        }

        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechInputController)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                val langTag = when (language) {
                    ChatLanguage.EN -> "en-IN"
                    ChatLanguage.HI, ChatLanguage.HINGLISH -> "hi-IN"
                }
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            _partialTranscript.value = ""
            smoothedRms = 0f
            _speechState.value = SpeechState.Preparing
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _speechState.value = SpeechState.Error("Could not start microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        } finally {
            speechRecognizer = null
            _speechState.value = SpeechState.Idle
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _speechState.value = SpeechState.Listening(0f)
    }

    override fun onBeginningOfSpeech() {
        _speechState.value = SpeechState.Listening(0.2f)
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalise and smooth dB (-2 to 10 dB typically) to 0..1 range with Exponential Moving Average
        val clamped = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        smoothedRms = (smoothedRms * 0.7f) + (clamped * 0.3f)
        if (_speechState.value is SpeechState.Listening) {
            _speechState.value = SpeechState.Listening(smoothedRms)
        }
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _speechState.value = SpeechState.Processing
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check microphone."
            SpeechRecognizer.ERROR_CLIENT -> "Client speech error."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
            SpeechRecognizer.ERROR_NETWORK -> "Network issue for voice service."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Voice network request timed out."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Please speak clearly and try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please try again."
            SpeechRecognizer.ERROR_SERVER -> "Voice server error."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Tap the mic to talk."
            else -> "Speech error ($error). Please try again."
        }
        _speechState.value = SpeechState.Error(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) {
            _partialTranscript.value = text
            _speechState.value = SpeechState.Result(text)
        } else {
            _speechState.value = SpeechState.Error("No words recognized. Please try again.")
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull()
        if (!partial.isNullOrBlank()) {
            _partialTranscript.value = partial
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
