package com.safex.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.presentation.theme.*

@Composable
fun AlertBanner(
    message: String,
    severity: String = "HIGH",
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (severity.uppercase()) {
        "CRITICAL", "HIGH", "DANGER" -> Triple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson)
        "MEDIUM", "WARNING" -> Triple(CautionAmberContainer, CautionAmberBorder, CautionAmber)
        else -> Triple(ShieldBlueContainer, BorderSubtle, ShieldBlue)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = textColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = CharcoalBlack,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
