package com.safex.app.presentation.components

import androidx.compose.foundation.background
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
import com.safex.app.domain.model.RiskLevel
import com.safex.app.presentation.theme.Green
import com.safex.app.presentation.theme.Red
import com.safex.app.presentation.theme.Amber

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    riskScore: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (riskLevel) {
        RiskLevel.SAFE -> Triple(Green, Color.White, "SAFE")
        RiskLevel.SUSPICIOUS -> Triple(Amber, Color.White, "SUSPICIOUS")
        RiskLevel.FRAUD -> Triple(Red, Color.White, "FRAUD")
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$riskScore/100",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
