package com.safex.app.presentation.contactverification

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactVerificationScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToReport: () -> Unit = {},
    viewModel: ContactVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Verification", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )

                    Text(
                        "Check if a phone number is legitimate or reported for fraud",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                trailingIcon = {
                    if (phoneNumber.isNotEmpty()) {
                        IconButton(onClick = { phoneNumber = "" }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Verify Button
            Button(
                onClick = { viewModel.verifyContact(phoneNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = phoneNumber.isNotEmpty() && !uiState.isVerifying
            ) {
                if (uiState.isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Search, "Verify")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify Contact", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Verification Result
            AnimatedVisibility(
                visible = uiState.verificationResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                uiState.verificationResult?.let { result ->
                    VerificationResultCard(
                        result = result,
                        onReportClick = onNavigateToReport
                    )
                }
            }

            // Recent Verifications
            if (uiState.recentVerifications.isNotEmpty()) {
                Text(
                    "Recent Verifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                uiState.recentVerifications.take(5).forEach { verification ->
                    RecentVerificationItem(verification)
                }
            }
        }
    }
}

@Composable
fun VerificationResultCard(
    result: VerificationResult,
    onReportClick: () -> Unit = {}
) {
    val containerColor = when (result.trustLevel) {
        TrustLevel.SAFE -> MaterialTheme.colorScheme.primaryContainer
        TrustLevel.SUSPICIOUS -> MaterialTheme.colorScheme.tertiaryContainer
        TrustLevel.DANGEROUS -> MaterialTheme.colorScheme.errorContainer
        TrustLevel.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconColor = when (result.trustLevel) {
        TrustLevel.SAFE -> MaterialTheme.colorScheme.primary
        TrustLevel.SUSPICIOUS -> MaterialTheme.colorScheme.tertiary
        TrustLevel.DANGEROUS -> MaterialTheme.colorScheme.error
        TrustLevel.UNKNOWN -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Icon and Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (result.trustLevel) {
                            TrustLevel.SAFE -> Icons.Default.CheckCircle
                            TrustLevel.SUSPICIOUS -> Icons.Default.Warning
                            TrustLevel.DANGEROUS -> Icons.Default.Dangerous
                            TrustLevel.UNKNOWN -> Icons.Default.Help
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column {
                    Text(
                        result.trustLevel.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        result.phoneNumber,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Divider()

            // Details
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(
                    icon = Icons.Default.Report,
                    label = "Reports",
                    value = "${result.reportCount} complaints"
                )

                DetailRow(
                    icon = Icons.Default.Star,
                    label = "Trust Score",
                    value = "${result.trustScore}/100"
                )

                if (result.category.isNotEmpty()) {
                    DetailRow(
                        icon = Icons.Default.Category,
                        label = "Category",
                        value = result.category
                    )
                }

                if (result.lastReported != null) {
                    DetailRow(
                        icon = Icons.Default.Schedule,
                        label = "Last Reported",
                        value = result.lastReported
                    )
                }
            }

            // Recommendation
            if (result.recommendation.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            result.recommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (result.trustLevel == TrustLevel.DANGEROUS || result.trustLevel == TrustLevel.SUSPICIOUS) {
                    OutlinedButton(
                        onClick = { /* Block number */ },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Block, "Block", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Block")
                    }
                }

                OutlinedButton(
                    onClick = onReportClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Flag, "Report", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report")
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecentVerificationItem(verification: RecentVerification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (verification.trustLevel) {
                    TrustLevel.SAFE -> Icons.Default.CheckCircle
                    TrustLevel.SUSPICIOUS -> Icons.Default.Warning
                    TrustLevel.DANGEROUS -> Icons.Default.Dangerous
                    TrustLevel.UNKNOWN -> Icons.Default.Help
                },
                contentDescription = null,
                tint = when (verification.trustLevel) {
                    TrustLevel.SAFE -> MaterialTheme.colorScheme.primary
                    TrustLevel.SUSPICIOUS -> MaterialTheme.colorScheme.tertiary
                    TrustLevel.DANGEROUS -> MaterialTheme.colorScheme.error
                    TrustLevel.UNKNOWN -> MaterialTheme.colorScheme.outline
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    verification.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    verification.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (verification.trustLevel) {
                    TrustLevel.SAFE -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    TrustLevel.SUSPICIOUS -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    TrustLevel.DANGEROUS -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    TrustLevel.UNKNOWN -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                }
            ) {
                Text(
                    verification.trustLevel.displayName,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Data Classes
data class VerificationResult(
    val phoneNumber: String,
    val trustLevel: TrustLevel,
    val trustScore: Int,
    val reportCount: Int,
    val category: String,
    val lastReported: String?,
    val recommendation: String
)

data class RecentVerification(
    val phoneNumber: String,
    val trustLevel: TrustLevel,
    val timestamp: String
)

enum class TrustLevel(val displayName: String) {
    SAFE("Safe Contact"),
    SUSPICIOUS("Suspicious"),
    DANGEROUS("Dangerous - Fraud Reported"),
    UNKNOWN("Unknown")
}
