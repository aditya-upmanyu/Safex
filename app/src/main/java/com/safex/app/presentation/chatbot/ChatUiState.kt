package com.safex.app.presentation.chatbot

import com.safex.app.domain.chatbot.model.*

enum class ChatMode {
    TEXT,
    VOICE
}

sealed interface AiState {
    data object Idle : AiState
    data object Thinking : AiState
    data class Error(val retryable: Boolean, val reason: String) : AiState
    data class RateLimited(val minutesLeft: Int) : AiState
    data object Offline : AiState
}

data class ChatUiState(
    val sessionId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val language: ChatLanguage = ChatLanguage.EN,
    val mode: ChatMode = ChatMode.TEXT,
    val aiState: AiState = AiState.Idle,
    val aiEnabled: Boolean = true,
    val ttsEnabled: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val partialTranscript: String = "",
    val suggestedChips: List<FollowUpChip> = emptyList(),
    val activeChecker: Checker? = null,
    val activeCheckerAnswers: Map<String, CheckerAnswer> = emptyMap(),
    val activeCheckerIndex: Int = 0,
    val elderMode: Boolean = false,
    val remainingAiMessages: Int = 20,
    val showConsentDialog: Boolean = false,
    val maskedPreviewText: String? = null,
    val uiStrings: ChatUiStrings? = null
)

sealed interface ChatUiEvent {
    data class ShowSnackbar(val message: String) : ChatUiEvent
    data class DialNumber(val number: String) : ChatUiEvent
    data class OpenUrl(val url: String) : ChatUiEvent
    data class OpenYoutube(val videoId: String) : ChatUiEvent
    data object RequestMicPermission : ChatUiEvent
    data object ShowTtsInstallDialog : ChatUiEvent
    data class CopyToClipboard(val text: String) : ChatUiEvent
    data object ScrollToBottom : ChatUiEvent
    data class ShowMaskedPreview(val text: String) : ChatUiEvent
}

sealed class ChatAction {
    data class SelectTriage(val optionId: String) : ChatAction()
    data class SelectTopic(val topicId: String) : ChatAction()
    data class SendText(val text: String) : ChatAction()
    data object RetryLast : ChatAction()
    data class StartChecker(val checkerId: String) : ChatAction()
    data class AnswerChecker(val questionId: String, val answer: CheckerAnswer) : ChatAction()
    data object SubmitChecker : ChatAction()
    data object PreviousCheckerQuestion : ChatAction()
    data object CloseChecker : ChatAction()
    data class ChangeLanguage(val language: ChatLanguage) : ChatAction()
    data object ToggleTts : ChatAction()
    data class ToggleAi(val enabled: Boolean) : ChatAction()
    data class ConsentAi(val agreed: Boolean) : ChatAction()
    data object DeleteChats : ChatAction()
    data object NewChat : ChatAction()
    data class FeedbackUp(val messageId: String) : ChatAction()
    data class FeedbackDown(val messageId: String) : ChatAction()
    data class TapAction(val action: ScriptAction) : ChatAction()
    data class UpdateInputText(val text: String) : ChatAction()
    data class SwitchMode(val mode: ChatMode) : ChatAction()
    data object DismissMaskedPreview : ChatAction()
}
