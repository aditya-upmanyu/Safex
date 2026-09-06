package com.safex.app.presentation.screens.reportfraud

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.R
import com.safex.app.presentation.theme.Spacing
import com.safex.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFraudScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportFraudViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Other") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_fraud_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = "Report to Government",
                style = MaterialTheme.typography.titleLarge
            )

            ReportOption(
                title = stringResource(R.string.report_fraud_ncrp),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.NCRP_URL))
                    context.startActivity(intent)
                }
            )

            ReportOption(
                title = stringResource(R.string.report_fraud_cyber_helpline),
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constants.CYBER_HELPLINE}"))
                    context.startActivity(intent)
                }
            )

            ReportOption(
                title = stringResource(R.string.report_fraud_chakshu),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CHAKSHU_URL))
                    context.startActivity(intent)
                }
            )

            Divider()

            Text(
                text = "Fraud Details",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.report_fraud_description)) },
                placeholder = { Text(stringResource(R.string.report_fraud_description_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )
        }
    }
}

@Composable
fun ReportOption(
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
