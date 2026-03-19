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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var prenom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val passwordsMatch = password == confirmPassword || confirmPassword.isEmpty()

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

            Text("👋", fontSize = 72.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Créer un compte",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MeliColors.White
            )
            Text(
                "Rejoignez votre foyer",
                fontSize = 15.sp,
                color = MeliColors.White.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MeliShapes.bigCard)
                    .background(MeliColors.White)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Inscription",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeliColors.TextDark
                    )
                    Spacer(Modifier.height(20.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MeliColors.BgDark,
                        focusedLabelColor = MeliColors.BgDark,
                        cursorColor = MeliColors.BgDark
                    )

                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        label = { Text("Prénom") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = MeliColors.BgDark) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = MeliColors.BgDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MeliColors.BgDark) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = MeliColors.TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmer le mot de passe") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = if (passwordsMatch) MeliColors.BgDark else MeliColors.RedAlert) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = !passwordsMatch,
                        supportingText = if (!passwordsMatch) {
                            { Text("Les mots de passe ne correspondent pas") }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
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
                            viewModel.inscrire(email, password, prenom)
                        },
                        enabled = prenom.isNotBlank() && email.isNotBlank() &&
                                password.length >= 6 && passwordsMatch && !authState.isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MeliShapes.pill,
                        colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Créer mon compte", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "Déjà un compte ? Se connecter",
                    color = MeliColors.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}
