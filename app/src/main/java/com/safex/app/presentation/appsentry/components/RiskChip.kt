package com.safex.app.presentation.appsentry.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.presentation.theme.*

@Composable
fun RiskChip(
    level: AppRiskLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (level) {
        AppRiskLevel.SAFE -> Triple(TrustEmeraldContainer, TrustEmeraldBorder, TrustEmerald)
        AppRiskLevel.LOW -> Triple(TrustEmeraldContainer, TrustEmeraldBorder, TrustEmerald)
        AppRiskLevel.MEDIUM -> Triple(CautionAmberContainer, CautionAmberBorder, CautionAmber)
        AppRiskLevel.HIGH -> Triple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson)
        AppRiskLevel.CRITICAL -> Triple(Color(0xFFB71C1C), AlertCrimson, Color.White)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (level != AppRiskLevel.CRITICAL) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = level.displayName.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}
