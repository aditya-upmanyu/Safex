package com.safex.app.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.safex.app.R
import com.safex.app.presentation.components.*
import com.safex.app.presentation.navigation.Screen
import com.safex.app.presentation.theme.Spacing
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                            score = state.data.securityScore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.large)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (state.data.protectionActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (state.data.protectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(Spacing.medium))
                                Text(
                                    text = if (state.data.protectionActive) 
                                        stringResource(R.string.home_protection_active) 
                                    else 
                                        stringResource(R.string.home_protection_inactive),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.home_quick_actions),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = Spacing.medium)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            QuickActionCard(
                                title = stringResource(R.string.home_scan_url),
                                icon = Icons.Default.QrCode,
                                onClick = { navController.navigate(Screen.UrlScanner.route) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                title = stringResource(R.string.home_device_security),
                                icon = Icons.Default.Security,
                                onClick = { navController.navigate(Screen.DeviceSecurity.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (state.data.recentAlerts.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.home_recent_alerts),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = Spacing.medium)
                            )
                        }

                        items(state.data.recentAlerts) { call ->
                            CallRecordItem(
                                record = call,
                                onClick = { }
                            )
                        }
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
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Button(onClick = { viewModel.loadHomeData() }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            is UiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.empty_state))
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
