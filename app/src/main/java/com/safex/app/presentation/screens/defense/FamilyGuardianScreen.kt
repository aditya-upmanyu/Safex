package com.safex.app.presentation.screens.defense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.safex.app.data.local.entity.GuardianEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGuardianScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FamilyGuardianViewModel = hiltViewModel()
) {
    val guardians by viewModel.guardians.collectAsState()
    val message by viewModel.uiMessage.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Guardian", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, "Add Guardian", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B2A))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, "Add Guardian")
            }
        },
        containerColor = Color(0xFF0D1B2A)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Info banner
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF1A2A3A)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.FamilyRestroom, null,
                                tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                            Text("Family SOS Contacts", color = Color.White,
                                fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "In a critical threat event, SafeX can alert these contacts via SMS. " +
                            "Alerts are rate-limited and never include private data. " +
                            "Guardians receive only a safety check request with an incident ID.",
                            color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, lineHeight = 17.sp
                        )
                    }
                }
            }

            if (guardians.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.PeopleOutline, null,
                                tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Text("No guardians added", color = Color.White.copy(alpha = 0.4f),
                                fontSize = 14.sp)
                            Text("Tap + to add a family contact",
                                color = Color.White.copy(alpha = 0.3f), fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(guardians) { guardian ->
                    GuardianCard(
                        guardian = guardian,
                        onToggle = { viewModel.toggleGuardian(guardian, it) },
                        onDelete = { viewModel.deleteGuardian(guardian) },
                        onSetPrimary = { viewModel.setPrimary(guardian) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showAddDialog) {
        AddGuardianDialog(
            onAdd = { name, phone, rel ->
                viewModel.addGuardian(name, phone, rel)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun GuardianCard(
    guardian: GuardianEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onSetPrimary: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1A2A3A)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        if (guardian.isPrimary) Icons.Default.Star else Icons.Default.Person,
                        null,
                        tint = if (guardian.isPrimary) Color(0xFFFFD600) else Color(0xFF78909C),
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(guardian.guardianName, color = Color.White,
                            fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(guardian.relationship, color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp)
                    }
                }
                Switch(
                    checked = guardian.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!guardian.isPrimary) {
                    TextButton(onClick = onSetPrimary) {
                        Icon(Icons.Default.Star, null,
                            modifier = Modifier.size(14.dp), tint = Color(0xFFFFD600))
                        Spacer(Modifier.width(4.dp))
                        Text("Set Primary", color = Color(0xFFFFD600), fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null,
                        modifier = Modifier.size(14.dp), tint = Color(0xFFEF5350))
                    Spacer(Modifier.width(4.dp))
                    Text("Remove", color = Color(0xFFEF5350), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AddGuardianDialog(onAdd: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2A3A),
        title = { Text("Add Guardian", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GuardianField("Name", name, { name = it })
                GuardianField("Phone Number", phone, { phone = it },
                    keyboardType = KeyboardType.Phone)
                GuardianField("Relationship (e.g. Son, Daughter)", relationship,
                    { relationship = it })
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, phone, relationship) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
private fun GuardianField(
    label: String, value: String, onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color(0xFF4CAF50),
            focusedContainerColor = Color(0xFF0D1B2A),
            unfocusedContainerColor = Color(0xFF0D1B2A)
        )
    )
}
