package com.safex.app.presentation.screens.defense

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.data.local.entity.SecurityIncidentEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreDefenseDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToUpi: () -> Unit = {},
    onNavigateToRemoteAccess: () -> Unit = {},
    onNavigateToDigitalArrest: () -> Unit = {},
    onNavigateToFamilyGuardian: () -> Unit = {},
    viewModel: CoreDefenseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Core Defense Engine", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B2A))
            )
        },
        containerColor = Color(0xFF0D1B2A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Combined threat banner
            item {
                ThreatLevelBanner(level = state.combinedThreatLevel)
            }

            // Module cards
            item {
                Text("Protection Modules", color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }
            item {
                ModuleCard(
                    icon = Icons.Default.AccountBalance, title = "UPI Protection Shield",
                    subtitle = "Detects UPI fraud risk during calls",
                    level = state.upiShieldLevel, onClick = onNavigateToUpi
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.DesktopWindows, title = "Remote Access Sentry",
                    subtitle = "Monitors screen-sharing & remote control apps",
                    level = state.remoteAccessLevel, onClick = onNavigateToRemoteAccess
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.Gavel, title = "Digital Arrest Shield",
                    subtitle = "Detects govt impersonation & coercion language",
                    level = state.digitalArrestLevel, onClick = onNavigateToDigitalArrest
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.FamilyRestroom, title = "Family Guardian",
                    subtitle = "Emergency SOS to family contacts",
                    level = state.familyGuardianLevel, onClick = onNavigateToFamilyGuardian
                )
            }

            // Recent incidents
            if (state.recentIncidents.isNotEmpty()) {
                item {
                    Text("Recent Incidents", color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
                items(state.recentIncidents) { incident ->
                    IncidentRow(incident = incident,
                        onAck = { viewModel.acknowledgeIncident(incident.incidentId) })
                }
            }
        }
    }
}

@Composable
private fun ThreatLevelBanner(level: ThreatLevel) {
    val (bg, label, icon) = when (level) {
        ThreatLevel.CRITICAL -> Triple(Color(0xFFB71C1C), "CRITICAL THREAT ACTIVE", Icons.Default.Warning)
        ThreatLevel.HIGH     -> Triple(Color(0xFFE65100), "HIGH RISK", Icons.Default.Error)
        ThreatLevel.MODERATE -> Triple(Color(0xFFF57F17), "MODERATE RISK", Icons.Default.Info)
        ThreatLevel.LOW      -> Triple(Color(0xFF1B5E20), "LOW RISK", Icons.Default.CheckCircle)
        ThreatLevel.SAFE     -> Triple(Color(0xFF1B5E20), "ALL CLEAR", Icons.Default.Shield)
        ThreatLevel.UNKNOWN  -> Triple(Color(0xFF37474F), "MONITORING...", Icons.Default.HelpOutline)
    }
    Surface(shape = RoundedCornerShape(16.dp), color = bg) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Column {
                Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tap a module card for details", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun ModuleCard(
    icon: ImageVector, title: String, subtitle: String,
    level: ThreatLevel, onClick: () -> Unit
) {
    val levelColor = threatColor(level)
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A2A3A),
        border = BorderStroke(1.dp, levelColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(levelColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = levelColor, modifier = Modifier.size(24.dp)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(8.dp), color = levelColor.copy(alpha = 0.2f)) {
                    Text(
                        text = level.name,
                        color = levelColor, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentRow(incident: SecurityIncidentEntity, onAck: () -> Unit) {
    val color = when (incident.severity) {
        "CRITICAL" -> Color(0xFFEF5350)
        "HIGH"     -> Color(0xFFFF7043)
        "MODERATE" -> Color(0xFFFFCA28)
        else       -> Color(0xFF78909C)
    }
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    Surface(
        shape = RoundedCornerShape(12.dp), color = Color(0xFF1A2A3A),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(color)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(incident.incidentType.replace("_", " "),
                    color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(fmt.format(Date(incident.timestamp)),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            }
            if (!incident.userAcknowledged) {
                TextButton(onClick = onAck) {
                    Text("Ack", color = color, fontSize = 11.sp)
                }
            } else {
                Icon(Icons.Default.CheckCircle, null,
                    tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun threatColor(level: ThreatLevel) = when (level) {
    ThreatLevel.CRITICAL -> Color(0xFFEF5350)
    ThreatLevel.HIGH     -> Color(0xFFFF7043)
    ThreatLevel.MODERATE -> Color(0xFFFFCA28)
    ThreatLevel.LOW      -> Color(0xFF66BB6A)
    ThreatLevel.SAFE     -> Color(0xFF4CAF50)
    ThreatLevel.UNKNOWN  -> Color(0xFF78909C)
}
