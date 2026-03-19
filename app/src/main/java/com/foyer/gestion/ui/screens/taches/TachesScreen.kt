package com.foyer.gestion.ui.screens.taches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.PrioriteTache
import com.foyer.gestion.data.model.StatutTache
import com.foyer.gestion.data.model.Tache
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
        topBar = {
            TopAppBar(
                title = { Text("✅ Tâches") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle tâche")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtres
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtreStatut == null,
                    onClick = { filtreStatut = null },
                    label = { Text("Toutes") }
                )
                FilterChip(
                    selected = filtreStatut == StatutTache.A_FAIRE,
                    onClick = { filtreStatut = if (filtreStatut == StatutTache.A_FAIRE) null else StatutTache.A_FAIRE },
                    label = { Text("À faire") }
                )
                FilterChip(
                    selected = filtreStatut == StatutTache.TERMINEE,
                    onClick = { filtreStatut = if (filtreStatut == StatutTache.TERMINEE) null else StatutTache.TERMINEE },
                    label = { Text("Terminées") }
                )
            }

            if (tachesFiltrees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Aucune tâche", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Appuyez sur + pour créer une tâche",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tachesFiltrees, key = { it.id }) { tache ->
                        TacheCard(
                            tache = tache,
                            onChangerStatut = { nouveau ->
                                viewModel.changerStatut(foyerId, tache.id, nouveau)
                            },
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
private fun TacheCard(
    tache: Tache,
    onChangerStatut: (StatutTache) -> Unit,
    onSupprimer: () -> Unit
) {
    val statut = runCatching { StatutTache.valueOf(tache.statut) }.getOrDefault(StatutTache.A_FAIRE)
    val priorite = runCatching { PrioriteTache.valueOf(tache.priorite) }.getOrDefault(PrioriteTache.NORMALE)

    val (prioriteColor, prioriteLabel) = when (priorite) {
        PrioriteTache.HAUTE -> Color(0xFFE53935) to "Haute"
        PrioriteTache.NORMALE -> Color(0xFF1976D2) to "Normale"
        PrioriteTache.BASSE -> Color(0xFF388E3C) to "Basse"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (statut == StatutTache.TERMINEE)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Checkbox statut
                Checkbox(
                    checked = statut == StatutTache.TERMINEE,
                    onCheckedChange = { checked ->
                        onChangerStatut(if (checked) StatutTache.TERMINEE else StatutTache.A_FAIRE)
                    }
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tache.titre,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (tache.description.isNotEmpty()) {
                        Text(
                            tache.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                IconButton(onClick = onSupprimer) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(prioriteLabel, style = MaterialTheme.typography.labelSmall) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = prioriteColor.copy(alpha = 0.12f)),
                    border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = prioriteColor.copy(alpha = 0.4f))
                )

                val statutLabel = when (statut) {
                    StatutTache.A_FAIRE -> "À faire"
                    StatutTache.EN_COURS -> "En cours"
                    StatutTache.TERMINEE -> "Terminée ✓"
                }
                AssistChip(
                    onClick = {
                        val prochain = when (statut) {
                            StatutTache.A_FAIRE -> StatutTache.EN_COURS
                            StatutTache.EN_COURS -> StatutTache.TERMINEE
                            StatutTache.TERMINEE -> StatutTache.A_FAIRE
                        }
                        onChangerStatut(prochain)
                    },
                    label = { Text(statutLabel, style = MaterialTheme.typography.labelSmall) }
                )
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
        title = { Text("Nouvelle tâche") },
        text = {
            Column {
                OutlinedTextField(
                    value = titre,
                    onValueChange = { titre = it },
                    label = { Text("Titre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedPriorite,
                    onExpandedChange = { expandedPriorite = it }
                ) {
                    OutlinedTextField(
                        value = when (priorite) {
                            PrioriteTache.HAUTE.name -> "Haute"
                            PrioriteTache.BASSE.name -> "Basse"
                            else -> "Normale"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priorité") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedPriorite) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriorite,
                        onDismissRequest = { expandedPriorite = false }
                    ) {
                        listOf("Haute" to PrioriteTache.HAUTE.name,
                               "Normale" to PrioriteTache.NORMALE.name,
                               "Basse" to PrioriteTache.BASSE.name).forEach { (label, value) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { priorite = value; expandedPriorite = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(titre, description, priorite, assigneA) },
                enabled = titre.isNotBlank()
            ) { Text("Créer") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
