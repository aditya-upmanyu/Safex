// presentation/appsentry/AppSentryScreen.kt
package com.safex.app.presentation.appsentry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.domain.appsentry.model.ApkInspectionResult
import com.safex.app.domain.appsentry.model.AppRiskLevel
import com.safex.app.domain.appsentry.model.ScannedApp
import com.safex.app.presentation.appsentry.components.ApkInspectorDetailBottomSheet
import com.safex.app.presentation.appsentry.components.AppDetailBottomSheet
import com.safex.app.presentation.appsentry.components.RiskChip
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSentryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AppSentryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // File picker launcher for APK Armor inspection
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(AppSentryEvent.InspectApk(uri.toString()))
        }
    }

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Sentry & APK Armor",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ShieldBlue)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onEvent(AppSentryEvent.ScanAllApps) },
                        enabled = !state.isScanning
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Scan", tint = MutedSlate)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanSlate)
                .padding(innerPadding)
        ) {
            // Hero Status Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Application Security Status",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CharcoalBlack
                            )
                            Text(
                                text = "${state.installedApps.size} apps analyzed on device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }

                        if (state.dangerousCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AlertCrimsonContainer,
                                border = BorderStroke(1.dp, AlertCrimsonBorder)
                            ) {
                                Text(
                                    "${state.dangerousCount} Threats",
                                    color = AlertCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatusSummaryItem(count = state.safeCount, label = "Verified Safe", color = TrustEmerald)
                        StatusSummaryItem(count = state.suspiciousCount, label = "Suspicious", color = CautionAmber)
                        StatusSummaryItem(count = state.dangerousCount, label = "Dangerous", color = AlertCrimson)
                    }

                    if (state.isScanning) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ShieldBlue,
                            trackColor = BorderSubtle
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.currentScanningApp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                }
            }

            // Tab Row (0: Installed Apps, 1: APK Armor)
            PrimaryTabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.onEvent(AppSentryEvent.SwitchTab(0)) },
                    text = { Text("Installed Apps (${state.installedApps.size})") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.onEvent(AppSentryEvent.SwitchTab(1)) },
                    text = { Text("APK Armor (${state.inspectedApks.size})") }
                )
            }

            if (state.selectedTab == 0) {
                // Tab 0: Installed Apps List
                InstalledAppsTab(
                    state = state,
                    onSearchQueryChange = { viewModel.onEvent(AppSentryEvent.SearchQueryChanged(it)) },
                    onFilterChange = { viewModel.onEvent(AppSentryEvent.FilterByRisk(it)) },
                    onSelectApp = { viewModel.onEvent(AppSentryEvent.SelectApp(it)) }
                )
            } else {
                // Tab 1: APK Armor
                ApkArmorTab(
                    state = state,
                    onPickApk = { apkPickerLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*")) },
                    onSelectApk = { viewModel.onEvent(AppSentryEvent.SelectApk(it)) }
                )
            }
        }

        // Bottom Sheet: App Detail
        state.selectedApp?.let { app ->
            AppDetailBottomSheet(
                app = app,
                onDismiss = { viewModel.onEvent(AppSentryEvent.SelectApp(null)) },
                onToggleTrust = { trust -> viewModel.onEvent(AppSentryEvent.ToggleTrustApp(app.packageName, trust)) }
            )
        }

        // Bottom Sheet: APK Detail
        state.selectedApk?.let { apk ->
            ApkInspectorDetailBottomSheet(
                apk = apk,
                onDismiss = { viewModel.onEvent(AppSentryEvent.SelectApk(null)) },
                onDeleteApk = { deleteFile -> viewModel.onEvent(AppSentryEvent.DeleteApkInspection(apk.filePath, deleteFile)) }
            )
        }
    }
}

@Composable
private fun InstalledAppsTab(
    state: AppSentryUiState,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (AppRiskLevel?) -> Unit,
    onSelectApp: (ScannedApp) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search installed apps...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = state.filterRiskLevel == null,
                    onClick = { onFilterChange(null) },
                    label = { Text("All") }
                )
            }
            item {
                FilterChip(
                    selected = state.filterRiskLevel == AppRiskLevel.CRITICAL || state.filterRiskLevel == AppRiskLevel.HIGH,
                    onClick = { onFilterChange(if (state.filterRiskLevel == AppRiskLevel.HIGH) null else AppRiskLevel.HIGH) },
                    label = { Text("High Risk") }
                )
            }
            item {
                FilterChip(
                    selected = state.filterRiskLevel == AppRiskLevel.MEDIUM,
                    onClick = { onFilterChange(if (state.filterRiskLevel == AppRiskLevel.MEDIUM) null else AppRiskLevel.MEDIUM) },
                    label = { Text("Medium Risk") }
                )
            }
            item {
                FilterChip(
                    selected = state.filterRiskLevel == AppRiskLevel.SAFE,
                    onClick = { onFilterChange(if (state.filterRiskLevel == AppRiskLevel.SAFE) null else AppRiskLevel.SAFE) },
                    label = { Text("Safe") }
                )
            }
        }

        // Apps List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.filteredApps, key = { it.packageName }) { app ->
                ScannedAppItem(app = app, onClick = { onSelectApp(app) })
            }
        }
    }
}

@Composable
private fun ApkArmorTab(
    state: AppSentryUiState,
    onPickApk: () -> Unit,
    onSelectApk: (ApkInspectionResult) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Inspect APK Button Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onPickApk() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Inspect Downloaded APK",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Verify SHA-256 checksum and scan with VirusTotal before installing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.isInspectingApk) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Inspecting APK file and checking VirusTotal...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Inspected APKs History
        Text(
            text = "Recent Inspections",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (state.inspectedApks.isEmpty() && !state.isInspectingApk) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No APK files inspected yet.\nTap 'Inspect Downloaded APK' above to analyze a file.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.inspectedApks, key = { it.filePath }) { apk ->
                    InspectedApkItem(apk = apk, onClick = { onSelectApk(apk) })
                }
            }
        }
    }
}

@Composable
private fun ScannedAppItem(app: ScannedApp, onClick: () -> Unit) {
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (app.icon != null) {
                Image(
                    bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                    .background(ShieldBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = null,
                        tint = ShieldBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CharcoalBlack
                )
                Text(
                    text = if (app.isSideLoaded) "Side-loaded" else "Google Play Store",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (app.isSideLoaded) AlertCrimson else MutedSlate
                )
            }

            RiskChip(level = app.riskLevel)
        }
    }
}

@Composable
private fun InspectedApkItem(apk: ApkInspectionResult, onClick: () -> Unit) {
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShieldBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = ShieldBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apk.appName ?: apk.fileName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CharcoalBlack
                )
                Text(
                    text = apk.packageName ?: "Unresolved package",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
            }

            RiskChip(level = apk.riskLevel)
        }
    }
}

@Composable
private fun StatusSummaryItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
