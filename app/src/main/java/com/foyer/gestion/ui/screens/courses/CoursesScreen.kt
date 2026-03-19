package com.foyer.gestion.ui.screens.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.ListeCourses
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.CoursesViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
        topBar = {
            TopAppBar(
                title = { Text("🛒 Courses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showNewListDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle liste")
            }
        }
    ) { padding ->
        if (uiState.listes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Aucune liste de courses", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Appuyez sur + pour créer une liste",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
            title = { Text("Nouvelle liste") },
            text = {
                OutlinedTextField(
                    value = nomNouvelleListe,
                    onValueChange = { nomNouvelleListe = it },
                    label = { Text("Nom de la liste") },
                    placeholder = { Text("Ex: Courses Lidl") },
                    singleLine = true,
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
                    enabled = nomNouvelleListe.isNotBlank()
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
    val dateFormat = SimpleDateFormat("dd MMM", Locale.FRENCH)
    val coches = liste.articles.count { it.estCoche }
    val total = liste.articles.size

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🛒", fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(liste.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (total == 0) "Liste vide" else "$coches/$total articles cochés",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (total > 0) {
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { if (total > 0) coches.toFloat() / total else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
