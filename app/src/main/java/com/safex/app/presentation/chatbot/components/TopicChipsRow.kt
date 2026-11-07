package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safex.app.domain.chatbot.model.FollowUpChip
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun TopicChipsRow(
    chips: List<FollowUpChip>,
    dimens: ChatDimensConfig,
    onChipSelected: (FollowUpChip) -> Unit
) {
    if (chips.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            SuggestionChip(
                onClick = { onChipSelected(chip) },
                label = { Text(chip.label, fontSize = dimens.subtextSize) }
            )
        }
    }
}
