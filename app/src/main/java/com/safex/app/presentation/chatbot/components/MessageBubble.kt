package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.domain.chatbot.model.ChatMessage
import com.safex.app.domain.chatbot.model.MessageRole
import com.safex.app.presentation.chatbot.ChatDimensConfig
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    dimens: ChatDimensConfig,
    onLongClickCopy: (String) -> Unit,
    onMaskedInfoClick: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    val timeString = rememberFormattedTime(message.createdAt)
    val hasMaskedTokens = message.text.contains("[") && message.text.contains("]")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = bubbleShape,
            tonalElevation = 2.dp,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { onLongClickCopy(message.text) }
                )
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = dimens.bubblePaddingHorizontal,
                    vertical = dimens.bubblePaddingVertical
                )
            ) {
                Text(
                    text = message.text,
                    fontSize = dimens.bodySize,
                    lineHeight = dimens.bodySize.times(1.35f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeString,
                        fontSize = dimens.subtextSize,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (isUser && hasMaskedTokens) {
            Spacer(modifier = Modifier.height(2.dp))
            SuggestionChip(
                onClick = { onMaskedInfoClick(message.text) },
                label = { Text("Sensitive data hidden", fontSize = 11.sp) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Data Masked",
                        modifier = Modifier.size(12.dp)
                    )
                },
                modifier = Modifier.height(26.dp)
            )
        }
    }
}

@Composable
private fun rememberFormattedTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
