package com.foyer.gestion.ui.screens.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.foyer.gestion.data.model.Note
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val foyerId = authViewModel.authState.collectAsState().value.foyerId ?: ""
    var showDialog by remember { mutableStateOf(false) }
    var noteEnEdition by remember { mutableStateOf<Note?>(null) }

    LaunchedEffect(foyerId) { if (foyerId.isNotEmpty()) viewModel.observer(foyerId) }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Notes", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
                onClick = { noteEnEdition = null; showDialog = true },
                containerColor = MeliColors.CardPink,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        if (uiState.notes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucune note", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour créer une note", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = { noteEnEdition = note; showDialog = true },
                        onSupprimer = { viewModel.supprimer(foyerId, note.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        NoteDialog(
            note = noteEnEdition,
            onDismiss = { showDialog = false },
            onSauvegarder = { titre, contenu ->
                if (noteEnEdition != null) {
                    viewModel.modifier(foyerId, noteEnEdition!!.copy(titre = titre, contenu = contenu))
                } else {
                    viewModel.creer(foyerId, titre, contenu)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onSupprimer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.CardPink)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.titre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeliColors.TextDark)
                    if (note.contenu.isNotEmpty()) {
                        Text(
                            note.contenu,
                            fontSize = 13.sp, color = MeliColors.TextMuted,
                            maxLines = 2
                        )
                    }
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
private fun NoteDialog(note: Note?, onDismiss: () -> Unit, onSauvegarder: (String, String) -> Unit) {
    var titre by remember { mutableStateOf(note?.titre ?: "") }
    var contenu by remember { mutableStateOf(note?.contenu ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text(if (note == null) "Nouvelle note" else "Modifier la note", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = titre, onValueChange = { titre = it },
                    label = { Text("Titre *") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenu, onValueChange = { contenu = it },
                    label = { Text("Contenu") }, minLines = 4, maxLines = 8,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSauvegarder(titre, contenu) },
                enabled = titre.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Sauvegarder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
