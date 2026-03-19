package com.foyer.gestion.ui.screens.foyer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏡", fontSize = 64.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "Votre Foyer",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            "Créez votre foyer ou rejoignez-en un avec un code",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Toggle
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = afficherCreer,
                onClick = { afficherCreer = true },
                label = { Text("Créer un foyer") },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            )
            FilterChip(
                selected = !afficherCreer,
                onClick = { afficherCreer = false },
                label = { Text("Rejoindre") },
                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (afficherCreer) {
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    viewModel.creerFoyer(nomFoyer)
                },
                enabled = nomFoyer.isNotBlank() && !authState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Créer le foyer")
                }
            }
        } else {
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
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("${codeInvitation.length}/6 caractères") }
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    viewModel.rejoindreParCode(codeInvitation)
                },
                enabled = codeInvitation.length == 6 && !authState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Rejoindre le foyer")
                }
            }
        }

        authState.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(
                    error, modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
