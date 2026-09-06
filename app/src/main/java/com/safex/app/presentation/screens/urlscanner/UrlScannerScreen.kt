package com.safex.app.presentation.screens.urlscanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.components.RiskBadge
import com.safex.app.presentation.theme.Spacing
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: UrlScannerViewModel = hiltViewModel()
) {
    var urlInput by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.url_scanner_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text(stringResource(R.string.url_scanner_input_label)) },
                placeholder = { Text(stringResource(R.string.url_scanner_input_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    if (urlInput.isNotBlank()) {
                        viewModel.scanUrl(urlInput)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = urlInput.isNotBlank()
            ) {
                Text(stringResource(R.string.url_scanner_scan_button))
            }

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.url_scanner_scanning),
                            modifier = Modifier.padding(top = Spacing.extraLarge)
                        )
                    }
                }

                is UiState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            Text(
                                text = "Scan Result",
                                style = MaterialTheme.typography.titleMedium
                            )
                            RiskBadge(
                                riskLevel = state.data.verdict,
                                riskScore = state.data.riskScore
                            )
                            Text(
                                text = "URL: ${state.data.targetUrl}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = state.message,
                            modifier = Modifier.padding(Spacing.medium),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                is UiState.Empty -> {
                    // No result yet
                }
            }
        }
    }
}
