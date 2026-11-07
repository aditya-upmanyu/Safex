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
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.core.digitalarrest.CoercionRiskScorer
import com.safex.app.domain.usecase.EvaluateDigitalArrestUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalArrestScreen(
    onNavigateBack: () -> Unit = {},
    evaluateUseCase: EvaluateDigitalArrestUseCase? = null
) {
    var inputText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CoercionRiskScorer.CoercionRiskResult?>(null) }
    var analysed by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Arrest Shield", fontWeight = FontWeight.SemiBold) },
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
            // Info
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Gavel, null,
                            tint = Color(0xFFE91E63), modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                "Digital Arrest Detection", 
                                color = Color.White,
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "ENABLED",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Indian law enforcement does NOT arrest people over a video call or ask " +
                        "for money transfers to 'verification accounts'.\n\n" +
                        "Paste suspicious call text below to check for digital arrest scam indicators.",
                        color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
            }

            // Text input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it; analysed = false; result = null },
                label = { Text("Paste suspicious conversation text", color = Color.White.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFE91E63),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFFE91E63),
                    focusedContainerColor = Color(0xFF1A2A3A),
                    unfocusedContainerColor = Color(0xFF1A2A3A)
                )
            )

            Button(
                onClick = {
                    result = evaluateUseCase?.evaluate(inputText)
                    analysed = true
                },
                enabled = inputText.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
            ) {
                Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Analyse Text", fontWeight = FontWeight.SemiBold)
            }

            // Result
            if (analysed) {
                val r = result
                if (r != null) {
                    ScoreResultCard(r)
                } else {
                    // evaluateUseCase not injected in preview — show placeholder
                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                        Text("Analysis service not available.",
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                    }
                }
            }

            // Legal reality box
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1B0000)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚖️ Legal Reality", color = Color(0xFFEF9A9A),
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "No legitimate Indian law enforcement arrests people via video call.",
                        "No government agency asks you to transfer money for 'verification'.",
                        "Skype / WhatsApp 'interrogations' are not real legal proceedings.",
                        "CBI, ED, and Customs do not demand OTPs or UPI payments.",
                        "If threatened, disconnect and call 1930 (Cyber Crime Helpline)."
                    ).forEach { item ->
                        Text("• $item", color = Color(0xFFEF9A9A).copy(alpha = 0.85f),
                            fontSize = 12.sp, lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreResultCard(result: CoercionRiskScorer.CoercionRiskResult) {
    val (bg, textColor) = when (result.riskCategory) {
        CoercionRiskScorer.RiskCategory.CRITICAL -> Color(0xFFB71C1C) to Color.White
        CoercionRiskScorer.RiskCategory.HIGH     -> Color(0xFFE65100) to Color.White
        CoercionRiskScorer.RiskCategory.MODERATE -> Color(0xFFF57F17) to Color.Black
        CoercionRiskScorer.RiskCategory.LOW      -> Color(0xFF1B5E20) to Color.White
    }
    Surface(shape = RoundedCornerShape(16.dp), color = bg) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Risk: ${result.riskCategory}", color = textColor,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Score: ${result.score}/100", color = textColor,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text("Confidence: ${result.confidence}", color = textColor.copy(alpha = 0.85f), fontSize = 13.sp)
            if (result.negationDetected) {
                Text("ℹ️ Negation detected — scores reduced accordingly",
                    color = textColor.copy(alpha = 0.75f), fontSize = 12.sp)
            }
            if (result.matchedIndicators.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("Matched indicators:", color = textColor.copy(alpha = 0.85f),
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                result.matchedIndicators.take(6).forEach { ind ->
                    Text("• ${ind.phrase} (+${ind.weight})", color = textColor.copy(alpha = 0.8f),
                        fontSize = 11.sp)
                }
            }
        }
    }
}
