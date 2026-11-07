package com.safex.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.safex.app.presentation.theme.*

/**
 * Production-ready Call Guard Live Pulse Indicator
 * - Smooth infinite transition scaling from 1.0f to 1.4f
 * - Fading alpha from 0.4f to 0.0f over 1500ms
 * - Soft circular border avoiding erratic visualizer anxiety for elders
 */
@Composable
fun LiveWaveformPulseIndicator(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isLive: Boolean = true,
    tintColor: Color = TrustEmerald,
    size: Dp = 64.dp
) {
    if (!isLive) {
        Box(
            modifier = modifier
                .size(size)
                .background(tintColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(size * 0.45f)
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(size * 1.5f),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing wave
        Box(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .border(2.dp, tintColor.copy(alpha = pulseAlpha), CircleShape)
        )

        // Core icon circle
        Box(
            modifier = Modifier
                .size(size)
                .background(tintColor.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}
