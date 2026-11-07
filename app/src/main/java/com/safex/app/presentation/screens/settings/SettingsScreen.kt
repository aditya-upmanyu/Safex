package com.safex.app.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val emergencyContact by viewModel.emergencyContact.collectAsState()
    val riskThreshold by viewModel.riskThreshold.collectAsState()

    val ttsEnabled by viewModel.chatTtsEnabled.collectAsState()
    val speechRate by viewModel.chatSpeechRate.collectAsState()
    val aiEnabled by viewModel.chatAiEnabled.collectAsState()
    val elderMode by viewModel.chatElderMode.collectAsState()
    val autoDeleteDays by viewModel.chatAutoDeleteDays.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showDeleteChatsConfirm by remember { mutableStateOf(false) }
    var contactInput by remember(emergencyContact) { mutableStateOf(emergencyContact) }

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ─────────────────────────────────────────────────────────────────
            // SECTION: General Appearance
            // ─────────────────────────────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_general))

            SettingsGroupCard {
                SettingsClickableItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.settings_theme),
                    subtitle = themeMode.name,
                    onClick = { showThemeDialog = true }
                )
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION: Security & Alerts
            // ─────────────────────────────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_security))

            SettingsGroupCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = stringResource(R.string.settings_emergency_contact),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalBlack
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contactInput,
                        onValueChange = {
                            contactInput = it
                            viewModel.setEmergencyContact(it)
                        },
                        placeholder = { Text(stringResource(R.string.settings_emergency_contact_hint), color = SubduedSlate) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShieldBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = CleanSlate,
                            unfocusedContainerColor = CleanSlate
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.settings_risk_threshold),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = "$riskThreshold / 100",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShieldBlue
                        )
                    }

                    Slider(
                        value = riskThreshold.toFloat(),
                        onValueChange = { viewModel.setRiskThreshold(it.toInt()) },
                        valueRange = 50f..90f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = ShieldBlue,
                            activeTrackColor = ShieldBlue
                        )
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION: SAFEx Guard Assistant
            // ─────────────────────────────────────────────────────────────────
            SettingsSectionHeader(title = "SAFEx Guard Assistant")

            SettingsGroupCard {
                Column {
                    SettingsSwitchRow(
                        title = "Elder Mode (Larger Display)",
                        subtitle = "High contrast, larger fonts & expanded touch targets",
                        checked = elderMode,
                        onCheckedChange = { viewModel.setChatElderMode(it) }
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    SettingsSwitchRow(
                        title = "AI Free-Text Assistant",
                        subtitle = "Ask open-ended fraud questions. Offline guides always work.",
                        checked = aiEnabled,
                        onCheckedChange = { viewModel.setChatAiEnabled(it) }
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    SettingsSwitchRow(
                        title = "Read Responses Aloud (TTS)",
                        subtitle = "Speaks guidance with Indian helpline pronunciation (1930)",
                        checked = ttsEnabled,
                        onCheckedChange = { viewModel.setChatTtsEnabled(it) }
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 18.dp))

                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Voice Speech Rate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
                            Text("${"%.1f".format(speechRate)}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ShieldBlue)
                        }

                        Slider(
                            value = speechRate,
                            onValueChange = { viewModel.setChatSpeechRate(it) },
                            valueRange = 0.7f..1.2f,
                            steps = 5,
                            colors = SliderDefaults.colors(
                                thumbColor = ShieldBlue,
                                activeTrackColor = ShieldBlue
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Auto-Delete Chat History", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Never", 7 to "7 Days", 30 to "30 Days").forEach { (days, label) ->
                                FilterChip(
                                    selected = autoDeleteDays == days,
                                    onClick = { viewModel.setChatAutoDeleteDays(days) },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { showDeleteChatsConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                            border = BorderStroke(1.dp, AlertCrimsonBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete All Chat History", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION: About & App Info
            // ─────────────────────────────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_about))

            SettingsGroupCard {
                Column {
                    SettingsClickableItem(
                        icon = Icons.Default.PrivacyTip,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = onNavigateToPrivacyPolicy
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 18.dp))
                    SettingsInfoRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_version),
                        value = "2.0.0 (Enterprise Guard)"
                    )
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(horizontal = 18.dp))
                    SettingsInfoRow(
                        icon = Icons.Default.Security,
                        title = "Security Architecture",
                        value = "Local-First • On-Device Masking"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDeleteChatsConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteChatsConfirm = false },
            title = { Text("Delete All Chats?", fontWeight = FontWeight.Bold, color = CharcoalBlack) },
            text = { Text("This will permanently remove all stored conversation records from your phone.", color = MutedSlate) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllChats()
                    showDeleteChatsConfirm = false
                }) {
                    Text("Delete", color = AlertCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChatsConfirm = false }) {
                    Text("Cancel", color = CharcoalBlack)
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Bold, color = CharcoalBlack) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = mode.name,
                                fontSize = 15.sp,
                                color = CharcoalBlack
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.ok), color = ShieldBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = ShieldBlue,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsGroupCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        content()
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ShieldBlueContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = SubduedSlate, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MutedSlate, lineHeight = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = ShieldBlue,
                uncheckedThumbColor = MutedSlate,
                uncheckedTrackColor = CleanSlate
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(ShieldBlueContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ShieldBlue, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CharcoalBlack, modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 13.sp, color = MutedSlate, fontWeight = FontWeight.Medium)
    }
}
