package com.safex.app.presentation.chatbot.voice

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import com.safex.app.domain.chatbot.model.*
import com.safex.app.presentation.chatbot.ChatAction
import com.safex.app.presentation.chatbot.ChatDimens
import com.safex.app.presentation.chatbot.ChatViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceModeScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    speechInputController: SpeechInputController,
    ttsController: TtsController
) {
    val state by viewModel.uiState.collectAsState()
    val speechState by speechInputController.speechState.collectAsState()
    val partialTranscript by speechInputController.partialTranscript.collectAsState()
    val isSpeaking by ttsController.isSpeaking.collectAsState()
    val context = LocalContext.current
    val dimens = ChatDimens.get(state.elderMode)

    var hasMicPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            speechInputController.startListening(state.language)
        } else {
            Toast.makeText(context, "Microphone permission is needed for Voice Mode", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechInputController.stopListening()
            ttsController.stop()
        }
    }

    // Determine current Orb state
    val orbState = when {
        isSpeaking -> OrbState.SPEAKING
        speechState is SpeechState.Listening -> OrbState.LISTENING
        speechState is SpeechState.Processing -> OrbState.THINKING
        else -> OrbState.IDLE
    }

    val currentRms = when (val s = speechState) {
        is SpeechState.Listening -> s.rmsLevel
        else -> 0f
    }

    // Caption for the orb
    val orbCaption = when (orbState) {
        OrbState.LISTENING -> state.uiStrings?.listening ?: "Listening..."
        OrbState.THINKING -> state.uiStrings?.thinking ?: "Thinking..."
        OrbState.SPEAKING -> state.uiStrings?.speaking ?: "Speaking..."
        OrbState.IDLE -> state.uiStrings?.tapToTalk ?: "Tap the mic to talk"
    }

    // Auto-send recognized text through viewModel pipeline
    LaunchedEffect(speechState) {
        if (speechState is SpeechState.Result) {
            val recognizedText = (speechState as SpeechState.Result).text
            if (recognizedText.isNotBlank()) {
                delay(600)
                viewModel.onAction(ChatAction.SendText(recognizedText))
                speechInputController.stopListening()
            }
        }
    }

    // Read aloud latest assistant response if TTS is active
    val lastAssistantMessage = remember(state.messages) {
        state.messages.lastOrNull { it.role == MessageRole.ASSISTANT }
    }

    LaunchedEffect(lastAssistantMessage?.id) {
        val msg = lastAssistantMessage ?: return@LaunchedEffect
        val gson = Gson()
        when (msg.contentType) {
            MessageContentType.SCRIPT_CARD -> {
                val topic = try { gson.fromJson(msg.payloadJson, SafetyTopic::class.java) } catch (_: Exception) { null }
                if (topic != null) {
                    ttsController.speak(topic.headline, topic.doNow.map { it.text }, state.language)
                }
            }
            MessageContentType.AI_CARD -> {
                val reply = try { gson.fromJson(msg.payloadJson, AiReply::class.java) } catch (_: Exception) { null }
                if (reply != null) {
                    ttsController.speak(reply.headline, reply.doNow, state.language)
                }
            }
            MessageContentType.EMERGENCY -> {
                ttsController.speak(msg.text, listOf("Call national helpline 1 9 3 0 right now."), state.language)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SAFEx Voice Guard") },
                navigationIcon = {
                    IconButton(onClick = {
                        speechInputController.stopListening()
                        ttsController.stop()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val nextLang = when (state.language) {
                            ChatLanguage.EN -> ChatLanguage.HI
                            ChatLanguage.HI -> ChatLanguage.HINGLISH
                            ChatLanguage.HINGLISH -> ChatLanguage.EN
                        }
                        viewModel.onAction(ChatAction.ChangeLanguage(nextLang))
                    }) {
                        Text(state.language.displayName, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Last message headline or transcription
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (partialTranscript.isNotBlank()) {
                    Text(
                        text = "\"$partialTranscript\"",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                } else if (lastAssistantMessage != null) {
                    Text(
                        text = lastAssistantMessage.text,
                        fontSize = dimens.headlineSize,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Animated Guard Orb
            GuardOrb(
                state = orbState,
                rmsLevel = currentRms,
                caption = orbCaption,
                size = dimens.orbSize
            )

            // Bottom controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big round Mic button
                    val isListening = orbState == OrbState.LISTENING
                    IconButton(
                        onClick = {
                            if (isListening) {
                                speechInputController.stopListening()
                            } else {
                                ttsController.stop()
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .background(
                                color = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "Stop listening" else "Start talking",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(20.dp))
                        OutlinedButton(
                            onClick = { ttsController.stop() },
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mute")
                        }
                    }
                }

                // Switch back to typing
                FilledTonalButton(
                    onClick = {
                        speechInputController.stopListening()
                        ttsController.stop()
                        onNavigateBack()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch to Typing Chat")
                }
            }
        }
    }
}
