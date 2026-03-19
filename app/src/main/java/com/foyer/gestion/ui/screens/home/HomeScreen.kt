package com.foyer.gestion.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.viewmodel.AuthViewModel

private val BgDarkGreen = Color(0xFF0D2B25)
private val CardMint    = Color(0xFF8EDFD0)
private val CardYellow  = Color(0xFFF5D87A)
private val CardPink    = Color(0xFFF4A3A3)
private val CardBlue    = Color(0xFFB8D8E8)
private val CardTeal    = Color(0xFF9FE3CE)
private val CardPeach   = Color(0xFFF5C5A3)

data class MenuTile(
    val emoji: String,
    val titre: String,
    val couleur: Color,
    val onClick: () -> Unit
)

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
        MenuTile("🛒", "Courses", CardMint, onNavigateToCourses),
        MenuTile("✅", "Tâches", CardYellow, onNavigateToTaches),
        MenuTile("💰", "Budget", CardPink, onNavigateToBudget),
        MenuTile("🏠", "Foyer", CardBlue) {},
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDarkGreen)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foyer pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            authState.foyer?.nom ?: "Mon Foyer",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Invite button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { showCodeDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Code", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.width(10.dp))

                // Profile button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { showLogoutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        authState.utilisateur?.avatarEmoji ?: "👤",
                        fontSize = 18.sp
                    )
                }
            }

            // ── Greeting ─────────────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    "Bonjour ${authState.utilisateur?.prenom ?: ""} 👋",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Mes applications",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Grid ─────────────────────────────────────────────────
            val rows = tiles.chunked(2)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                rows.forEach { rowTiles ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowTiles.forEach { tile ->
                            MeliMeloTileCard(
                                tile = tile,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Si ligne impaire, remplir l'espace vide
                        if (rowTiles.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
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
private fun MeliMeloTileCard(tile: MenuTile, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .clip(RoundedCornerShape(24.dp))
            .background(tile.couleur)
            .clickable { tile.onClick() }
            .padding(18.dp)
    ) {
        // Titre en haut à gauche
        Text(
            text = tile.titre,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Grand emoji en bas à droite
        Text(
            text = tile.emoji,
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}
