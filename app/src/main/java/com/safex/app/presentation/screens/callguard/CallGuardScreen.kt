package com.safex.app.presentation.screens.callguard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.components.CallRecordItem
import com.safex.app.presentation.components.LiveWaveformPulseIndicator
import com.safex.app.presentation.theme.*
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallGuardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVoiceAnalysis: (Long) -> Unit = {},
    viewModel: CallGuardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.call_guard_title),
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
                    IconButton(onClick = { viewModel.loadCalls() }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanSlate)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ─────────────────────────────────────────────────────────────────
            // HERO ZONE — Live Protection Status with Steady Pulse
            // ─────────────────────────────────────────────────────────────────
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
                    LiveWaveformPulseIndicator(
                        icon = Icons.Default.PhoneInTalk,
                        tintColor = TrustEmerald,
                        size = 60.dp,
                        isLive = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Real-Time Call Shield Active",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalBlack
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Incoming calls are analyzed on-device for digital arrest, bank fraud & impersonation patterns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monitored Calls",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
            }

            // ─────────────────────────────────────────────────────────────────
            // CONTENT STREAM — 12.dp spacing between white cards
            // ─────────────────────────────────────────────────────────────────
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ShieldBlue)
                    }
                }

                is UiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.data) { call ->
                            CallRecordItem(
                                record = call,
                                onClick = { onNavigateToVoiceAnalysis(call.id) }
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = AlertCrimson,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadCalls() },
                                colors = ButtonDefaults.buttonColors(containerColor = ShieldBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(stringResource(R.string.retry), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                is UiState.Empty -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.call_guard_no_calls),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedSlate,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
