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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.core.security.model.ThreatLevel
import com.safex.app.data.local.entity.SecurityIncidentEntity
import com.safex.app.presentation.theme.*
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
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Core Defense Engine",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Back",
                            tint = ShieldBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Combined threat banner
            item {
                ThreatLevelBanner(level = state.combinedThreatLevel)
            }

            // Module cards
            item {
                Text(
                    "Protection Modules",
                    color = MutedSlate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.AccountBalance,
                    title = "UPI Protection Shield",
                    subtitle = "Detects UPI fraud risk during calls",
                    level = state.upiShieldLevel,
                    onClick = onNavigateToUpi
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.DesktopWindows,
                    title = "Remote Access Sentry",
                    subtitle = "Monitors screen-sharing & remote control apps",
                    level = state.remoteAccessLevel,
                    onClick = onNavigateToRemoteAccess
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.Gavel,
                    title = "Digital Arrest Shield",
                    subtitle = "Detects govt impersonation & coercion language",
                    level = state.digitalArrestLevel,
                    onClick = onNavigateToDigitalArrest
                )
            }
            item {
                ModuleCard(
                    icon = Icons.Default.FamilyRestroom,
                    title = "Family Guardian",
                    subtitle = "Emergency SOS to family contacts",
                    level = state.familyGuardianLevel,
                    onClick = onNavigateToFamilyGuardian
                )
            }

            // Recent incidents
            if (state.recentIncidents.isNotEmpty()) {
                item {
                    Text(
                        "Recent Incidents",
                        color = MutedSlate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(state.recentIncidents) { incident ->
                    IncidentRow(
                        incident = incident,
                        onAck = { viewModel.acknowledgeIncident(incident.incidentId) }
                    )
                }
            }

            // Bottom disclaimer
            item {
                Text(
                    text = "SAFEx Guard provides safety guidance, not legal or financial advice. " +
                            "We cannot live-verify websites or phone numbers.",
                    fontSize = 11.sp,
                    color = SubduedSlate,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ThreatLevelBanner(level: ThreatLevel) {
    val (bgColor, borderColor, textColor, label, icon) = when (level) {
        ThreatLevel.CRITICAL -> Quintuple(
            AlertCrimsonContainer,
            AlertCrimsonBorder,
            AlertCrimson,
            "CRITICAL THREAT ACTIVE",
            Icons.Default.Warning
        )
        ThreatLevel.HIGH -> Quintuple(
            AlertCrimsonContainer,
            AlertCrimsonBorder,
            AlertCrimson,
            "HIGH RISK",
            Icons.Default.Error
        )
        ThreatLevel.MODERATE -> Quintuple(
            CautionAmberContainer,
            CautionAmberBorder,
            CautionAmber,
            "MODERATE RISK",
            Icons.Default.Info
        )
        ThreatLevel.LOW -> Quintuple(
            TrustEmeraldContainer,
            TrustEmeraldBorder,
            TrustEmerald,
            "LOW RISK",
            Icons.Default.CheckCircle
        )
        ThreatLevel.SAFE -> Quintuple(
            TrustEmeraldContainer,
            TrustEmeraldBorder,
            TrustEmerald,
            "ALL SYSTEMS PROTECTED",
            Icons.Default.Shield
        )
        ThreatLevel.UNKNOWN -> Quintuple(
            CleanSlate,
            BorderSubtle,
            MutedSlate,
            "MONITORING...",
            Icons.Default.HelpOutline
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = textColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    "Tap a module card for details",
                    color = CharcoalBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
private fun ModuleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    level: ThreatLevel,
    onClick: () -> Unit
) {
    val statusText = if (level == ThreatLevel.UNKNOWN) "UNKNOWN" else "ENABLED"
    val (statusBg, statusTextColor, statusBorder) = if (level == ThreatLevel.UNKNOWN) {
        Triple(CleanSlate, SubduedSlate, BorderSubtle)
    } else {
        Triple(TrustEmeraldContainer, TrustEmerald, TrustEmeraldBorder)
    }

    val levelColor = when (level) {
        ThreatLevel.CRITICAL -> AlertCrimson
        ThreatLevel.HIGH -> AlertCrimson
        ThreatLevel.MODERATE -> CautionAmber
        ThreatLevel.LOW -> TrustEmerald
        ThreatLevel.SAFE -> TrustEmerald
        ThreatLevel.UNKNOWN -> MutedSlate
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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(levelColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = levelColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    title,
                    color = CharcoalBlack,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    color = MutedSlate,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status pills column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ENABLED/UNKNOWN pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder)
                ) {
                    Text(
                        text = statusText,
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Threat level pill
                if (level != ThreatLevel.UNKNOWN) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = levelColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = level.name,
                            color = levelColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentRow(incident: SecurityIncidentEntity, onAck: () -> Unit) {
    val color = when (incident.severity) {
        "CRITICAL" -> AlertCrimson
        "HIGH" -> AlertCrimson
        "MODERATE" -> CautionAmber
        else -> MutedSlate
    }
    val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    incident.incidentType.replace("_", " "),
                    color = CharcoalBlack,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    fmt.format(Date(incident.timestamp)),
                    color = MutedSlate,
                    fontSize = 12.sp
                )
            }
            if (!incident.userAcknowledged) {
                Button(
                    onClick = onAck,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = color,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        "Acknowledge",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = TrustEmerald,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}