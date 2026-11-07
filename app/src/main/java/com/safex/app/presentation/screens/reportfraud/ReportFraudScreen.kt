package com.safex.app.presentation.screens.reportfraud

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.theme.*
import com.safex.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFraudScreen(
    onNavigateBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") viewModel: ReportFraudViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.report_fraud_title),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Official Reporting Channels
            Text(
                text = "Official National Helplines",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalBlack
            )

            ReportOption(
                icon = Icons.Default.Public,
                title = stringResource(R.string.report_fraud_ncrp),
                subtitle = "National Cyber Crime Reporting Portal (cybercrime.gov.in)",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.NCRP_URL))
                    context.startActivity(intent)
                }
            )

            ReportOption(
                icon = Icons.Default.Phone,
                title = stringResource(R.string.report_fraud_cyber_helpline),
                subtitle = "Call 1930 for immediate financial cyber fraud freezing",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constants.CYBER_HELPLINE}"))
                    context.startActivity(intent)
                }
            )

            ReportOption(
                icon = Icons.Default.Report,
                title = stringResource(R.string.report_fraud_chakshu),
                subtitle = "Report suspected fraud communications on Sanchar Saathi",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CHAKSHU_URL))
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Section 2: Local Incident Notes
            Text(
                text = "Describe What Happened",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalBlack
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.report_fraud_description)) },
                        placeholder = { Text(stringResource(R.string.report_fraud_description_hint), color = SubduedSlate) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShieldBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = CleanSlate,
                            unfocusedContainerColor = CleanSlate
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ReportOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ShieldBlueContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ShieldBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = SubduedSlate,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
