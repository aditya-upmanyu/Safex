package com.safex.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safex.app.presentation.theme.Spacing

@Composable
fun AlertBanner(
    message: String,
    severity: String = "HIGH",
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (severity) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH" -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        "MEDIUM" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Alert",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onError
            )
            Spacer(modifier = Modifier.width(Spacing.medium))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}
