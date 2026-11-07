package com.safex.app.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.presentation.components.SecurityScoreGauge
import com.safex.app.presentation.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedHomeScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToCallGuard: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToEmergency: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDeviceSecurity: () -> Unit = {},
    onNavigateToReportFraud: () -> Unit = {},
    onNavigateToChatbot: () -> Unit = {},
    onNavigateToAppSentry: () -> Unit = {},
    onNavigateToSecurityAdvisor: () -> Unit = {},
    onNavigateToCoreDefense: () -> Unit = {},
    viewModel: EnhancedHomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            // Screen Header: Crisp background, Shield Blue title — no gradients
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SAFEx",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ShieldBlue,
                            letterSpacing = 0.sp
                        )
                        Text(
                            text = getCurrentGreeting(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                },
                actions = {
                    // Notification bell
                    IconButton(onClick = onNavigateToHistory) {
                        BadgedBox(
                            badge = {
                                if (uiState.notificationCount > 0) {
                                    Badge(
                                        containerColor = AlertCrimson,
                                        contentColor = SurfaceWhite
                                    ) {
                                        Text("${uiState.notificationCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MutedSlate
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MutedSlate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CleanSlate)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─────────────────────────────────────────────────────────────────
            // HERO ZONE — Security Score Gauge (top 30%: the most critical metric)
            // ─────────────────────────────────────────────────────────────────
            HeroSecurityCard(
                isActive = uiState.isProtectionActive,
                score = uiState.protectionScore,
                onToggle = { viewModel.toggleProtection() },
                onTap = onNavigateToDeviceSecurity
            )

            // ─────────────────────────────────────────────────────────────────
            // SAFEx Guard + App Sentry — Primary Feature Cards
            // ─────────────────────────────────────────────────────────────────
            PrimaryFeatureCard(
                icon = Icons.Default.SupportAgent,
                title = "Ask SAFEx Guard",
                subtitle = "Instant scam guidance, email & call fraud checks, voice mode",
                iconBg = ShieldBlue,
                onClick = onNavigateToChatbot
            )

            PrimaryFeatureCard(
                icon = Icons.Default.Shield,
                title = "App Sentry & APK Armor",
                subtitle = "Scan installed apps, detect fake banking apps & inspect APKs",
                iconBg = Color(0xFF1E3A5F),
                onClick = onNavigateToAppSentry
            )

            // ─────────────────────────────────────────────────────────────────
            // QUICK STATS — 3 columns
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(title = "Today's Activity")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    value = uiState.todayCalls.toString(),
                    label = "Analyzed",
                    icon = Icons.Default.Phone,
                    color = ShieldBlue,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = uiState.fraudBlocked.toString(),
                    label = "Blocked",
                    icon = Icons.Default.Block,
                    color = AlertCrimson,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = uiState.safeCalls.toString(),
                    label = "Safe",
                    icon = Icons.Default.CheckCircle,
                    color = TrustEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            // ─────────────────────────────────────────────────────────────────
            // QUICK ACTIONS GRID — Minimum 80dp×80dp per cell
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(title = "Quick Actions")

            QuickActionsGrid(
                onDashboardClick = onNavigateToDashboard,
                onCallGuardClick = onNavigateToCallGuard,
                onScannerClick = onNavigateToScanner,
                onVerifyClick = onNavigateToVerification,
                onEmergencyClick = onNavigateToEmergency,
                onHistoryClick = onNavigateToHistory,
                onReportFraudClick = onNavigateToReportFraud,
                onSecurityAdvisorClick = onNavigateToSecurityAdvisor,
                onCoreDefenseClick = onNavigateToCoreDefense
            )

            // ─────────────────────────────────────────────────────────────────
            // RECENT ALERTS STREAM
            // ─────────────────────────────────────────────────────────────────
            if (uiState.recentAlerts.isNotEmpty()) {
                SectionHeader(
                    title = "Recent Alerts",
                    actionLabel = "See All",
                    onAction = onNavigateToHistory
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.recentAlerts.take(3).forEach { alert ->
                        AlertListItem(alert = alert)
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // FRAUD TIPS HORIZONTAL SCROLL
            // ─────────────────────────────────────────────────────────────────
            if (uiState.fraudTips.isNotEmpty()) {
                SectionHeader(title = "Fraud Awareness")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(uiState.fraudTips) { tip ->
                        FraudTipCard(tip = tip)
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // PROTECTION FEATURES OVERVIEW
            // ─────────────────────────────────────────────────────────────────
            SectionHeader(title = "Protection Suite")
            ProtectionSuiteList()
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// HERO: Security Score Gauge Card
// FastOutSlowInEasing arc sweep 800ms · semantic color transitions
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun HeroSecurityCard(
    isActive: Boolean,
    score: Int,
    onToggle: () -> Unit,
    onTap: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header style: 22sp SemiBold Shield Blue
            Text(
                text = if (isActive) "Device Protected" else "Protection Off",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShieldBlue,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // The Security Gauge (animated arc, FastOutSlowIn 800ms)
            SecurityScoreGauge(
                score = if (isActive) score else 0,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Protection Button — min 56.dp height
            Button(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) ShieldBlue else AlertCrimson,
                    contentColor = SurfaceWhite
                )
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Shield else Icons.Default.ShieldMoon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isActive) "Active — Tap to Pause" else "Tap to Enable",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// PRIMARY FEATURE CARD — 56dp min height, tactile 3% scale feedback
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun PrimaryFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(100),
        label = "feature_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Card Title: 16sp Medium Charcoal Black
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = SubduedSlate,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// STAT TILE — Metric display 36sp Bold Charcoal Black letterSpacing 0.5sp
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatTile(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            // Main metric: 36sp Bold, Charcoal Black, letterSpacing 0.5sp
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = CharcoalBlack,
                letterSpacing = 0.5.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlate,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// SECTION HEADER
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Screen headers: 22sp SemiBold Shield Blue
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = CharcoalBlack
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    fontSize = 13.sp,
                    color = ShieldBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// QUICK ACTIONS GRID — 80dp×80dp cells, tactile feedback
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickActionsGrid(
    onDashboardClick: () -> Unit,
    onCallGuardClick: () -> Unit,
    onScannerClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onReportFraudClick: () -> Unit,
    onSecurityAdvisorClick: () -> Unit,
    onCoreDefenseClick: () -> Unit
) {
    val actions = listOf(
        Triple(Icons.Default.Dashboard, "Dashboard", onDashboardClick) to ShieldBlue,
        Triple(Icons.Default.Security, "Call Guard", onCallGuardClick) to TrustEmerald,
        Triple(Icons.Default.QrCode, "URL Scanner", onScannerClick) to ShieldBlueLight,
        Triple(Icons.Default.VerifiedUser, "Verify Contact", onVerifyClick) to CautionAmber,
        Triple(Icons.Default.ReportProblem, "Report Fraud", onReportFraudClick) to AlertCrimson,
        Triple(Icons.Default.Emergency, "Emergency SOS", onEmergencyClick) to AlertCrimson,
        Triple(Icons.Default.History, "History", onHistoryClick) to MutedSlate,
        Triple(Icons.Default.AdminPanelSettings, "Security Advisor", onSecurityAdvisorClick) to ShieldBlue,
        Triple(Icons.Default.GppGood, "Core Defense", onCoreDefenseClick) to TrustEmerald,
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        actions.chunked(3).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowActions.forEach { (triple, color) ->
                    val (icon, label, onClick) = triple
                    ActionCell(
                        icon = icon,
                        label = label,
                        color = color,
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill empty cells in the last row
                repeat(3 - rowActions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ActionCell(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(100),
        label = "action_cell_scale"
    )

    Surface(
        modifier = modifier
            .height(90.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CharcoalBlack,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// ALERT LIST ITEM
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun AlertListItem(alert: HomeAlert) {
    val (bgColor, borderColor, dotColor) = when (alert.severity.uppercase()) {
        "CRITICAL", "HIGH" -> Triple(AlertCrimsonContainer, AlertCrimsonBorder, AlertCrimson)
        "MEDIUM" -> Triple(CautionAmberContainer, CautionAmberBorder, CautionAmber)
        else -> Triple(ShieldBlueContainer, BorderSubtle, ShieldBlue)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Timestamp: 13sp Regular Muted Slate
                Text(
                    text = alert.timestamp,
                    fontSize = 12.sp,
                    color = SubduedSlate
                )
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// FRAUD TIP CARD — horizontal scroll carousel
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun FraudTipCard(tip: String) {
    Surface(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        color = ShieldBlueContainer,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = ShieldBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = tip,
                fontSize = 13.sp,
                color = CharcoalBlack,
                lineHeight = 19.sp
            )
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────────
// PROTECTION SUITE — Feature list
// ───────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProtectionSuiteList() {
    val features = listOf(
        Triple(Icons.Default.Call, "Call Guard", "Live AI scam call analysis"),
        Triple(Icons.Default.Link, "URL Scanner", "Phishing & fraud link detection"),
        Triple(Icons.Default.DevicesOther, "Device Security", "System vulnerability checks"),
        Triple(Icons.Default.SupportAgent, "SAFEx Guard AI", "24/7 fraud guidance assistant"),
        Triple(Icons.Default.Shield, "App Sentry", "Installed app threat detection"),
        Triple(Icons.Default.FolderZip, "APK Armor", "Pre-install file inspection"),
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column {
            features.forEachIndexed { index, (icon, title, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ShieldBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ShieldBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalBlack
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(TrustEmerald)
                    )
                }
                if (index < features.size - 1) {
                    HorizontalDivider(
                        color = BorderSubtle,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 18.dp)
                    )
                }
            }
        }
    }
}

fun getCurrentGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
}

data class HomeAlert(
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: String
)
