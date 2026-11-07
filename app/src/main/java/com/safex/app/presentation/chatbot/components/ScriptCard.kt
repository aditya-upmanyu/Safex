package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safex.app.domain.chatbot.model.*
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun ScriptCard(
    topic: SafetyTopic,
    dimens: ChatDimensConfig,
    feedback: FeedbackStatus,
    onActionTap: (ScriptAction) -> Unit,
    onFollowUpTap: (FollowUpChip) -> Unit,
    onFeedbackUp: () -> Unit,
    onFeedbackDown: () -> Unit
) {
    val urgencyContainerColor = when (topic.urgency) {
        Urgency.EMERGENCY -> MaterialTheme.colorScheme.errorContainer
        Urgency.URGENT -> MaterialTheme.colorScheme.tertiaryContainer
        Urgency.NORMAL -> MaterialTheme.colorScheme.secondaryContainer
    }

    val urgencyContentColor = when (topic.urgency) {
        Urgency.EMERGENCY -> MaterialTheme.colorScheme.onErrorContainer
        Urgency.URGENT -> MaterialTheme.colorScheme.onTertiaryContainer
        Urgency.NORMAL -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Topic Title & Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = getTopicIcon(topic.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic.chipLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimens.headlineSize,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = urgencyContainerColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = topic.urgency.name,
                        color = urgencyContentColor,
                        fontSize = dimens.subtextSize,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Empathy Opening
            Text(
                text = "\"${topic.empathyOpening}\"",
                fontStyle = FontStyle.Italic,
                fontSize = dimens.bodySize,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = topic.headline,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimens.bodySize,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Section: Why
            if (topic.why.isNotEmpty()) {
                Text(
                    text = "Why this matters:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                topic.why.forEach { point ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("• ", fontWeight = FontWeight.Bold, fontSize = dimens.bodySize)
                        Text(point, fontSize = dimens.bodySize)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Do Now
            if (topic.doNow.isNotEmpty()) {
                Text(
                    text = "Do This Now:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                topic.doNow.forEachIndexed { index, step ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}. ",
                                fontWeight = FontWeight.Bold,
                                fontSize = dimens.bodySize
                            )
                            Text(
                                text = step.text,
                                fontSize = dimens.bodySize,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (step.action.type != ActionType.NONE) {
                            Spacer(modifier = Modifier.height(4.dp))
                            FilledTonalButton(
                                onClick = { onActionTap(step.action) },
                                modifier = Modifier
                                    .padding(start = 20.dp)
                                    .heightIn(min = dimens.minTouchTarget)
                            ) {
                                val buttonIcon = when (step.action.type) {
                                    ActionType.DIAL -> Icons.Default.Phone
                                    ActionType.OPEN_URL -> Icons.AutoMirrored.Filled.OpenInNew
                                    ActionType.OPEN_CHECKER -> Icons.Default.CheckCircle
                                    ActionType.OPEN_TOPIC -> Icons.AutoMirrored.Filled.ArrowForward
                                    ActionType.COPY -> Icons.Default.ContentCopy
                                    else -> Icons.Default.Info
                                }
                                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                val buttonLabel = when (step.action.type) {
                                    ActionType.DIAL -> "Call ${step.action.value}"
                                    ActionType.OPEN_CHECKER -> "Start Quick Check"
                                    ActionType.OPEN_URL -> "Open Official Link"
                                    ActionType.OPEN_TOPIC -> "View Guide"
                                    ActionType.COPY -> "Copy Text"
                                    else -> "Open"
                                }
                                Text(buttonLabel, fontSize = dimens.bodySize)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Don't
            if (topic.dont.isNotEmpty()) {
                Text(
                    text = "What NOT to do:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                topic.dont.forEach { dontPoint ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text("✗ ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        Text(dontPoint, fontSize = dimens.bodySize)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Section: Official Contacts
            if (topic.contacts.isNotEmpty()) {
                Text(
                    text = "Official Helplines:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    topic.contacts.forEach { contact ->
                        OutlinedButton(
                            onClick = {
                                val action = if (contact.type == ContactType.DIAL) {
                                    ScriptAction(ActionType.DIAL, contact.value)
                                } else {
                                    ScriptAction(ActionType.OPEN_URL, contact.value)
                                }
                                onActionTap(action)
                            },
                            modifier = Modifier.heightIn(min = dimens.minTouchTarget)
                        ) {
                            val icon = if (contact.type == ContactType.DIAL) Icons.Default.Call else Icons.Default.Link
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(contact.label, fontSize = dimens.subtextSize)
                        }
                    }
                }
            }

            // Follow-up Chips
            if (topic.followUpChips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Related questions:",
                    fontSize = dimens.subtextSize,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    topic.followUpChips.forEach { chip ->
                        SuggestionChip(
                            onClick = { onFollowUpTap(chip) },
                            label = { Text(chip.label, fontSize = dimens.subtextSize) }
                        )
                    }
                }
            }

            // Bottom row: Feedback
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Was this helpful?",
                    fontSize = dimens.subtextSize,
                    color = MaterialTheme.colorScheme.outline
                )
                IconButton(onClick = onFeedbackUp) {
                    Icon(
                        imageVector = if (feedback == FeedbackStatus.UP) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Helpful",
                        tint = if (feedback == FeedbackStatus.UP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onFeedbackDown) {
                    Icon(
                        imageVector = if (feedback == FeedbackStatus.DOWN) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = "Not helpful",
                        tint = if (feedback == FeedbackStatus.DOWN) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun getTopicIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName.lowercase()) {
        "email" -> Icons.Default.Email
        "phone" -> Icons.Default.Phone
        "security" -> Icons.Default.Security
        "key" -> Icons.Default.Key
        "alert" -> Icons.Default.Warning
        "qr_code" -> Icons.Default.QrCode
        "shield" -> Icons.Default.Shield
        "lock" -> Icons.Default.Lock
        "account_box" -> Icons.Default.AccountBox
        else -> Icons.AutoMirrored.Filled.HelpOutline
    }
}
