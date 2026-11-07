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
import com.safex.app.core.upi.UpiAppRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiProtectionScreen(onNavigateBack: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UPI Protection Shield", fontWeight = FontWeight.SemiBold) },
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
            // Status card
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.AccountBalance, null,
                            tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                        Text("UPI Protection Active", color = Color.White,
                            fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "SafeX monitors when you open a UPI payment app during an active phone call. " +
                        "If a call is detected simultaneously, a warning is shown.\n\n" +
                        "You NEVER need a UPI PIN to receive money. Only enter your PIN to send.",
                        color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
            }

            // Detection capability
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Detection Capability", color = Color.White,
                        fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    CapabilityRow("UPI app foreground detection", true, "Accessibility service")
                    CapabilityRow("Active cellular call detection", true, "TelephonyManager")
                    CapabilityRow("VoIP call detection (WhatsApp/etc)", false, "NOT VERIFIED — Android restriction")
                    CapabilityRow("UPI PIN interception", false, "Never — we do not read screen content")
                }
            }

            // Monitored apps
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Monitored UPI Apps (${UpiAppRegistry.supportedApps.size})",
                        color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    UpiAppRegistry.supportedApps.forEach { app ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            Text(app.displayName, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                        }
                    }
                }
            }

            // Safety tips
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1B0000)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🛡️ Safety Rules", color = Color(0xFFEF9A9A),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Never enter your UPI PIN while on a call with an unknown person.",
                        "No bank or government officer will ask for your UPI PIN.",
                        "You do NOT need a PIN to receive money — only to send it.",
                        "If pressured, end the call immediately and call 1930."
                    ).forEach { tip ->
                        Text("• $tip", color = Color(0xFFEF9A9A).copy(alpha = 0.85f),
                            fontSize = 12.sp, lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityRow(label: String, supported: Boolean, note: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
            if (supported) Icons.Default.CheckCircle else Icons.Default.Cancel,
            null,
            tint = if (supported) Color(0xFF4CAF50) else Color(0xFF78909C),
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Column {
            Text(label, color = Color.White, fontSize = 13.sp)
            Text(note, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}
