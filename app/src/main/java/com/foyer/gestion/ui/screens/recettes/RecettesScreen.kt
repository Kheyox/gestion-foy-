package com.foyer.gestion.ui.screens.recettes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.foyer.gestion.data.model.Recette
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.RecettesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecettesScreen(
    onBack: () -> Unit,
    viewModel: RecettesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val foyerId = authViewModel.authState.collectAsState().value.foyerId ?: ""
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(foyerId) { if (foyerId.isNotEmpty()) viewModel.observer(foyerId) }

    if (uiState.recetteSelectionnee != null) {
        RecetteDetailScreen(
            recette = uiState.recetteSelectionnee!!,
            onBack = { viewModel.selectionner(null) },
            onSupprimer = {
                viewModel.supprimer(foyerId, uiState.recetteSelectionnee!!.id)
                viewModel.selectionner(null)
            }
        )
        return
    }

    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = { Text("Recettes", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
            ) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        if (uiState.recettes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👨‍🍳", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Aucune recette", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour ajouter une recette", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.recettes, key = { it.id }) { recette ->
                    RecetteCard(recette, onClick = { viewModel.selectionner(recette) })
                }
            }
        }
    }

    if (showAddDialog) {
        AjouterRecetteDialog(
            onDismiss = { showAddDialog = false },
            onAjouter = { titre, ingredients, instructions, duree, portions ->
                viewModel.creer(foyerId, titre, ingredients, instructions, duree, portions)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun RecetteCard(recette: Recette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.CardYellow)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recette.titre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeliColors.TextDark)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (recette.dureeMinutes > 0) {
                    Text("⏱ ${recette.dureeMinutes} min", fontSize = 12.sp, color = MeliColors.TextMuted)
                }
                Text("🍽 ${recette.portions} portions", fontSize = 12.sp, color = MeliColors.TextMuted)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecetteDetailScreen(recette: Recette, onBack: () -> Unit, onSupprimer: () -> Unit) {
    Scaffold(
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = { Text(recette.titre, color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = MeliColors.White)
                    }
                },
                actions = {
                    IconButton(onClick = onSupprimer) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer", tint = MeliColors.CardPink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeliColors.BgDark)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (recette.dureeMinutes > 0) {
                    InfoChip("⏱ ${recette.dureeMinutes} min")
                }
                InfoChip("🍽 ${recette.portions} portions")
            }
            if (recette.ingredients.isNotEmpty()) {
                SectionCard("Ingrédients", recette.ingredients, MeliColors.CardMint)
            }
            if (recette.instructions.isNotEmpty()) {
                SectionCard("Instructions", recette.instructions, MeliColors.CardYellow)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(MeliShapes.pill)
            .background(MeliColors.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 13.sp, color = MeliColors.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionCard(titre: String, contenu: String, couleur: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(couleur)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MeliColors.TextDark)
            Spacer(Modifier.height(8.dp))
            Text(contenu, fontSize = 14.sp, color = MeliColors.TextDark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterRecetteDialog(
    onDismiss: () -> Unit,
    onAjouter: (String, String, String, Int, Int) -> Unit
) {
    var titre by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var duree by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Nouvelle recette", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = titre, onValueChange = { titre = it },
                    label = { Text("Titre *") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = duree, onValueChange = { duree = it.filter { c -> c.isDigit() } },
                        label = { Text("Durée (min)") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = portions, onValueChange = { portions = it.filter { c -> c.isDigit() } },
                        label = { Text("Portions") }, singleLine = true,
                        shape = MeliShapes.input, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ingredients, onValueChange = { ingredients = it },
                    label = { Text("Ingrédients") }, minLines = 3, maxLines = 6,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructions, onValueChange = { instructions = it },
                    label = { Text("Instructions") }, minLines = 3, maxLines = 8,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAjouter(titre, ingredients, instructions, duree.toIntOrNull() ?: 0, portions.toIntOrNull() ?: 4)
                },
                enabled = titre.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Créer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
