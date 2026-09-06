package com.safex.app.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.theme.Spacing
import com.safex.app.presentation.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val riskThreshold by viewModel.riskThreshold.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var contactInput by remember(emergencyContact) { mutableStateOf(emergencyContact) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.settings_general),
                style = MaterialTheme.typography.titleLarge
            )

            SettingItem(
                title = stringResource(R.string.settings_theme),
                subtitle = themeMode.name,
                onClick = { showThemeDialog = true }
            )

            Divider()

            Text(
                text = stringResource(R.string.settings_security),
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = contactInput,
                onValueChange = {
                    contactInput = it
                    viewModel.setEmergencyContact(it)
                },
                label = { Text(stringResource(R.string.settings_emergency_contact)) },
                placeholder = { Text(stringResource(R.string.settings_emergency_contact_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "${stringResource(R.string.settings_risk_threshold)}: $riskThreshold",
                style = MaterialTheme.typography.bodyLarge
            )

            Slider(
                value = riskThreshold.toFloat(),
                onValueChange = { viewModel.setRiskThreshold(it.toInt()) },
                valueRange = 50f..90f,
                steps = 7
            )

            Divider()

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleLarge
            )

            SettingItem(
                title = stringResource(R.string.settings_version),
                subtitle = "1.0.0"
            )
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme)) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.small)
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = mode.name,
                                modifier = Modifier.padding(start = Spacing.small)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth(),
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
