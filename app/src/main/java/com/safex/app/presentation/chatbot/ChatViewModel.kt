package com.safex.app.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.safex.app.data.datastore.SettingsDataStore
import com.safex.app.domain.chatbot.model.*
import com.safex.app.domain.chatbot.repository.AiGuardRepository
import com.safex.app.domain.chatbot.repository.AiRequest
import com.safex.app.domain.chatbot.repository.AiResult
import com.safex.app.domain.chatbot.repository.ChatRepository
import com.safex.app.domain.chatbot.repository.ScriptRepository
import com.safex.app.domain.chatbot.usecase.DetectEmergencyUseCase
import com.safex.app.domain.chatbot.usecase.MaskSensitiveDataUseCase
import com.safex.app.domain.chatbot.usecase.MatchIntentUseCase
import com.safex.app.domain.chatbot.usecase.RunCheckerUseCase
import com.safex.app.domain.chatbot.usecase.SendToAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val scriptRepository: ScriptRepository,
    private val matchIntentUseCase: MatchIntentUseCase,
    private val detectEmergencyUseCase: DetectEmergencyUseCase,
    private val runCheckerUseCase: RunCheckerUseCase,
    private val sendToAiUseCase: SendToAiUseCase,
    private val maskSensitiveDataUseCase: MaskSensitiveDataUseCase,
    private val settingsDataStore: SettingsDataStore,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatUiEvent>()
    val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

    init {
        initSessionAndPreferences()
    }

    private fun initSessionAndPreferences() {
        viewModelScope.launch {
            // Observe settings
            combine(
                settingsDataStore.chatLanguage,
                settingsDataStore.chatTtsEnabled,
                settingsDataStore.chatAiEnabled,
                settingsDataStore.chatElderMode
            ) { langCode, tts, ai, elder ->
                val lang = ChatLanguage.fromCode(langCode)
                _uiState.update { it.copy(language = lang, ttsEnabled = tts, aiEnabled = ai, elderMode = elder) }
                lang
            }.collectLatest { lang ->
                loadScriptsForLanguage(lang)
            }
        }

        startOrLoadSession()
    }

    private fun startOrLoadSession() {
        val newSessionId = UUID.randomUUID().toString()
        _uiState.update { it.copy(sessionId = newSessionId) }

        viewModelScope.launch {
            chatRepository.getMessages(newSessionId).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
                if (msgs.isEmpty()) {
                    insertInitialTriageMessage(newSessionId)
                }
            }
        }
    }

    private suspend fun loadScriptsForLanguage(language: ChatLanguage) {
        val bundle = scriptRepository.getScriptBundle(language)
        _uiState.update { it.copy(uiStrings = bundle.ui) }
    }

    private suspend fun insertInitialTriageMessage(sessionId: String) {
        val lang = _uiState.value.language
        val bundle = scriptRepository.getScriptBundle(lang)
        val welcomeMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = MessageRole.ASSISTANT,
            contentType = MessageContentType.TRIAGE,
            text = bundle.ui.welcome,
            payloadJson = gson.toJson(bundle.ui.triageOptions),
            scriptId = null,
            language = lang
        )
        chatRepository.insertMessage(welcomeMsg)
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.UpdateInputText -> {
                _uiState.update { it.copy(inputText = action.text) }
            }
            is ChatAction.SelectTriage -> handleTriageSelection(action.optionId)
            is ChatAction.SelectTopic -> handleSelectTopic(action.topicId)
            is ChatAction.SendText -> handleSendText(action.text)
            is ChatAction.RetryLast -> handleRetryLast()
            is ChatAction.StartChecker -> handleStartChecker(action.checkerId)
            is ChatAction.AnswerChecker -> handleAnswerChecker(action.questionId, action.answer)
            is ChatAction.PreviousCheckerQuestion -> handlePreviousCheckerQuestion()
            is ChatAction.CloseChecker -> _uiState.update { it.copy(activeChecker = null) }
            is ChatAction.SubmitChecker -> handleSubmitChecker()
            is ChatAction.ChangeLanguage -> handleChangeLanguage(action.language)
            is ChatAction.ToggleTts -> handleToggleTts()
            is ChatAction.ToggleAi -> handleToggleAi(action.enabled)
            is ChatAction.ConsentAi -> handleConsentAi(action.agreed)
            is ChatAction.DeleteChats -> handleDeleteChats()
            is ChatAction.NewChat -> handleNewChat()
            is ChatAction.FeedbackUp -> updateFeedback(action.messageId, FeedbackStatus.UP)
            is ChatAction.FeedbackDown -> updateFeedback(action.messageId, FeedbackStatus.DOWN)
            is ChatAction.TapAction -> handleTapAction(action.action)
            is ChatAction.SwitchMode -> _uiState.update { it.copy(mode = action.mode) }
            is ChatAction.DismissMaskedPreview -> _uiState.update { it.copy(maskedPreviewText = null) }
        }
    }

    private fun handleTriageSelection(optionId: String) {
        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            val lang = _uiState.value.language
            val bundle = scriptRepository.getScriptBundle(lang)

            when (optionId) {
                "HAPPENED" -> {
                    val userMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.USER,
                        contentType = MessageContentType.TEXT,
                        text = bundle.ui.triageOptions.firstOrNull { it.id == "HAPPENED" }?.label ?: "Fraud Happened",
                        language = lang
                    )
                    chatRepository.insertMessage(userMsg)

                    val promptMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.TRIAGE,
                        text = bundle.ui.happenedPrompt,
                        payloadJson = gson.toJson(bundle.ui.happenedOptions),
                        language = lang
                    )
                    chatRepository.insertMessage(promptMsg)
                }
                "SUSPECT" -> {
                    val userMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.USER,
                        contentType = MessageContentType.TEXT,
                        text = bundle.ui.triageOptions.firstOrNull { it.id == "SUSPECT" }?.label ?: "I suspect fraud",
                        language = lang
                    )
                    chatRepository.insertMessage(userMsg)

                    val suspectTopicChips = listOf(
                        FollowUpChip(label = "Email verification", topicId = "email_check"),
                        FollowUpChip(label = "Suspicious call", topicId = "call_received"),
                        FollowUpChip(label = "OTP / PIN asked", topicId = "otp_pin_asked"),
                        FollowUpChip(label = "Link or QR check", topicId = "link_qr_check"),
                        FollowUpChip(label = "Digital arrest claim", topicId = "digital_arrest")
                    )
                    _uiState.update { it.copy(suggestedChips = suspectTopicChips) }

                    val assistantMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.TEXT,
                        text = "I am here to protect you. Choose a situation below or type your question:",
                        language = lang
                    )
                    chatRepository.insertMessage(assistantMsg)
                }
                "LEARN" -> {
                    val userMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.USER,
                        contentType = MessageContentType.TEXT,
                        text = bundle.ui.triageOptions.firstOrNull { it.id == "LEARN" }?.label ?: "Learn safety",
                        language = lang
                    )
                    chatRepository.insertMessage(userMsg)

                    val learnChips = listOf(
                        FollowUpChip(label = "How to setup 2FA", topicId = "setup_2fa"),
                        FollowUpChip(label = "Link & QR safety", topicId = "link_qr_check"),
                        FollowUpChip(label = "Identify phishing mail", topicId = "email_check")
                    )
                    _uiState.update { it.copy(suggestedChips = learnChips) }

                    val assistantMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.TEXT,
                        text = bundle.ui.learnPrompt,
                        language = lang
                    )
                    chatRepository.insertMessage(assistantMsg)
                }
                else -> {
                    // Specific topic tapped from happened_options
                    handleSelectTopic(optionId)
                }
            }
        }
    }

    private fun handleSelectTopic(topicId: String) {
        viewModelScope.launch {
            val sessionId = _uiState.value.sessionId
            val lang = _uiState.value.language
            val topic = scriptRepository.getTopic(topicId, lang) ?: return@launch

            // User bubble
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = MessageRole.USER,
                contentType = MessageContentType.TEXT,
                text = topic.chipLabel,
                language = lang
            )
            chatRepository.insertMessage(userMsg)

            // If emergency topic, insert Emergency banner
            if (topic.urgency == Urgency.EMERGENCY) {
                val emergencyMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    contentType = MessageContentType.EMERGENCY,
                    text = topic.headline,
                    scriptId = topic.id,
                    language = lang
                )
                chatRepository.insertMessage(emergencyMsg)
            }

            // Assistant Script card
            val scriptMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = MessageRole.ASSISTANT,
                contentType = MessageContentType.SCRIPT_CARD,
                text = topic.headline,
                payloadJson = gson.toJson(topic),
                scriptId = topic.id,
                language = lang
            )
            chatRepository.insertMessage(scriptMsg)

            _uiState.update { it.copy(suggestedChips = topic.followUpChips) }
            _events.emit(ChatUiEvent.ScrollToBottom)
        }
    }

    private fun handleSendText(text: String) {
        if (text.isBlank()) return
        val sessionId = _uiState.value.sessionId
        val lang = _uiState.value.language

        viewModelScope.launch {
            _uiState.update { it.copy(inputText = "") }

            // Mask sensitive data locally on device
            val maskResult = maskSensitiveDataUseCase(text)
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = MessageRole.USER,
                contentType = MessageContentType.TEXT,
                text = maskResult.maskedText,
                payloadJson = text, // original stored locally only for preview
                language = lang
            )
            chatRepository.insertMessage(userMsg)

            // 1. Detect Emergency immediately
            val emergency = detectEmergencyUseCase(text)
            if (emergency.isEmergency && emergency.topicId != null) {
                val topic = scriptRepository.getTopic(emergency.topicId, lang)
                if (topic != null) {
                    val emergencyBannerMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.EMERGENCY,
                        text = topic.headline,
                        scriptId = topic.id,
                        language = lang
                    )
                    chatRepository.insertMessage(emergencyBannerMsg)

                    val scriptMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.SCRIPT_CARD,
                        text = topic.headline,
                        payloadJson = gson.toJson(topic),
                        scriptId = topic.id,
                        language = lang
                    )
                    chatRepository.insertMessage(scriptMsg)
                    _uiState.update { it.copy(suggestedChips = topic.followUpChips) }
                    _events.emit(ChatUiEvent.ScrollToBottom)
                    return@launch
                }
            }

            // 2. Match Intent offline
            val match = matchIntentUseCase(text)
            if (match.bestTopicId != null) {
                val topic = scriptRepository.getTopic(match.bestTopicId, lang)
                if (topic != null) {
                    val scriptMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.SCRIPT_CARD,
                        text = topic.headline,
                        payloadJson = gson.toJson(topic),
                        scriptId = topic.id,
                        language = lang
                    )
                    chatRepository.insertMessage(scriptMsg)

                    val alternativeChips = match.alternatives.mapNotNull { altId ->
                        scriptRepository.getTopic(altId, lang)?.let {
                            FollowUpChip(label = it.chipLabel, topicId = it.id)
                        }
                    }
                    _uiState.update { it.copy(suggestedChips = alternativeChips + topic.followUpChips) }
                    _events.emit(ChatUiEvent.ScrollToBottom)
                    return@launch
                }
            }

            // 3. AI Pathway
            callAiGuard(text)
        }
    }

    private suspend fun callAiGuard(userText: String) {
        val sessionId = _uiState.value.sessionId
        val lang = _uiState.value.language

        if (!_uiState.value.aiEnabled) {
            val offlineMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = MessageRole.ASSISTANT,
                contentType = MessageContentType.TEXT,
                text = _uiState.value.uiStrings?.aiUnavailable ?: "AI assistant is disabled. Here are safety topics you can check:",
                language = lang
            )
            chatRepository.insertMessage(offlineMsg)
            return
        }

        _uiState.update { it.copy(aiState = AiState.Thinking) }

        val history = _uiState.value.messages.takeLast(6).map { it.role.name to it.text }
        when (val result = sendToAiUseCase(userText, lang, history)) {
            is AiResult.Success -> {
                _uiState.update { it.copy(aiState = AiState.Idle) }

                if (result.reply.verdict == Verdict.LIKELY_SCAM) {
                    val emergencyMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        role = MessageRole.ASSISTANT,
                        contentType = MessageContentType.EMERGENCY,
                        text = result.reply.headline,
                        language = lang
                    )
                    chatRepository.insertMessage(emergencyMsg)
                }

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    contentType = MessageContentType.AI_CARD,
                    text = result.reply.headline,
                    payloadJson = gson.toJson(result.reply),
                    language = lang
                )
                chatRepository.insertMessage(aiMsg)

                val followUps = result.reply.followUpQuestions.map {
                    FollowUpChip(label = it, askAi = it)
                }
                _uiState.update { it.copy(suggestedChips = followUps) }
                _events.emit(ChatUiEvent.ScrollToBottom)
            }
            is AiResult.QuotaExceeded -> {
                _uiState.update { it.copy(aiState = AiState.RateLimited(result.retryAfterMinutes)) }
                val limitMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    contentType = MessageContentType.TEXT,
                    text = _uiState.value.uiStrings?.rateLimited ?: "Rate limit reached. Please wait a few minutes.",
                    language = lang
                )
                chatRepository.insertMessage(limitMsg)
            }
            else -> {
                _uiState.update { it.copy(aiState = AiState.Error(true, "AI service unavailable")) }
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    contentType = MessageContentType.TEXT,
                    text = _uiState.value.uiStrings?.aiUnavailable ?: "AI service is offline. Please refer to our offline guides below.",
                    language = lang
                )
                chatRepository.insertMessage(errorMsg)
            }
        }
    }

    private fun handleRetryLast() {
        val lastUserMsg = _uiState.value.messages.lastOrNull { it.role == MessageRole.USER }
        if (lastUserMsg != null) {
            handleSendText(lastUserMsg.text)
        }
    }

    private fun handleStartChecker(checkerId: String) {
        viewModelScope.launch {
            val lang = _uiState.value.language
            val checker = scriptRepository.getChecker(checkerId, lang) ?: return@launch
            _uiState.update {
                it.copy(
                    activeChecker = checker,
                    activeCheckerIndex = 0,
                    activeCheckerAnswers = emptyMap()
                )
            }
        }
    }

    private fun handleAnswerChecker(questionId: String, answer: CheckerAnswer) {
        val currentAnswers = _uiState.value.activeCheckerAnswers.toMutableMap()
        currentAnswers[questionId] = answer
        val nextIdx = _uiState.value.activeCheckerIndex + 1
        _uiState.update {
            it.copy(
                activeCheckerAnswers = currentAnswers,
                activeCheckerIndex = nextIdx.coerceAtMost((it.activeChecker?.questions?.size ?: 1) - 1)
            )
        }
    }

    private fun handlePreviousCheckerQuestion() {
        val prevIdx = (_uiState.value.activeCheckerIndex - 1).coerceAtLeast(0)
        _uiState.update { it.copy(activeCheckerIndex = prevIdx) }
    }

    private fun handleSubmitChecker() {
        val checker = _uiState.value.activeChecker ?: return
        val answers = _uiState.value.activeCheckerAnswers
        val evaluation = runCheckerUseCase(checker, answers)
        val sessionId = _uiState.value.sessionId
        val lang = _uiState.value.language

        viewModelScope.launch {
            _uiState.update { it.copy(activeChecker = null) }

            val evalMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = MessageRole.ASSISTANT,
                contentType = MessageContentType.CHECKER_RESULT,
                text = evaluation.resultText.headline,
                payloadJson = gson.toJson(evaluation),
                scriptId = checker.id,
                language = lang
            )
            chatRepository.insertMessage(evalMsg)
            _events.emit(ChatUiEvent.ScrollToBottom)
        }
    }

    private fun handleChangeLanguage(language: ChatLanguage) {
        viewModelScope.launch {
            settingsDataStore.setChatLanguage(language.code)
            _uiState.update { it.copy(language = language) }
            loadScriptsForLanguage(language)
        }
    }

    private fun handleToggleTts() {
        viewModelScope.launch {
            val newState = !_uiState.value.ttsEnabled
            settingsDataStore.setChatTtsEnabled(newState)
            _uiState.update { it.copy(ttsEnabled = newState) }
        }
    }

    private fun handleToggleAi(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setChatAiEnabled(enabled)
            _uiState.update { it.copy(aiEnabled = enabled) }
        }
    }

    private fun handleConsentAi(agreed: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setChatAiConsent(agreed)
            _uiState.update { it.copy(showConsentDialog = false, aiEnabled = agreed) }
        }
    }

    private fun handleDeleteChats() {
        viewModelScope.launch {
            chatRepository.deleteSession(_uiState.value.sessionId)
            startOrLoadSession()
            _events.emit(ChatUiEvent.ShowSnackbar("Chat history deleted"))
        }
    }

    private fun handleNewChat() {
        startOrLoadSession()
    }

    private fun updateFeedback(messageId: String, feedback: FeedbackStatus) {
        viewModelScope.launch {
            val msg = _uiState.value.messages.firstOrNull { it.id == messageId } ?: return@launch
            chatRepository.updateMessage(msg.copy(feedback = feedback))
        }
    }

    private fun handleTapAction(action: ScriptAction) {
        viewModelScope.launch {
            when (action.type) {
                ActionType.DIAL -> _events.emit(ChatUiEvent.DialNumber(action.value))
                ActionType.OPEN_URL -> _events.emit(ChatUiEvent.OpenUrl(action.value))
                ActionType.OPEN_CHECKER -> handleStartChecker(action.value)
                ActionType.OPEN_TOPIC -> handleSelectTopic(action.value)
                ActionType.COPY -> _events.emit(ChatUiEvent.CopyToClipboard(action.value))
                ActionType.NONE -> {}
            }
        }
    }
}
