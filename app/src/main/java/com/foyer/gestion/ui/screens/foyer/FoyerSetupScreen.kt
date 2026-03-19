package com.foyer.gestion.ui.screens.foyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel

@Composable
fun FoyerSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    var nomFoyer by remember { mutableStateOf("") }
    var codeInvitation by remember { mutableStateOf("") }
    var afficherCreer by remember { mutableStateOf(true) }

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

            Text("🏡", fontSize = 80.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Votre Foyer",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MeliColors.White
            )
            Text(
                "Créez votre foyer ou rejoignez-en un avec un code",
                fontSize = 15.sp,
                color = MeliColors.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            // Toggle créer / rejoindre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MeliShapes.pill)
                    .background(MeliColors.White.copy(alpha = 0.12f))
                    .padding(4.dp)
            ) {
                Row {
                    ToggleTab("➕ Créer", afficherCreer, Modifier.weight(1f)) { afficherCreer = true }
                    ToggleTab("👥 Rejoindre", !afficherCreer, Modifier.weight(1f)) { afficherCreer = false }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Formulaire card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MeliShapes.bigCard)
                    .background(MeliColors.White)
                    .padding(24.dp)
            ) {
                if (afficherCreer) {
                    Column {
                        Text("Créer un foyer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MeliColors.TextDark)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = nomFoyer,
                            onValueChange = { nomFoyer = it },
                            label = { Text("Nom du foyer") },
                            placeholder = { Text("Ex: Famille Dupont") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                if (nomFoyer.isNotBlank()) viewModel.creerFoyer(nomFoyer)
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
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearError(); viewModel.creerFoyer(nomFoyer) },
                            enabled = nomFoyer.isNotBlank() && !authState.isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = MeliShapes.pill,
                            colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Créer le foyer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column {
                        Text("Rejoindre un foyer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MeliColors.TextDark)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = codeInvitation,
                            onValueChange = { codeInvitation = it.uppercase().take(6) },
                            label = { Text("Code d'invitation") },
                            placeholder = { Text("Ex: AB12CD") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                if (codeInvitation.length == 6) viewModel.rejoindreParCode(codeInvitation)
                            }),
                            singleLine = true,
                            shape = MeliShapes.input,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { Text("${codeInvitation.length}/6 caractères") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MeliColors.BgDark,
                                focusedLabelColor = MeliColors.BgDark,
                                cursorColor = MeliColors.BgDark
                            )
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearError(); viewModel.rejoindreParCode(codeInvitation) },
                            enabled = codeInvitation.length == 6 && !authState.isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = MeliShapes.pill,
                            colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Rejoindre le foyer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            authState.error?.let { error ->
                Spacer(Modifier.height(16.dp))
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

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ToggleTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(MeliShapes.pill)
            .background(if (selected) MeliColors.White else Color.Transparent)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MeliColors.BgDark else MeliColors.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}
