package com.safex.app.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safex.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSkipForNow: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = CleanSlate
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo/Icon
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ShieldBlueContainer
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = ShieldBlue,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "SAFEx Guard",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ShieldBlue
            )

            Text(
                text = if (isLogin) "Welcome Back" else "Create Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalBlack
            )

            Text(
                text = "Your personal safety companion",
                fontSize = 14.sp,
                color = MutedSlate,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name (only for signup)
                    if (!isLogin) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, null, tint = ShieldBlue)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ShieldBlue,
                                unfocusedBorderColor = BorderSubtle
                            )
                        )
                    }

                    // Phone Number
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null, tint = ShieldBlue)
                        },
                        prefix = { Text("+91 ", color = MutedSlate) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShieldBlue,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = ShieldBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility 
                                    else Icons.Default.VisibilityOff,
                                    null,
                                    tint = MutedSlate
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) 
                            VisualTransformation.None 
                        else 
                            PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShieldBlue,
                            unfocusedBorderColor = BorderSubtle
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit Button
                    Button(
                        onClick = onAuthSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShieldBlue,
                            contentColor = Color.White
                        ),
                        enabled = phoneNumber.isNotBlank() && password.isNotBlank() &&
                                (isLogin || fullName.isNotBlank())
                    ) {
                        Text(
                            if (isLogin) "Login" else "Sign Up",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Toggle Login/Signup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLogin) "Don't have an account?" else "Already have an account?",
                            fontSize = 13.sp,
                            color = MutedSlate
                        )
                        TextButton(onClick = { isLogin = !isLogin }) {
                            Text(
                                if (isLogin) "Sign Up" else "Login",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShieldBlue
                            )
                        }
                    }
                }
            }

            // Skip for now
            TextButton(onClick = onSkipForNow) {
                Text(
                    "Skip for now",
                    fontSize = 14.sp,
                    color = SubduedSlate,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ShieldBlueContainer,
                border = BorderStroke(1.dp, ShieldBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = ShieldBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Your data is stored securely on your device. We never share your information.",
                        fontSize = 12.sp,
                        color = ShieldBlue,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
