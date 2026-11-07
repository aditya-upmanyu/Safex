package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.domain.chatbot.model.AiReply
import com.safex.app.domain.chatbot.model.FeedbackStatus
import com.safex.app.domain.chatbot.model.Verdict
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun AiAnswerCard(
    reply: AiReply,
    dimens: ChatDimensConfig,
    feedback: FeedbackStatus,
    onFollowUpTap: (String) -> Unit,
    onFeedbackUp: () -> Unit,
    onFeedbackDown: () -> Unit,
    onCallHelpline: () -> Unit
) {
    val (verdictColor, verdictIcon, verdictText) = when (reply.verdict) {
        Verdict.LOW_RISK -> Triple(Color(0xFF2E7D32), Icons.Default.CheckCircle, "LOW RISK")
        Verdict.SUSPICIOUS -> Triple(Color(0xFFF57C00), Icons.Default.Warning, "SUSPICIOUS")
        Verdict.LIKELY_SCAM -> Triple(Color(0xFFD32F2F), Icons.Default.Dangerous, "LIKELY SCAM")
        Verdict.INFO -> Triple(Color(0xFF1976D2), Icons.Default.Info, "INFORMATION")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, verdictColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // AI indicator header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AI-Generated Advice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Verdict Chip with icon AND text (Never color alone)
                Surface(
                    color = verdictColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, verdictColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = verdictIcon,
                            contentDescription = verdictText,
                            tint = verdictColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = verdictText,
                            color = verdictColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.subtextSize
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = reply.headline,
                fontWeight = FontWeight.Bold,
                fontSize = dimens.headlineSize,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Why section
            if (reply.why.isNotEmpty()) {
                Text(
                    text = "Key Observations:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimens.bodySize
                )
                Spacer(modifier = Modifier.height(4.dp))
                reply.why.forEach { item ->
                    Text(
                        text = "• $item",
                        fontSize = dimens.bodySize,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Do Now steps
            if (reply.doNow.isNotEmpty()) {
                Text(
                    text = "Recommended Steps:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimens.bodySize,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                reply.doNow.forEachIndexed { idx, step ->
                    Text(
                        text = "${idx + 1}. $step",
                        fontSize = dimens.bodySize,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Don't points
            if (reply.dont.isNotEmpty()) {
                Text(
                    text = "Do Not:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimens.bodySize,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                reply.dont.forEach { item ->
                    Text(
                        text = "✗ $item",
                        fontSize = dimens.bodySize,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Report To
            if (reply.reportTo.isNotEmpty()) {
                Text(
                    text = "Official Action:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = dimens.subtextSize
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCallHelpline,
                        modifier = Modifier.heightIn(min = dimens.minTouchTarget)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call 1930 Helpline", fontSize = dimens.subtextSize)
                    }
                }
            }

            // Follow-up suggestions
            if (reply.followUpQuestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Ask more:",
                    fontSize = dimens.subtextSize,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                reply.followUpQuestions.forEach { question ->
                    AssistChip(
                        onClick = { onFollowUpTap(question) },
                        label = { Text(question, fontSize = dimens.subtextSize) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Bottom Feedback
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
