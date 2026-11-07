package com.safex.app.presentation.chatbot

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.safex.app.domain.chatbot.model.*
import com.safex.app.presentation.chatbot.components.*
import com.safex.app.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVoiceMode: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val gson = remember { Gson() }
    val dimens = ChatDimens.get(state.elderMode)

    var showMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }

    // Handle UI Events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChatUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is ChatUiEvent.DialNumber -> {
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${event.number}")
                        }
                        context.startActivity(dialIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open phone dialer", Toast.LENGTH_SHORT).show()
                    }
                }
                is ChatUiEvent.OpenUrl -> {
                    try {
                        val urlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
                        context.startActivity(urlIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open browser link", Toast.LENGTH_SHORT).show()
                    }
                }
                is ChatUiEvent.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("SAFEx Guard", event.text))
                    snackbarHostState.showSnackbar("Copied to clipboard")
                }
                is ChatUiEvent.ScrollToBottom -> {
                    if (state.messages.isNotEmpty()) {
                        listState.animateScrollToItem(state.messages.size - 1)
                    }
                }
                is ChatUiEvent.ShowMaskedPreview -> {
                    // Handled via state dialog
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = CleanSlate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(ShieldBlue, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SurfaceWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "SAFEx Guard",
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = ShieldBlue
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                ),
                actions = {
                    // Voice mode shortcut
                    IconButton(onClick = onNavigateToVoiceMode) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = "Voice Mode")
                    }

                    // Language Selector
                    Box {
                        TextButton(onClick = { showLangMenu = true }) {
                            Text(
                                text = state.language.displayName,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            ChatLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        viewModel.onAction(ChatAction.ChangeLanguage(lang))
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Overflow Menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Conversation") },
                            onClick = {
                                viewModel.onAction(ChatAction.NewChat)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Add, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete This Chat") },
                            onClick = {
                                viewModel.onAction(ChatAction.DeleteChats)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(if (state.aiEnabled) "Disable AI (Offline Only)" else "Enable AI Assistant") },
                            onClick = {
                                viewModel.onAction(ChatAction.ToggleAi(!state.aiEnabled))
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, null) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopicChipsRow(
                    chips = state.suggestedChips,
                    dimens = dimens,
                    onChipSelected = { chip ->
                        chip.topicId?.let { viewModel.onAction(ChatAction.SelectTopic(it)) }
                        chip.askAi?.let { viewModel.onAction(ChatAction.SendText(it)) }
                    }
                )

                DisclaimerBar(
                    disclaimerText = state.uiStrings?.disclaimer ?: "",
                    dimens = dimens
                )

                ChatInputBar(
                    inputText = state.inputText,
                    ttsEnabled = state.ttsEnabled,
                    dimens = dimens,
                    onTextChange = { viewModel.onAction(ChatAction.UpdateInputText(it)) },
                    onSend = { viewModel.onAction(ChatAction.SendText(it)) },
                    onMicTap = onNavigateToVoiceMode,
                    onToggleTts = { viewModel.onAction(ChatAction.ToggleTts) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = state.messages,
                    key = { it.id }
                ) { message ->
                    when (message.contentType) {
                        MessageContentType.TRIAGE -> {
                            val options = remember(message.payloadJson) {
                                try {
                                    val type = object : com.google.gson.reflect.TypeToken<List<TriageOption>>() {}.type
                                    gson.fromJson<List<TriageOption>>(message.payloadJson, type)
                                } catch (_: Exception) {
                                    emptyList()
                                }
                            }
                            TriageCard(
                                prompt = message.text,
                                options = options,
                                dimens = dimens,
                                onOptionSelected = { optionId ->
                                    viewModel.onAction(ChatAction.SelectTriage(optionId))
                                }
                            )
                        }
                        MessageContentType.EMERGENCY -> {
                            EmergencyBanner(
                                title = state.uiStrings?.emergencyTitle ?: "EMERGENCY FRAUD RESPONSE",
                                body = message.text,
                                dimens = dimens,
                                onCall1930 = {
                                    viewModel.onAction(ChatAction.TapAction(ScriptAction(ActionType.DIAL, "1930")))
                                }
                            )
                        }
                        MessageContentType.SCRIPT_CARD -> {
                            val topic = remember(message.payloadJson) {
                                try {
                                    gson.fromJson(message.payloadJson, SafetyTopic::class.java)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (topic != null) {
                                ScriptCard(
                                    topic = topic,
                                    dimens = dimens,
                                    feedback = message.feedback,
                                    onActionTap = { action ->
                                        viewModel.onAction(ChatAction.TapAction(action))
                                    },
                                    onFollowUpTap = { chip ->
                                        chip.topicId?.let { viewModel.onAction(ChatAction.SelectTopic(it)) }
                                        chip.askAi?.let { viewModel.onAction(ChatAction.SendText(it)) }
                                    },
                                    onFeedbackUp = { viewModel.onAction(ChatAction.FeedbackUp(message.id)) },
                                    onFeedbackDown = { viewModel.onAction(ChatAction.FeedbackDown(message.id)) }
                                )
                            } else {
                                MessageBubble(
                                    message = message,
                                    dimens = dimens,
                                    onLongClickCopy = {
                                        viewModel.onAction(ChatAction.TapAction(ScriptAction(ActionType.COPY, it)))
                                    },
                                    onMaskedInfoClick = {}
                                )
                            }
                        }
                        MessageContentType.AI_CARD -> {
                            val reply = remember(message.payloadJson) {
                                try {
                                    gson.fromJson(message.payloadJson, AiReply::class.java)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (reply != null) {
                                AiAnswerCard(
                                    reply = reply,
                                    dimens = dimens,
                                    feedback = message.feedback,
                                    onFollowUpTap = { question ->
                                        viewModel.onAction(ChatAction.SendText(question))
                                    },
                                    onFeedbackUp = { viewModel.onAction(ChatAction.FeedbackUp(message.id)) },
                                    onFeedbackDown = { viewModel.onAction(ChatAction.FeedbackDown(message.id)) },
                                    onCallHelpline = {
                                        viewModel.onAction(ChatAction.TapAction(ScriptAction(ActionType.DIAL, "1930")))
                                    }
                                )
                            } else {
                                MessageBubble(
                                    message = message,
                                    dimens = dimens,
                                    onLongClickCopy = {
                                        viewModel.onAction(ChatAction.TapAction(ScriptAction(ActionType.COPY, it)))
                                    },
                                    onMaskedInfoClick = {}
                                )
                            }
                        }
                        MessageContentType.CHECKER_RESULT -> {
                            val evaluation = remember(message.payloadJson) {
                                try {
                                    gson.fromJson(message.payloadJson, CheckerEvaluation::class.java)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (evaluation != null) {
                                CheckerResultCard(
                                    evaluation = evaluation,
                                    dimens = dimens,
                                    onNextTopicTap = { topicId ->
                                        viewModel.onAction(ChatAction.SelectTopic(topicId))
                                    }
                                )
                            }
                        }
                        MessageContentType.TEXT -> {
                            MessageBubble(
                                message = message,
                                dimens = dimens,
                                onLongClickCopy = {
                                    viewModel.onAction(ChatAction.TapAction(ScriptAction(ActionType.COPY, it)))
                                },
                                onMaskedInfoClick = {
                                    // Open masked preview dialog
                                }
                            )
                        }
                    }
                }

                if (state.aiState is AiState.Thinking) {
                    item {
                        TypingIndicator(
                            thinkingText = state.uiStrings?.thinking ?: "SAFEx Guard is thinking...",
                            dimens = dimens
                        )
                    }
                }
            }
        }
    }

    // Active Checker BottomSheet
    state.activeChecker?.let { checker ->
        CheckerSheet(
            checker = checker,
            currentIndex = state.activeCheckerIndex,
            answers = state.activeCheckerAnswers,
            dimens = dimens,
            onAnswer = { qId, ans ->
                viewModel.onAction(ChatAction.AnswerChecker(qId, ans))
            },
            onPrevious = {
                viewModel.onAction(ChatAction.PreviousCheckerQuestion)
            },
            onClose = {
                viewModel.onAction(ChatAction.CloseChecker)
            },
            onSubmit = {
                viewModel.onAction(ChatAction.SubmitChecker)
            }
        )
    }
}
