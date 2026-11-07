package com.safex.app.presentation.chatbot.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun GuardOrb(
    state: OrbState,
    rmsLevel: Float = 0f,
    caption: String = "",
    size: Dp = 200.dp
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    // Breathing glow for IDLE
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Rotating angle for THINKING
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Speaking amplitude ripple
    val speakingRipple by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speakingRipple"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val baseRadius = this.size.minDimension / 3.2f

                when (state) {
                    OrbState.IDLE -> {
                        // Soft outer aura
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = baseRadius * 1.5f * breathingScale
                            ),
                            radius = baseRadius * 1.5f * breathingScale,
                            center = center
                        )

                        // Core orb
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.9f),
                                    secondaryColor.copy(alpha = 0.7f),
                                    surfaceColor
                                ),
                                center = center,
                                radius = baseRadius * breathingScale
                            ),
                            radius = baseRadius * breathingScale,
                            center = center
                        )
                    }
                    OrbState.LISTENING -> {
                        // Concentric pulsing rings reacting to rmsLevel
                        val dynamicMultiplier = 1f + (rmsLevel * 0.8f)

                        drawCircle(
                            color = primaryColor.copy(alpha = 0.2f * dynamicMultiplier.coerceIn(0.2f, 0.8f)),
                            radius = baseRadius * 1.4f * dynamicMultiplier,
                            center = center,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.35f * dynamicMultiplier.coerceIn(0.3f, 0.9f)),
                            radius = baseRadius * 1.2f * dynamicMultiplier,
                            center = center,
                            style = Stroke(width = 6.dp.toPx())
                        )

                        // Core orb reacting to voice
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor,
                                    secondaryColor.copy(alpha = 0.85f)
                                ),
                                center = center,
                                radius = baseRadius * dynamicMultiplier
                            ),
                            radius = baseRadius * (0.95f + (rmsLevel * 0.25f)),
                            center = center
                        )
                    }
                    OrbState.THINKING -> {
                        // Rotating glowing arcs
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primaryColor,
                                    secondaryColor,
                                    Color.Transparent
                                ),
                                center = center
                            ),
                            startAngle = rotationAngle,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(center.x - (baseRadius * 1.25f), center.y - (baseRadius * 1.25f)),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 2.5f, baseRadius * 2.5f),
                            style = Stroke(width = 6.dp.toPx())
                        )

                        // Inner core pulsing
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryColor,
                                    primaryColor.copy(alpha = 0.6f)
                                ),
                                center = center,
                                radius = baseRadius * 0.9f
                            ),
                            radius = baseRadius * 0.9f,
                            center = center
                        )
                    }
                    OrbState.SPEAKING -> {
                        // Rhythmic speech wave ripples
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.25f),
                            radius = baseRadius * 1.35f * speakingRipple,
                            center = center,
                            style = Stroke(width = 5.dp.toPx())
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    secondaryColor,
                                    primaryColor
                                ),
                                center = center,
                                radius = baseRadius * speakingRipple
                            ),
                            radius = baseRadius * speakingRipple,
                            center = center
                        )
                    }
                }
            }
        }

        if (caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = caption,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
