package com.foyer.gestion.ui.screens.calendrier

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.Evenement
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.CalendrierViewModel
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendrierScreen(
    onBack: () -> Unit,
    viewModel: CalendrierViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val foyerId = authViewModel.authState.collectAsState().value.foyerId ?: ""
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(foyerId) { if (foyerId.isNotEmpty()) viewModel.observer(foyerId) }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Calendrier", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
                onClick = { showDialog = true },
                containerColor = MeliColors.CardYellow,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        if (uiState.evenements.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucun événement", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour ajouter", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.evenements, key = { it.id }) { evt ->
                    EvenementCard(evt, onSupprimer = { viewModel.supprimer(foyerId, evt.id) })
                }
            }
        }
    }

    if (showDialog) {
        AjouterEvenementDialog(
            onDismiss = { showDialog = false },
            onAjouter = { titre, description ->
                viewModel.creer(foyerId, titre, description, Timestamp.now(), null)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EvenementCard(evt: Evenement, onSupprimer: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.CardYellow)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(evt.titre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeliColors.TextDark)
                    if (evt.description.isNotEmpty()) {
                        Text(evt.description, fontSize = 13.sp, color = MeliColors.TextMuted)
                    }
                    Text(
                        "📅 ${fmt.format(evt.dateDebut.toDate())}",
                        fontSize = 12.sp, color = MeliColors.TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer",
                        tint = MeliColors.BgDark.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterEvenementDialog(onDismiss: () -> Unit, onAjouter: (String, String) -> Unit) {
    var titre by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Nouvel événement", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = titre, onValueChange = { titre = it },
                    label = { Text("Titre *") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") }, maxLines = 3,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(titre, description) },
                enabled = titre.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
