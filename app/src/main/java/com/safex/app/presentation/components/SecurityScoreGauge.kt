package com.safex.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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
import com.safex.app.presentation.theme.Green
import com.safex.app.presentation.theme.Red
import com.safex.app.presentation.theme.Amber

@Composable
fun SecurityScoreGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val currentScore = animateFloatAsState(
        targetValue = if (animationPlayed) score.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "score"
    )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val color = when {
        score >= 80 -> Green
        score >= 50 -> Amber
        else -> Red
    }

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            // Background arc
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset.Zero
            )

            // Score arc
            drawArc(
                color = color,
                startAngle = 140f,
                sweepAngle = (currentScore.value / 100f) * 260f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height),
                topLeft = Offset.Zero
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentScore.value.toInt()}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "Security Score",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
