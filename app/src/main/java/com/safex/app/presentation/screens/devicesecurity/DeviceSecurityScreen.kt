package com.safex.app.presentation.screens.devicesecurity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.domain.security.SecurityRecommendationEngine
import com.safex.app.domain.security.model.SecurityCheckResult
import com.safex.app.presentation.components.SecurityScoreGauge
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceSecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DeviceSecurityEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DeviceSecurityEvent.ScanCompleted -> {
                    snackbarHostState.showSnackbar("Security scan completed")
                }
                is DeviceSecurityEvent.ShowCheckDetail -> {
                    // TODO: Show detail bottom sheet
                }
            }
        }
    }

    Scaffold(
        containerColor = CleanSlate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.device_security_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShieldBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSecurityStatus() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MutedSlate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is DeviceSecurityUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanSlate)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ShieldBlue)
                }
            }

            is DeviceSecurityUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanSlate)
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ─────────────────────────────────────────────────────────
                    // HERO ZONE — Device Health Security Gauge (top 30%)
                    // ─────────────────────────────────────────────────────────
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = SurfaceWhite,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Device Integrity Audit",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CharcoalBlack
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SecurityScoreGauge(
                                    score = state.score.totalScore,
                                    modifier = Modifier
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Grade and message
                                Text(
                                    text = "Grade: ${state.score.grade.displayName}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (state.score.grade) {
                                        com.safex.app.domain.security.model.SecurityScore.Grade.A,
                                        com.safex.app.domain.security.model.SecurityScore.Grade.B -> TrustEmerald
                                        com.safex.app.domain.security.model.SecurityScore.Grade.C -> CautionAmber
                                        else -> AlertCrimson
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = state.score.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Coverage indicator
                                Text(
                                    text = "Coverage: ${state.checks.count { it.status !in listOf(com.safex.app.domain.security.model.SecurityStatus.UNKNOWN, com.safex.app.domain.security.model.SecurityStatus.UNSUPPORTED) }} of ${state.checks.size} checks verified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SubduedSlate,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Status filter chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusFilterChip(
                                label = "Critical: ${state.criticalCount}",
                                isSelected = state.statusFilter == com.safex.app.domain.security.model.SecurityStatus.DANGER,
                                onClick = { viewModel.filterByStatus(com.safex.app.domain.security.model.SecurityStatus.DANGER) },
                                color = AlertCrimson
                            )
                            StatusFilterChip(
                                label = "Warnings: ${state.warningCount}",
                                isSelected = state.statusFilter == com.safex.app.domain.security.model.SecurityStatus.WARNING,
                                onClick = { viewModel.filterByStatus(com.safex.app.domain.security.model.SecurityStatus.WARNING) },
                                color = CautionAmber
                            )
                            StatusFilterChip(
                                label = "Secure: ${state.secureCount}",
                                isSelected = state.statusFilter == com.safex.app.domain.security.model.SecurityStatus.SECURE,
                                onClick = { viewModel.filterByStatus(com.safex.app.domain.security.model.SecurityStatus.SECURE) },
                                color = TrustEmerald
                            )
                        }
                    }
                    
                    // Recommendations section
                    if (state.recommendations.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recommended Actions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalBlack,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        items(state.recommendations.take(3)) { recommendation ->
                            RecommendationCard(
                                recommendation = recommendation,
                                onActionClick = {
                                    recommendation.remediation?.let { viewModel.navigateToSettings(it) }
                                }
                            )
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = "Security Checks by Category",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalBlack,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    // Checks grouped by category
                    state.checksByCategory.forEach { (category, checks) ->
                        item {
                            Text(
                                text = category.displayName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ShieldBlue,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        
                        items(checks) { check ->
                            SecurityCheckItem(
                                check = check,
                                onClick = { viewModel.showCheckDetail(check) }
                            )
                        }
                    }
                }
            }

            is DeviceSecurityUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanSlate)
                        .padding(padding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = state.message,
                                color = AlertCrimson,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSecurityStatus() },
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(stringResource(R.string.retry), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            /* No Empty state needed anymore */
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CleanSlate)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No security check data available", color = MutedSlate)
                }
            }
        }
    }
}

@Composable
fun SecurityCheckItem(check: SecurityCheckResult, onClick: () -> Unit) {
    val (statusBg, statusBorder, statusColor, statusLabel) = when (check.status) {
        com.safex.app.domain.security.model.SecurityStatus.SECURE -> Quadruple(TrustEmeraldContainer, TrustEmeraldBorder, TrustEmerald, "SECURE")
        com.safex.app.domain.security.model.SecurityStatus.WARNING -> Quadruple(CautionAmberContainer, CautionAmberBorder, CautionAmber, "WARNING")
        com.safex.app.domain.security.model.SecurityStatus.DANGER -> Quadruple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson, "DANGER")
        com.safex.app.domain.security.model.SecurityStatus.INFO -> Quadruple(ShieldBlueContainer, BorderSubtle, ShieldBlue, "INFO")
        com.safex.app.domain.security.model.SecurityStatus.UNKNOWN -> Quadruple(CleanSlate, BorderSubtle, MutedSlate, "UNKNOWN")
        else -> Quadruple(CleanSlate, BorderSubtle, SubduedSlate, "N/A")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(statusBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (check.status) {
                        com.safex.app.domain.security.model.SecurityStatus.SECURE -> Icons.Default.CheckCircle
                        else -> Icons.Default.Warning
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = check.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                Text(
                    text = check.userExplanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg,
                border = BorderStroke(1.dp, statusBorder)
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)


@Composable
private fun StatusFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: androidx.compose.ui.graphics.Color
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        )
    )
}

@Composable
private fun RecommendationCard(
    recommendation: SecurityRecommendationEngine.Recommendation,
    onActionClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, when (recommendation.priority) {
            SecurityRecommendationEngine.Recommendation.Priority.CRITICAL -> AlertCrimsonBorder
            SecurityRecommendationEngine.Recommendation.Priority.HIGH -> CautionAmberBorder
            else -> BorderSubtle
        })
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(when (recommendation.priority) {
                            SecurityRecommendationEngine.Recommendation.Priority.CRITICAL -> AlertCrimson
                            SecurityRecommendationEngine.Recommendation.Priority.HIGH -> CautionAmber
                            SecurityRecommendationEngine.Recommendation.Priority.MEDIUM -> ShieldBlue
                            SecurityRecommendationEngine.Recommendation.Priority.LOW -> SubduedSlate
                        })
                )
                
                Text(
                    text = recommendation.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MutedSlate,
                lineHeight = 18.sp
            )
            
            if (recommendation.remediation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(recommendation.remediation.label, fontSize = 14.sp)
                }
            }
        }
    }
}
