package com.safex.app.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var age by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("INDEPENDENT") }
    var email by remember { mutableStateOf("") }
    
    // Emergency contacts
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var childName by remember { mutableStateOf("") }
    var childPhone by remember { mutableStateOf("") }
    var contact1Name by remember { mutableStateOf("") }
    var contact1Phone by remember { mutableStateOf("") }
    var contact2Name by remember { mutableStateOf("") }
    var contact2Phone by remember { mutableStateOf("") }
    
    // Police station
    var policeStation by remember { mutableStateOf("") }
    var policePhone by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CleanSlate,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Complete Your Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShieldBlue
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = { currentStep / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = ShieldBlue,
                trackColor = BorderSubtle,
            )

            Text(
                "Step $currentStep of 3",
                fontSize = 13.sp,
                color = MutedSlate,
                fontWeight = FontWeight.Medium
            )

            when (currentStep) {
                1 -> PersonalInfoStep(
                    age = age,
                    onAgeChange = { age = it },
                    userType = userType,
                    onUserTypeChange = { userType = it },
                    email = email,
                    onEmailChange = { email = it }
                )
                2 -> EmergencyContactsStep(
                    userType = userType,
                    parentName = parentName,
                    onParentNameChange = { parentName = it },
                    parentPhone = parentPhone,
                    onParentPhoneChange = { parentPhone = it },
                    childName = childName,
                    onChildNameChange = { childName = it },
                    childPhone = childPhone,
                    onChildPhoneChange = { childPhone = it },
                    contact1Name = contact1Name,
                    onContact1NameChange = { contact1Name = it },
                    contact1Phone = contact1Phone,
                    onContact1PhoneChange = { contact1Phone = it },
                    contact2Name = contact2Name,
                    onContact2NameChange = { contact2Name = it },
                    contact2Phone = contact2Phone,
                    onContact2PhoneChange = { contact2Phone = it }
                )
                3 -> PoliceStationStep(
                    policeStation = policeStation,
                    onPoliceStationChange = { policeStation = it },
                    policePhone = policePhone,
                    onPolicePhoneChange = { policePhone = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ShieldBlue
                        ),
                        border = BorderStroke(1.dp, ShieldBlue)
                    ) {
                        Text(
                            "Back",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShieldBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (currentStep < 3) "Next" else "Complete Setup",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Skip for now",
                    fontSize = 14.sp,
                    color = SubduedSlate,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PersonalInfoStep(
    age: String,
    onAgeChange: (String) -> Unit,
    userType: String,
    onUserTypeChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Personal Information",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalBlack
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Age
                OutlinedTextField(
                    value = age,
                    onValueChange = onAgeChange,
                    label = { Text("Age") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, null, tint = ShieldBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldBlue,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                // User Type
                Text(
                    "I am a:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "PARENT" to "Parent (I have children to protect)",
                        "CHILD" to "Child/Student (Under 18 or student)",
                        "INDEPENDENT" to "Independent Adult"
                    ).forEach { (type, label) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = userType == type,
                                    onClick = { onUserTypeChange(type) }
                                ),
                            shape = RoundedCornerShape(10.dp),
                            color = if (userType == type) ShieldBlueContainer else CleanSlate,
                            border = BorderStroke(
                                1.dp,
                                if (userType == type) ShieldBlue else BorderSubtle
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = userType == type,
                                    onClick = { onUserTypeChange(type) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = ShieldBlue
                                    )
                                )
                                Text(
                                    label,
                                    fontSize = 14.sp,
                                    color = CharcoalBlack,
                                    fontWeight = if (userType == type) 
                                        FontWeight.SemiBold 
                                    else 
                                        FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Email (optional)
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, null, tint = ShieldBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldBlue,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            }
        }
    }
}

@Composable
private fun EmergencyContactsStep(
    userType: String,
    parentName: String,
    onParentNameChange: (String) -> Unit,
    parentPhone: String,
    onParentPhoneChange: (String) -> Unit,
    childName: String,
    onChildNameChange: (String) -> Unit,
    childPhone: String,
    onChildPhoneChange: (String) -> Unit,
    contact1Name: String,
    onContact1NameChange: (String) -> Unit,
    contact1Phone: String,
    onContact1PhoneChange: (String) -> Unit,
    contact2Name: String,
    onContact2NameChange: (String) -> Unit,
    contact2Phone: String,
    onContact2PhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Emergency Contacts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalBlack
        )

        Text(
            "These contacts will be notified in case of emergencies",
            fontSize = 13.sp,
            color = MutedSlate,
            lineHeight = 18.sp
        )

        // Parent contact (for CHILD type)
        if (userType == "CHILD") {
            ContactCard(
                title = "Parent/Guardian Contact",
                name = parentName,
                onNameChange = onParentNameChange,
                phone = parentPhone,
                onPhoneChange = onParentPhoneChange,
                icon = Icons.Default.FamilyRestroom,
                required = true
            )
        }

        // Child contact (for PARENT type)
        if (userType == "PARENT") {
            ContactCard(
                title = "Child Contact",
                name = childName,
                onNameChange = onChildNameChange,
                phone = childPhone,
                onPhoneChange = onChildPhoneChange,
                icon = Icons.Default.ChildCare,
                required = false
            )
        }

        // Emergency Contact 1
        ContactCard(
            title = "Emergency Contact 1",
            name = contact1Name,
            onNameChange = onContact1NameChange,
            phone = contact1Phone,
            onPhoneChange = onContact1PhoneChange,
            icon = Icons.Default.ContactPhone,
            required = false
        )

        // Emergency Contact 2
        ContactCard(
            title = "Emergency Contact 2",
            name = contact2Name,
            onNameChange = onContact2NameChange,
            phone = contact2Phone,
            onPhoneChange = onContact2PhoneChange,
            icon = Icons.Default.ContactPhone,
            required = false
        )
    }
}

@Composable
private fun ContactCard(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    required: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceWhite,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(icon, null, tint = ShieldBlue, modifier = Modifier.size(22.dp))
                Text(
                    title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CharcoalBlack
                )
                if (required) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AlertCrimsonContainer
                    ) {
                        Text(
                            "Required",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertCrimson,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldBlue,
                    unfocusedBorderColor = BorderSubtle
                )
            )

            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Phone Number") },
                prefix = { Text("+91 ", color = MutedSlate) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShieldBlue,
                    unfocusedBorderColor = BorderSubtle
                )
            )
        }
    }
}

@Composable
private fun PoliceStationStep(
    policeStation: String,
    onPoliceStationChange: (String) -> Unit,
    policePhone: String,
    onPolicePhoneChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Nearest Police Station",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CharcoalBlack
        )

        Text(
            "This information helps us assist you better in emergencies",
            fontSize = 13.sp,
            color = MutedSlate,
            lineHeight = 18.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceWhite,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.LocalPolice,
                        null,
                        tint = ShieldBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Police Station Details",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalBlack
                    )
                }

                OutlinedTextField(
                    value = policeStation,
                    onValueChange = onPoliceStationChange,
                    label = { Text("Police Station Name/Address (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, null, tint = ShieldBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldBlue,
                        unfocusedBorderColor = BorderSubtle
                    )
                )

                OutlinedTextField(
                    value = policePhone,
                    onValueChange = onPolicePhoneChange,
                    label = { Text("Police Station Phone (Optional)") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, null, tint = ShieldBlue)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShieldBlue,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TrustEmeraldContainer,
            border = BorderStroke(1.dp, TrustEmeraldBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = TrustEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "You're all set! Your profile helps us provide better safety features.",
                    fontSize = 12.sp,
                    color = TrustEmerald,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
