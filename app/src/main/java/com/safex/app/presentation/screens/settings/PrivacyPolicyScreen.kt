package com.safex.app.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Last Updated
            Text(
                text = "Last Updated: January 2025",
                fontSize = 13.sp,
                color = MutedSlate,
                fontWeight = FontWeight.Medium
            )

            // Introduction
            PrivacySection(
                title = "Introduction",
                content = "SAFEx (Secure Anti-Fraud Shield) is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application."
            )

            // Information We Collect
            PrivacySection(
                title = "Information We Collect",
                content = """
                    |SAFEx operates on a "local-first" principle. Most of your data stays on your device:
                    |
                    |• Call records and analysis results (stored locally)
                    |• Scan history of URLs and QR codes (stored locally)
                    |• Chat conversations with SAFEx Guard (stored locally)
                    |• Emergency contacts you configure (stored locally)
                    |• App preferences and settings (stored locally)
                    |• Device security status information
                """.trimMargin()
            )

            // How We Use Your Information
            PrivacySection(
                title = "How We Use Your Information",
                content = """
                    |We use the information collected to:
                    |
                    |• Analyze incoming calls for potential fraud patterns
                    |• Scan URLs and QR codes for security threats
                    |• Provide personalized safety guidance through SAFEx Guard
                    |• Display security status and recommendations
                    |• Enable emergency SOS features
                    |
                    |All processing happens on your device unless you enable AI features.
                """.trimMargin()
            )

            // Third-Party Services
            PrivacySection(
                title = "Third-Party Services",
                content = """
                    |When you enable AI-powered features, the following services may be used:
                    |
                    |• Firebase (for AI Assistant features)
                    |• VirusTotal API (for URL/QR code scanning)
                    |
                    |When using these services:
                    |• Sensitive data (phone numbers, OTPs, card details) is automatically masked before transmission
                    |• Only the information necessary for analysis is sent
                    |• You can view what data was sent
                    |• You can disable AI features completely in Settings
                """.trimMargin()
            )

            // Permissions We Request
            PrivacySection(
                title = "Permissions We Request",
                content = """
                    |• Phone State: To detect incoming/outgoing calls
                    |• Contacts: To verify unknown numbers
                    |• Camera: To scan QR codes
                    |• Microphone: For voice input in SAFEx Guard (optional)
                    |• Internet: For online threat databases and AI features (optional)
                    |
                    |All permissions are requested only when needed and with clear explanations.
                """.trimMargin()
            )

            // Data Security
            PrivacySection(
                title = "Data Security",
                content = """
                    |We implement industry-standard security measures:
                    |
                    |• All local data is encrypted using Android Keystore
                    |• No passwords or sensitive credentials are stored
                    |• Automatic data masking before any external transmission
                    |• Regular security audits and updates
                """.trimMargin()
            )

            // Data Retention
            PrivacySection(
                title = "Data Retention",
                content = """
                    |• Call records: Stored locally until you delete them
                    |• Chat history: Auto-deleted based on your preference (never, 7 days, or 30 days)
                    |• Scan results: Stored locally for 90 days, then auto-deleted
                    |
                    |You can manually delete all data anytime from Settings.
                """.trimMargin()
            )

            // Your Rights
            PrivacySection(
                title = "Your Rights",
                content = """
                    |You have the right to:
                    |
                    |• Access your stored data
                    |• Delete your data at any time
                    |• Disable AI and cloud features
                    |• Export your data
                    |• Revoke permissions
                    |
                    |Use the Settings screen to manage your data and privacy preferences.
                """.trimMargin()
            )

            // Children's Privacy
            PrivacySection(
                title = "Children's Privacy",
                content = "SAFEx is not intended for users under 13 years of age. We do not knowingly collect personal information from children."
            )

            // Changes to Privacy Policy
            PrivacySection(
                title = "Changes to This Policy",
                content = "We may update this Privacy Policy from time to time. We will notify you of any significant changes through the app or via email. Your continued use of the app after changes constitutes acceptance of the updated policy."
            )

            // Contact Us
            PrivacySection(
                title = "Contact Us",
                content = """
                    |If you have questions about this Privacy Policy, contact us at:
                    |
                    |Email: aditya.upmanyu_cs24@gla.ac.in
                    |GitHub: github.com/aditya-upmanyu/Safex
                    |
                    |For cyber fraud reporting, always contact:
                    |National Cyber Crime Helpline: 1930
                    |Portal: cybercrime.gov.in
                """.trimMargin()
            )

            // Government Compliance
            PrivacySection(
                title = "Legal Compliance",
                content = "SAFEx complies with Indian data protection laws and regulations. We cooperate with law enforcement and government agencies for cyber fraud investigations when legally required."
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "SAFEx - Secure Anti-Fraud Shield\nProtecting India from Cyber Fraud",
                fontSize = 13.sp,
                color = MutedSlate,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ShieldBlue
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = CharcoalBlack,
            lineHeight = 22.sp
        )
    }
}
