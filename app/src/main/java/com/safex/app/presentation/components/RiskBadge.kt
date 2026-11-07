package com.safex.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.domain.model.RiskLevel
import com.safex.app.presentation.theme.*

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    riskScore: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, label) = when (riskLevel) {
        RiskLevel.SAFE -> Quadruple(TrustEmeraldContainer, TrustEmeraldBorder, TrustEmerald, "SAFE")
        RiskLevel.SUSPICIOUS -> Quadruple(CautionAmberContainer, CautionAmberBorder, CautionAmber, "SUSPICIOUS")
        RiskLevel.FRAUD -> Quadruple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson, "HIGH RISK")
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(textColor, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$riskScore/100",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MutedSlate
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
