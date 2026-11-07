package com.safex.app.presentation.screens.defense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.core.remote.RemoteAccessAppRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteAccessScreen(onNavigateBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Access Sentry", fontWeight = FontWeight.SemiBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.DesktopWindows, null,
                            tint = Color(0xFF7E57C2), modifier = Modifier.size(28.dp))
                        Text("Remote Access Protection", color = Color.White,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "SafeX detects when known remote-access or screen-sharing apps are in the " +
                        "foreground, especially alongside active phone calls or financial apps.\n\n" +
                        "Remote support apps have legitimate uses. The risk comes from context — " +
                        "if you did not initiate the session, disconnect immediately.",
                        color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
            }

            // Honest capability statement
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("What SafeX Can Detect", color = Color.White,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    DetectRow("Remote app in foreground", true, "Accessibility service (foreground app change)")
                    DetectRow("Remote app installed", true, "PackageManager query")
                    DetectRow("Active call during remote session", true, "TelephonyManager")
                    DetectRow("Actual screen-capture session", false,
                        "NOT VERIFIED — MediaProjection from other apps is not accessible")
                    DetectRow("Data transmitted to remote party", false,
                        "NOT SUPPORTED — Android sandboxing prevents this")
                }
            }

            // Monitored apps list
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Monitored Remote Apps (${RemoteAccessAppRegistry.knownApps.size})",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    RemoteAccessAppRegistry.knownApps.forEach { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DesktopWindows, null,
                                    tint = Color(0xFF7E57C2), modifier = Modifier.size(16.dp))
                                Text(app.displayName, color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (app.riskLevel == RemoteAccessAppRegistry.RiskLevel.HIGH)
                                    Color(0x33EF5350) else Color(0x33FF7043)
                            ) {
                                Text(
                                    app.riskLevel.name,
                                    color = if (app.riskLevel == RemoteAccessAppRegistry.RiskLevel.HIGH)
                                        Color(0xFFEF5350) else Color(0xFFFF7043),
                                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action guidance
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1B0000)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔴 If a Remote Session is Active Without Your Consent",
                        color = Color(0xFFEF9A9A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Go to Settings → Apps → find the remote app → Force Stop.",
                        "Uninstall the app immediately.",
                        "Change all passwords and UPI PINs from a different device.",
                        "Call Cyber Helpline 1930 to report.",
                        "Check your bank account for unauthorized transactions."
                    ).forEachIndexed { i, step ->
                        Text("${i + 1}. $step",
                            color = Color(0xFFEF9A9A).copy(alpha = 0.85f),
                            fontSize = 12.sp, lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectRow(label: String, supported: Boolean, note: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (supported) Icons.Default.CheckCircle else Icons.Default.Cancel, null,
            tint = if (supported) Color(0xFF4CAF50) else Color(0xFF78909C),
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Column {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(note, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}
