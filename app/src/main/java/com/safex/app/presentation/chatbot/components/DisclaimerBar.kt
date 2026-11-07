package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun DisclaimerBar(
    disclaimerText: String,
    dimens: ChatDimensConfig
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = disclaimerText.ifBlank {
                "SAFEx Guard provides safety guidance, not legal or financial advice. We cannot live-verify websites or phone numbers."
            },
            fontSize = dimens.subtextSize,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            lineHeight = dimens.subtextSize.times(1.3f)
        )
    }
}
