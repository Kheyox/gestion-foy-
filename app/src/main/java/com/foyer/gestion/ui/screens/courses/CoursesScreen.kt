package com.foyer.gestion.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.foyer.gestion.data.model.ListeCourses
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.CoursesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onNavigateToListe: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: CoursesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val foyerId = authState.foyerId ?: ""

    var showNewListDialog by remember { mutableStateOf(false) }
    var nomNouvelleListe by remember { mutableStateOf("") }

    LaunchedEffect(foyerId) {
        if (foyerId.isNotEmpty()) viewModel.observerListes(foyerId)
    }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Courses", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = MeliColors.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeliColors.BgDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewListDialog = true },
                containerColor = MeliColors.CardMint,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle liste")
            }
        }
    ) { padding ->
        if (uiState.listes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucune liste de courses", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour créer une liste", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.listes, key = { it.id }) { liste ->
                    ListeCoursesCard(
                        liste = liste,
                        onClick = { onNavigateToListe(liste.id, liste.nom) },
                        onDelete = { viewModel.supprimerListe(foyerId, liste.id) }
                    )
                }
            }
        }
    }

    if (showNewListDialog) {
        AlertDialog(
            onDismissRequest = { showNewListDialog = false; nomNouvelleListe = "" },
            shape = MeliShapes.bigCard,
            title = { Text("Nouvelle liste", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nomNouvelleListe,
                    onValueChange = { nomNouvelleListe = it },
                    label = { Text("Nom de la liste") },
                    placeholder = { Text("Ex: Courses Lidl") },
                    singleLine = true,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nomNouvelleListe.isNotBlank()) {
                            viewModel.creerListe(foyerId, nomNouvelleListe)
                            showNewListDialog = false
                            nomNouvelleListe = ""
                        }
                    },
                    enabled = nomNouvelleListe.isNotBlank(),
                    shape = MeliShapes.pill,
                    colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
                ) { Text("Créer") }
            },
            dismissButton = {
                TextButton(onClick = { showNewListDialog = false; nomNouvelleListe = "" }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ListeCoursesCard(
    liste: ListeCourses,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val coches = liste.articles.count { it.estCoche }
    val total = liste.articles.size
    val progress = if (total > 0) coches.toFloat() / total else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.White)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icône
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MeliColors.CardMint),
                contentAlignment = Alignment.Center
            ) {
                Text("🛒", fontSize = 24.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    liste.nom,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MeliColors.TextDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (total == 0) "Liste vide" else "$coches/$total articles cochés",
                    fontSize = 13.sp,
                    color = MeliColors.TextMuted
                )
                if (total > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(MeliShapes.pill),
                        color = MeliColors.BgDark,
                        trackColor = MeliColors.CardMint.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Supprimer",
                    tint = MeliColors.CardPink,
                    modifier = Modifier.size(20.dp)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MeliColors.TextMuted
            )
        }
    }
}
