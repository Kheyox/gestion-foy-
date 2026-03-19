package com.foyer.gestion.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.viewmodel.AuthViewModel

// ── Palette ──────────────────────────────────────────────────────────────────
private val BgDark    = Color(0xFF0D2B25)
private val CardMint  = Color(0xFF7ED8C0)
private val CardYellow= Color(0xFFF5D87A)
private val CardPink  = Color(0xFFF4A3A3)
private val CardBlue  = Color(0xFFB4D9F0)
private val CardTeal  = Color(0xFF9FE3CE)
private val CardPeach = Color(0xFFF5C5A3)
private val CardLavender = Color(0xFFCBB8F0)
private val CardSage  = Color(0xFFB8D8B0)

data class MenuTile(
    val emoji: String,
    val titre: String,
    val couleur: Color,
    val gradient: Color? = null,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToTaches: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCalendrier: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToFrigo: () -> Unit = {},
    onNavigateToRecettes: () -> Unit = {},
    onNavigateToAnniversaires: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCodeDialog by remember { mutableStateOf(false) }

    val appName = authState.foyer?.nom ?: "Mon Foyer"

    val tiles = listOf(
        MenuTile("🛒", "Courses",    CardMint,     null,         onNavigateToCourses),
        MenuTile("📅", "Calendrier", CardYellow,   null,         onNavigateToCalendrier),
        MenuTile("📝", "Notes",      CardPink,     null,         onNavigateToNotes),
        MenuTile("🥘", "Au frigo",   CardBlue,     null,         onNavigateToFrigo),
        MenuTile("✅", "Tâches",     CardTeal,     null,         onNavigateToTaches),
        MenuTile("👨‍🍳","Recettes",   CardYellow,   null,         onNavigateToRecettes),
        MenuTile("💰", "Budget",     CardPeach,    null,         onNavigateToBudget),
        MenuTile("🎂", "Anniversaires", CardLavender, null,      onNavigateToAnniversaires),
        MenuTile("🐾", "Animaux",    CardSage,     null)         {},
        MenuTile("💆", "Bien-être",  CardMint,     null)         {},
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App name – style cursif simulé
                Column {
                    Text(
                        text = appName.lowercase().let {
                            if (it.contains(" ")) {
                                it.split(" ").first()
                            } else it
                        },
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 26.sp
                    )
                    val second = appName.lowercase().split(" ").getOrNull(1)
                    if (second != null) {
                        Text(
                            text = second,
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 26.sp
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Sparkle icon (décoration)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .clickable { showCodeDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 18.sp)
                }

                Spacer(Modifier.width(10.dp))

                // Profile
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                        .clickable { showLogoutDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    val avatar = authState.utilisateur?.avatarEmoji
                    if (avatar != null && avatar != "👤") {
                        Text(avatar, fontSize = 18.sp)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Grid ─────────────────────────────────────────────────
            val rows = tiles.chunked(2)
            Column(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowTiles ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowTiles.forEach { tile ->
                            MeliTileCard(tile = tile, modifier = Modifier.weight(1f))
                        }
                        if (rowTiles.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(36.dp))
        }
    }

    // ── Dialog déconnexion ────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF163D30),
            title = { Text("Se déconnecter ?", color = Color.White) },
            text = { Text("Voulez-vous vraiment vous déconnecter ?", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deconnecter(); showLogoutDialog = false }) {
                    Text("Oui", color = CardPink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // ── Dialog code invitation ────────────────────────────────────────────────
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            containerColor = Color(0xFF163D30),
            title = { Text("Code d'invitation", color = Color.White) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Partagez ce code pour inviter un membre :", color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardMint.copy(alpha = 0.2f)
                    ) {
                        Text(
                            authState.foyer?.codeInvitation ?: "------",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = CardMint,
                            letterSpacing = 8.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodeDialog = false }) {
                    Text("Fermer", color = CardMint)
                }
            }
        )
    }
}

@Composable
private fun MeliTileCard(tile: MenuTile, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(0.88f)
            .clip(RoundedCornerShape(22.dp))
            .background(tile.couleur)
            .clickable { tile.onClick() }
    ) {
        // Titre en haut à gauche
        Text(
            text = tile.titre,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            lineHeight = 22.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 16.dp, top = 16.dp)
                .widthIn(max = 120.dp)
        )

        // Grand emoji en bas à droite
        Text(
            text = tile.emoji,
            fontSize = 62.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 4.dp)
        )
    }
}
