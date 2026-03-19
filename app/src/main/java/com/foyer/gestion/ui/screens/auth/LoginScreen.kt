package com.foyer.gestion.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MeliColors.BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(48.dp))

            // Logo
            Text("🏠", fontSize = 72.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Mon Foyer",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MeliColors.White
            )
            Text(
                "Gérez votre foyer ensemble",
                fontSize = 15.sp,
                color = MeliColors.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            // Carte formulaire
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MeliShapes.bigCard)
                    .background(MeliColors.White)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Connexion",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeliColors.TextDark
                    )
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MeliColors.BgDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeliColors.BgDark,
                            focusedLabelColor = MeliColors.BgDark,
                            cursorColor = MeliColors.BgDark
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeliColors.BgDark) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MeliColors.TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank() && password.isNotBlank()) viewModel.connecter(email, password)
                        }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeliColors.BgDark,
                            focusedLabelColor = MeliColors.BgDark,
                            cursorColor = MeliColors.BgDark
                        )
                    )

                    authState.error?.let { error ->
                        Spacer(Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MeliShapes.input)
                                .background(MeliColors.CardPink.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Text(error, color = MeliColors.RedAlert, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.connecter(email, password)
                        },
                        enabled = email.isNotBlank() && password.isNotBlank() && !authState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MeliShapes.pill,
                        colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Se connecter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    "Pas encore de compte ? S'inscrire",
                    color = MeliColors.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
