package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun ChatInputBar(
    inputText: String,
    ttsEnabled: Boolean,
    dimens: ChatDimensConfig,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onMicTap: () -> Unit,
    onToggleTts: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Speaker toggle button
            IconButton(
                onClick = onToggleTts,
                modifier = Modifier
                    .size(dimens.minTouchTarget)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = if (ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = if (ttsEnabled) "Speaker Enabled" else "Speaker Disabled",
                    tint = if (ttsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            // Input TextField
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Ask a question or describe an issue...",
                        fontSize = dimens.bodySize
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSend(inputText)
                        }
                    }
                )
            )

            // Mic or Send button
            if (inputText.isBlank()) {
                IconButton(
                    onClick = onMicTap,
                    modifier = Modifier
                        .size(dimens.minTouchTarget)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak into mic",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                IconButton(
                    onClick = { onSend(inputText) },
                    modifier = Modifier
                        .size(dimens.minTouchTarget)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
