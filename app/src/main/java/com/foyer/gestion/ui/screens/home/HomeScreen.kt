package com.foyer.gestion.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.viewmodel.AuthViewModel

data class MenuTile(
    val emoji: String,
    val titre: String,
    val description: String,
    val icon: ImageVector,
    val couleur: Color,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToTaches: () -> Unit,
    onNavigateToBudget: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }

    val tiles = listOf(
        MenuTile("🛒", "Courses", "Listes de courses partagées", Icons.Default.ShoppingCart,
            Color(0xFF4CAF50), onNavigateToCourses),
        MenuTile("✅", "Tâches", "Corvées et tâches du foyer", Icons.Default.CheckCircle,
            Color(0xFF2196F3), onNavigateToTaches),
        MenuTile("💰", "Budget", "Suivi des dépenses", Icons.Default.AccountBalance,
            Color(0xFFFF9800), onNavigateToBudget),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "🏠 ${authState.foyer?.nom ?: "Mon Foyer"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Bonjour ${authState.utilisateur?.prenom ?: ""} ${authState.utilisateur?.avatarEmoji ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCodeDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Code invitation")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Déconnexion")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Membres du foyer
            authState.foyer?.let { foyer ->
                if (foyer.membres.size > 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${foyer.membres.size} membres dans le foyer",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Grille des fonctionnalités
            Text(
                "Que voulez-vous faire ?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tiles) { tile ->
                    HomeTileCard(tile = tile)
                }
            }
        }
    }

    // Dialog déconnexion
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Se déconnecter ?") },
            text = { Text("Voulez-vous vraiment vous déconnecter ?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deconnecter()
                    showLogoutDialog = false
                }) { Text("Oui", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Annuler") }
            }
        )
    }

    // Dialog code invitation
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            title = { Text("Code d'invitation") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Partagez ce code pour inviter un membre :")
                    Spacer(Modifier.height(16.dp))
                    Text(
                        authState.foyer?.codeInvitation ?: "------",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 8.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) { Text("Fermer") }
            }
        )
    }
}

@Composable
private fun HomeTileCard(tile: MenuTile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { tile.onClick() },
        colors = CardDefaults.cardColors(containerColor = tile.couleur.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(tile.emoji, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                tile.titre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tile.couleur
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tile.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
