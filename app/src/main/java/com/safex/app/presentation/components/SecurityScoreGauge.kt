package com.safex.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.presentation.theme.*

/**
 * Production-ready SAFEx Security Gauge
 * - Continuous arc sweep using a FastOutSlowInEasing curve over 800ms
 * - Dynamic color transitions: AlertCrimson -> CautionAmber -> TrustEmerald
 * - High contrast large metric display (36sp bold Charcoal Black, letterSpacing = 0.5sp)
 * - Off-white subtle track background
 */
@Composable
fun SecurityScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    statusSubtitle: String? = null
) {
    var animationPlayed by remember { mutableStateOf(false) }

    // FastOutSlowInEasing curve over 800ms
    val animatedScore by animateFloatAsState(
        targetValue = if (animationPlayed) score.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "security_score_gauge"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    // Dynamic semantic safety colors
    val targetColor = when {
        animatedScore >= 80 -> TrustEmerald
        animatedScore >= 50 -> CautionAmber
        else -> AlertCrimson
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "security_score_color"
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 14.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Background subtle track
            drawArc(
                color = BorderSubtle,
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )

            // Dynamic score arc
            drawArc(
                color = animatedColor,
                startAngle = 140f,
                sweepAngle = (animatedScore.coerceIn(0f, 100f) / 100f) * 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = topLeft
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Security Metric: 36sp, Bold, Charcoal Black, letterSpacing = 0.5sp
            Text(
                text = "${animatedScore.toInt()}",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "/ 100",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MutedSlate
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Semantic Status Pill
            Box(
                modifier = Modifier
                    .background(
                        color = animatedColor.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusSubtitle ?: when {
                        score >= 80 -> "PROTECTED"
                        score >= 50 -> "ATTENTION"
                        else -> "RISK DETECTED"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
            }
        }
    }
}
