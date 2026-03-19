package com.foyer.gestion.ui.screens.taches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.foyer.gestion.data.model.PrioriteTache
import com.foyer.gestion.data.model.StatutTache
import com.foyer.gestion.data.model.Tache
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.TachesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TachesScreen(
    onBack: () -> Unit,
    viewModel: TachesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val foyerId = authState.foyerId ?: ""

    var showAddDialog by remember { mutableStateOf(false) }
    var filtreStatut by remember { mutableStateOf<StatutTache?>(null) }

    LaunchedEffect(foyerId) {
        if (foyerId.isNotEmpty()) viewModel.observerTaches(foyerId)
    }

    val tachesFiltrees = if (filtreStatut == null) uiState.taches
    else uiState.taches.filter { it.statut == filtreStatut!!.name }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Tâches", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
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
                onClick = { showAddDialog = true },
                containerColor = MeliColors.CardYellow,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle tâche")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtres
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MeliFilterChip("Toutes", filtreStatut == null) { filtreStatut = null }
                MeliFilterChip("À faire", filtreStatut == StatutTache.A_FAIRE) {
                    filtreStatut = if (filtreStatut == StatutTache.A_FAIRE) null else StatutTache.A_FAIRE
                }
                MeliFilterChip("Terminées", filtreStatut == StatutTache.TERMINEE) {
                    filtreStatut = if (filtreStatut == StatutTache.TERMINEE) null else StatutTache.TERMINEE
                }
            }

            if (tachesFiltrees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 64.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Aucune tâche", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Appuie sur + pour créer une tâche", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tachesFiltrees, key = { it.id }) { tache ->
                        TacheCard(
                            tache = tache,
                            onChangerStatut = { nouveau -> viewModel.changerStatut(foyerId, tache.id, nouveau) },
                            onSupprimer = { viewModel.supprimerTache(foyerId, tache.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AjouterTacheDialog(
            membres = authState.foyer?.membres ?: emptyList(),
            currentUserId = authState.user?.uid ?: "",
            onDismiss = { showAddDialog = false },
            onAjouter = { titre, description, priorite, assigneA ->
                viewModel.creerTache(foyerId, titre, description, priorite, assigneA)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun MeliFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MeliColors.CardYellow,
            selectedLabelColor = MeliColors.TextDark,
            containerColor = MeliColors.White.copy(alpha = 0.12f),
            labelColor = MeliColors.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MeliColors.White.copy(alpha = 0.2f),
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun TacheCard(
    tache: Tache,
    onChangerStatut: (StatutTache) -> Unit,
    onSupprimer: () -> Unit
) {
    val statut = runCatching { StatutTache.valueOf(tache.statut) }.getOrDefault(StatutTache.A_FAIRE)
    val priorite = runCatching { PrioriteTache.valueOf(tache.priorite) }.getOrDefault(PrioriteTache.NORMALE)

    val (prioriteColor, prioriteLabel) = when (priorite) {
        PrioriteTache.HAUTE -> MeliColors.CardPink to "🔴 Haute"
        PrioriteTache.NORMALE -> MeliColors.CardBlue to "🔵 Normale"
        PrioriteTache.BASSE -> MeliColors.CardMint to "🟢 Basse"
    }

    val isTerminee = statut == StatutTache.TERMINEE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(if (isTerminee) MeliColors.White.copy(alpha = 0.55f) else MeliColors.White)
    ) {
        // Barre colorée à gauche selon priorité
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(prioriteColor)
                .align(Alignment.CenterStart)
        )

        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isTerminee,
                    onCheckedChange = { checked ->
                        onChangerStatut(if (checked) StatutTache.TERMINEE else StatutTache.A_FAIRE)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MeliColors.BgDark,
                        uncheckedColor = MeliColors.BgDark.copy(alpha = 0.4f)
                    )
                )
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tache.titre,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isTerminee) MeliColors.TextMuted else MeliColors.TextDark
                    )
                    if (tache.description.isNotEmpty()) {
                        Text(
                            tache.description,
                            fontSize = 13.sp,
                            color = MeliColors.TextMuted
                        )
                    }
                }
                IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer",
                        tint = MeliColors.CardPink, modifier = Modifier.size(18.dp))
                }
            }

            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(MeliShapes.pill)
                        .background(prioriteColor.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(prioriteLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MeliColors.TextDark)
                }

                val statutLabel = when (statut) {
                    StatutTache.A_FAIRE -> "À faire"
                    StatutTache.EN_COURS -> "En cours ⏳"
                    StatutTache.TERMINEE -> "Terminée ✓"
                }
                TextButton(
                    onClick = {
                        val prochain = when (statut) {
                            StatutTache.A_FAIRE -> StatutTache.EN_COURS
                            StatutTache.EN_COURS -> StatutTache.TERMINEE
                            StatutTache.TERMINEE -> StatutTache.A_FAIRE
                        }
                        onChangerStatut(prochain)
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text(statutLabel, fontSize = 11.sp, color = MeliColors.BgDark)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterTacheDialog(
    membres: List<String>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onAjouter: (String, String, String, String) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priorite by remember { mutableStateOf(PrioriteTache.NORMALE.name) }
    var assigneA by remember { mutableStateOf(currentUserId) }
    var expandedPriorite by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Nouvelle tâche", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Titre *") },
                    singleLine = true,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedPriorite, onExpandedChange = { expandedPriorite = it }) {
                    OutlinedTextField(
                        value = when (priorite) {
                            PrioriteTache.HAUTE.name -> "🔴 Haute"
                            PrioriteTache.BASSE.name -> "🟢 Basse"
                            else -> "🔵 Normale"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPriorite) },
                        shape = MeliShapes.input,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedPriorite, onDismissRequest = { expandedPriorite = false }) {
                        listOf("🔴 Haute" to PrioriteTache.HAUTE.name, "🔵 Normale" to PrioriteTache.NORMALE.name, "🟢 Basse" to PrioriteTache.BASSE.name).forEach { (label, value) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = { priorite = value; expandedPriorite = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(titre, description, priorite, assigneA) },
                enabled = titre.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
