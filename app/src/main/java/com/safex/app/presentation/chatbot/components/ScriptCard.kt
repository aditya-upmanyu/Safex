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
import androidx.compose.ui.unit.sp
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
    // Use explicit colors for better visibility
    val (urgencyBg, urgencyText, urgencyBorder) = when (topic.urgency) {
        Urgency.EMERGENCY -> Triple(
            androidx.compose.ui.graphics.Color(0xFFEF4444),
            androidx.compose.ui.graphics.Color.White,
            androidx.compose.ui.graphics.Color(0xFFDC2626)
        )
        Urgency.URGENT -> Triple(
            androidx.compose.ui.graphics.Color(0xFFF59E0B),
            androidx.compose.ui.graphics.Color.White,
            androidx.compose.ui.graphics.Color(0xFFD97706)
        )
        Urgency.NORMAL -> Triple(
            androidx.compose.ui.graphics.Color(0xFF3B82F6),
            androidx.compose.ui.graphics.Color.White,
            androidx.compose.ui.graphics.Color(0xFF2563EB)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Topic Title & Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = getTopicIcon(topic.icon),
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = topic.chipLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimens.headlineSize.times(1.1f),
                        color = androidx.compose.ui.graphics.Color(0xFF111827),
                        lineHeight = 24.sp
                    )
                }

                Surface(
                    color = urgencyBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, urgencyBorder)
                ) {
                    Text(
                        text = topic.urgency.name,
                        color = urgencyText,
                        fontSize = maxOf((dimens.subtextSize * 0.9f).value, 11f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Empathy Opening
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = androidx.compose.ui.graphics.Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFBFDBFE))
            ) {
                Text(
                    text = "\"${topic.empathyOpening}\"",
                    fontStyle = FontStyle.Italic,
                    fontSize = dimens.bodySize,
                    color = androidx.compose.ui.graphics.Color(0xFF1E40AF),
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Headline
            Text(
                text = topic.headline,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimens.bodySize,
                color = androidx.compose.ui.graphics.Color(0xFF1F2937),
                lineHeight = 22.sp
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                color = androidx.compose.ui.graphics.Color(0xFFE5E7EB)
            )

            // Section: Why
            if (topic.why.isNotEmpty()) {
                Text(
                    text = "Why this matters:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize,
                    color = androidx.compose.ui.graphics.Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(6.dp))
                topic.why.forEach { point ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            "• ",
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.bodySize,
                            color = androidx.compose.ui.graphics.Color(0xFF3B82F6)
                        )
                        Text(
                            point,
                            fontSize = dimens.bodySize,
                            color = androidx.compose.ui.graphics.Color(0xFF374151),
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section: Do Now
            if (topic.doNow.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFF86EFAC))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "✓ Do This Now:",
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.bodySize.times(1.05f),
                            color = androidx.compose.ui.graphics.Color(0xFF15803D)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        topic.doNow.forEachIndexed { index, step ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "${index + 1}. ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = dimens.bodySize,
                                        color = androidx.compose.ui.graphics.Color(0xFF15803D)
                                    )
                                    Text(
                                        text = step.text,
                                        fontSize = dimens.bodySize,
                                        modifier = Modifier.weight(1f),
                                        color = androidx.compose.ui.graphics.Color(0xFF166534),
                                        lineHeight = 20.sp
                                    )
                                }
                                if (step.action.type != ActionType.NONE) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FilledTonalButton(
                                        onClick = { onActionTap(step.action) },
                                        modifier = Modifier
                                            .padding(start = 20.dp)
                                            .heightIn(min = dimens.minTouchTarget),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = androidx.compose.ui.graphics.Color(0xFF22C55E),
                                            contentColor = androidx.compose.ui.graphics.Color.White
                                        )
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
                                        Text(buttonLabel, fontSize = dimens.bodySize, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section: Don't
            if (topic.dont.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "⚠ What NOT to do:",
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.bodySize.times(1.05f),
                            color = androidx.compose.ui.graphics.Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        topic.dont.forEach { dontPoint ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    "✗ ",
                                    color = androidx.compose.ui.graphics.Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = dimens.bodySize
                                )
                                Text(
                                    dontPoint,
                                    fontSize = dimens.bodySize,
                                    color = androidx.compose.ui.graphics.Color(0xFF991B1B),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Section: Official Contacts
            if (topic.contacts.isNotEmpty()) {
                Text(
                    text = "📞 Official Helplines:",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.bodySize,
                    color = androidx.compose.ui.graphics.Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = dimens.minTouchTarget),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = androidx.compose.ui.graphics.Color(0xFF3B82F6)
                            ),
                            border = BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFF3B82F6))
                        ) {
                            val icon = if (contact.type == ContactType.DIAL) Icons.Default.Call else Icons.Default.Link
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                contact.label,
                                fontSize = dimens.bodySize,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Follow-up Chips
            if (topic.followUpChips.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFE5E7EB)
                )
                Text(
                    text = "Related questions:",
                    fontSize = dimens.subtextSize,
                    color = androidx.compose.ui.graphics.Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
                ) {
                    topic.followUpChips.take(2).forEach { chip ->
                        SuggestionChip(
                            onClick = { onFollowUpTap(chip) },
                            label = {
                                Text(
                                    chip.label,
                                    fontSize = dimens.subtextSize,
                                    color = androidx.compose.ui.graphics.Color(0xFF374151)
                                )
                            },
                            border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFD1D5DB))
                        )
                    }
                }
            }

            // Bottom row: Feedback
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = androidx.compose.ui.graphics.Color(0xFFE5E7EB)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Was this helpful?",
                    fontSize = dimens.subtextSize,
                    color = androidx.compose.ui.graphics.Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onFeedbackUp) {
                    Icon(
                        imageVector = if (feedback == FeedbackStatus.UP) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Helpful",
                        tint = if (feedback == FeedbackStatus.UP)
                            androidx.compose.ui.graphics.Color(0xFF22C55E)
                        else
                            androidx.compose.ui.graphics.Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onFeedbackDown) {
                    Icon(
                        imageVector = if (feedback == FeedbackStatus.DOWN) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = "Not helpful",
                        tint = if (feedback == FeedbackStatus.DOWN)
                            androidx.compose.ui.graphics.Color(0xFFEF4444)
                        else
                            androidx.compose.ui.graphics.Color(0xFF9CA3AF),
                        modifier = Modifier.size(20.dp)
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
