package com.safex.app.presentation.screens.urlscanner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.domain.model.RiskLevel
import com.safex.app.domain.model.ScanResult
import com.safex.app.presentation.components.RiskBadge
import com.safex.app.presentation.theme.*
import com.safex.app.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UrlScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: UrlScannerViewModel = hiltViewModel()
) {
    var urlInput by remember { mutableStateOf("") }
    var showQrScanner by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(100),
        label = "btn_scale"
    )

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.url_scanner_title),
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─────────────────────────────────────────────────────────────────
            // HERO ZONE — Bold Banner (Safe, Suspicious, or Malicious)
            // ─────────────────────────────────────────────────────────────────
            when (val state = uiState) {
                is UiState.Success -> {
                    ScanResultHero(result = state.data)
                }
                is UiState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = ShieldBlue)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Checking URL against VirusTotal & scam databases...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CharcoalBlack,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = AlertCrimsonContainer,
                        border = BorderStroke(1.dp, AlertCrimsonBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = state.message,
                                color = AlertCrimson,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> {
                    // Default Idle Hero
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceWhite,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ShieldBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ShieldBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Inspect Links Before Clicking",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CharcoalBlack
                                )
                                Text(
                                    text = "Paste any SMS, WhatsApp, or email link to check for phishing and fraud.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // INPUT CONTAINER — Clean White Card with 56dp Touch Target
            // ─────────────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                        Text(
                            text = "Website or Link URL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalBlack
                        )
                        
                        // QR Scanner Button
                        OutlinedButton(
                            onClick = { showQrScanner = true },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ShieldBlue
                            ),
                            border = BorderStroke(1.dp, ShieldBlue)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR Code",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Scan QR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.url_scanner_input_hint),
                                color = SubduedSlate
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = MutedSlate
                            )
                        },
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // 56.dp Minimum Touch Target Button with Tactile Feedback
                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                viewModel.scanUrl(urlInput.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .scale(btnScale),
                        enabled = urlInput.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldBlue,
                            disabledContainerColor = BorderSubtle,
                            contentColor = Color.White
                        ),
                        interactionSource = interactionSource
                    ) {
                        Text(
                            text = stringResource(R.string.url_scanner_scan_button),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // QR Scanner Dialog
            if (showQrScanner) {
                QrScannerDialog(
                    onDismiss = { showQrScanner = false },
                    onQrCodeScanned = { scannedUrl ->
                        urlInput = scannedUrl
                        showQrScanner = false
                        // Auto-scan the detected URL
                        viewModel.scanUrl(scannedUrl.trim())
                    }
                )
            }

            // ─────────────────────────────────────────────────────────────────
            // SCAN RESULT DETAILS (If available)
            // ─────────────────────────────────────────────────────────────────
            if (uiState is UiState.Success) {
                val data = (uiState as UiState.Success<ScanResult>).data
                ScanResultDetails(result = data)
            }
        }
    }
}

@Composable
private fun ScanResultHero(result: ScanResult) {
    val (bgColor, borderColor, textColor, title, icon) = when (result.verdict) {
        RiskLevel.SAFE -> Quintuple(
            TrustEmeraldContainer,
            TrustEmeraldBorder,
            TrustEmerald,
            "VERIFIED SAFE LINK",
            Icons.Default.CheckCircle
        )
        RiskLevel.SUSPICIOUS -> Quintuple(
            CautionAmberContainer,
            CautionAmberBorder,
            CautionAmber,
            "SUSPICIOUS LINK DETECTED",
            Icons.Default.Warning
        )
        RiskLevel.FRAUD -> Quintuple(
            AlertCrimsonContainer,
            AlertCrimsonBorder,
            AlertCrimson,
            "DANGEROUS PHISHING LINK",
            Icons.Default.Warning
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = result.targetUrl,
                style = MaterialTheme.typography.bodySmall,
                color = CharcoalBlack,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            RiskBadge(
                riskLevel = result.verdict,
                riskScore = result.riskScore
            )
        }
    }
}

@Composable
private fun ScanResultDetails(result: ScanResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Security Analysis Details",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalBlack
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Analysis Engine", value = result.source.ifBlank { "VirusTotal & Local Intelligence" })
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            DetailRow(label = "Security Verdict", value = result.verdict.name)
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            DetailRow(label = "Risk Assessment Score", value = "${result.riskScore} / 100")

            if (result.threatInfo.isNotBlank()) {
                HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Threat Intelligence Notes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.threatInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = CharcoalBlack
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MutedSlate)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = CharcoalBlack)
    }
}

private data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)

@Composable
private fun QrScannerDialog(
    onDismiss: () -> Unit,
    onQrCodeScanned: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = ShieldBlue,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Scan QR Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CharcoalBlack
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera preview placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CleanSlate)
                        .clickable { 
                            // Simulate QR code scan for demo
                            onQrCodeScanned("https://example.com/scanned-qr-code")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = ShieldBlue,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            "Point camera at QR code",
                            fontSize = 14.sp,
                            color = MutedSlate,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "(Tap here for demo)",
                            fontSize = 12.sp,
                            color = SubduedSlate,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShieldBlueContainer,
                    border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = ShieldBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "QR codes will be analyzed for safety before opening",
                            fontSize = 12.sp,
                            color = ShieldBlue,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = ShieldBlue
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
