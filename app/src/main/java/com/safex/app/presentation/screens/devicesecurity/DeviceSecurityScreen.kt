package com.safex.app.presentation.screens.devicesecurity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.domain.model.CheckStatus
import com.safex.app.domain.model.SecurityCheck
import com.safex.app.presentation.components.SecurityScoreGauge
import com.safex.app.presentation.theme.Spacing
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSecurityScreen(
    onNavigateBack: () -> Unit,
    viewModel: DeviceSecurityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_security_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    item {
                        SecurityScoreGauge(
                            score = state.data.overallScore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.large)
                        )
                    }

                    item {
                        Text(
                            text = stringResource(R.string.device_security_checks),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    items(state.data.checks) { check ->
                        SecurityCheckItem(check)
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Button(onClick = { viewModel.loadSecurityStatus() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            is UiState.Empty -> {}
        }
    }
}

@Composable
fun SecurityCheckItem(check: SecurityCheck) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = check.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = check.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(Spacing.medium))
            val statusColor = when (check.status) {
                CheckStatus.OK -> MaterialTheme.colorScheme.primary
                CheckStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                CheckStatus.ALERT -> MaterialTheme.colorScheme.error
            }
            Text(
                text = check.status.name,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
        }
    }
}
