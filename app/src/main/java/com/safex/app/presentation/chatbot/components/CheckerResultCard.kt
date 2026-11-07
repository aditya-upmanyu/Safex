package com.safex.app.presentation.chatbot.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safex.app.domain.chatbot.model.CheckerEvaluation
import com.safex.app.domain.chatbot.model.Verdict
import com.safex.app.presentation.chatbot.ChatDimensConfig

@Composable
fun CheckerResultCard(
    evaluation: CheckerEvaluation,
    dimens: ChatDimensConfig,
    onNextTopicTap: (String) -> Unit
) {
    val (verdictColor, verdictIcon, verdictLabel) = when (evaluation.verdict) {
        Verdict.LOW_RISK -> Triple(Color(0xFF2E7D32), Icons.Default.CheckCircle, "LOW RISK")
        Verdict.SUSPICIOUS -> Triple(Color(0xFFF57C00), Icons.Default.Warning, "SUSPICIOUS")
        Verdict.LIKELY_SCAM -> Triple(Color(0xFFD32F2F), Icons.Default.Dangerous, "LIKELY SCAM")
        Verdict.INFO -> Triple(Color(0xFF1976D2), Icons.Default.CheckCircle, "INFO")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(dimens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, verdictColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Checker Evaluation",
                    fontWeight = FontWeight.Bold,
                    fontSize = dimens.headlineSize
                )

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
                            contentDescription = verdictLabel,
                            tint = verdictColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = verdictLabel,
                            color = verdictColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = dimens.subtextSize
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = evaluation.resultText.headline,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimens.headlineSize,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = evaluation.resultText.body,
                fontSize = dimens.bodySize,
                lineHeight = dimens.bodySize.times(1.35f),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!evaluation.resultText.nextTopic.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { onNextTopicTap(evaluation.resultText.nextTopic) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimens.minTouchTarget)
                ) {
                    Text("View Recommended Guide", fontSize = dimens.bodySize)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
