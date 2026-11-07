package com.safex.app.presentation.screens.devicesecurity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.safex.app.domain.model.CheckStatus
import com.safex.app.domain.model.SecurityCheck
import com.safex.app.presentation.components.SecurityScoreGauge
import com.safex.app.presentation.theme.*
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceSecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CleanSlate,
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
            is UiState.Loading -> {
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

            is UiState.Success -> {
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
                                    score = state.data.overallScore,
                                    modifier = Modifier
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Scanned for developer mode, root exploits, system permissions & encryption state.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Section Title
                    item {
                        Text(
                            text = stringResource(R.string.device_security_checks),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalBlack,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    // Content Stream of Security Checks (exact 12.dp spacing)
                    items(state.data.checks) { check ->
                        SecurityCheckItem(check)
                    }
                }
            }

            is UiState.Error -> {
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

            is UiState.Empty -> {
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
fun SecurityCheckItem(check: SecurityCheck) {
    val (statusBg, statusBorder, statusColor, statusLabel) = when (check.status) {
        CheckStatus.OK -> Quadruple(TrustEmeraldContainer, TrustEmeraldBorder, TrustEmerald, "PASS")
        CheckStatus.WARNING -> Quadruple(CautionAmberContainer, CautionAmberBorder, CautionAmber, "WARNING")
        CheckStatus.ALERT -> Quadruple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson, "ALERT")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                    imageVector = if (check.status == CheckStatus.OK) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = check.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                Text(
                    text = check.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    lineHeight = 18.sp
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
