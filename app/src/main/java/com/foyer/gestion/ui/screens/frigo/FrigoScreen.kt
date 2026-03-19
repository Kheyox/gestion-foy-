package com.foyer.gestion.ui.screens.frigo

import androidx.compose.foundation.background
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
import com.foyer.gestion.data.model.ArticleFrigo
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.FrigoViewModel

private val categoriesFrigo = listOf("Frigo", "Congélateur", "Placard", "Fruits & Légumes", "Produits laitiers", "Viandes", "Boissons", "Autre")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrigoScreen(
    onBack: () -> Unit,
    viewModel: FrigoViewModel = hiltViewModel(),
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
                title = { Text("Au frigo", color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
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
                containerColor = MeliColors.CardBlue,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) { Icon(Icons.Default.Add, contentDescription = "Ajouter") }
        }
    ) { padding ->
        if (uiState.articles.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥘", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Frigo vide !", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour ajouter un aliment", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(uiState.articles, key = { it.id }) { article ->
                    ArticleCard(article, onSupprimer = { viewModel.supprimer(foyerId, article.id) })
                }
            }
        }
    }

    if (showDialog) {
        AjouterArticleDialog(
            onDismiss = { showDialog = false },
            onAjouter = { nom, quantite, categorie ->
                viewModel.ajouter(foyerId, nom, quantite, categorie, null)
                showDialog = false
            }
        )
    }
}

@Composable
private fun ArticleCard(article: ArticleFrigo, onSupprimer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(MeliColors.CardBlue)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(article.nom, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MeliColors.TextDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (article.quantite.isNotEmpty()) {
                            Text("Qté: ${article.quantite}", fontSize = 13.sp, color = MeliColors.TextMuted)
                        }
                        Text("📦 ${article.categorie}", fontSize = 13.sp, color = MeliColors.TextMuted)
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
private fun AjouterArticleDialog(onDismiss: () -> Unit, onAjouter: (String, String, String) -> Unit) {
    var nom by remember { mutableStateOf("") }
    var quantite by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf("Frigo") }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MeliShapes.bigCard,
        title = { Text("Ajouter un aliment", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom *") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantite, onValueChange = { quantite = it },
                    label = { Text("Quantité") }, singleLine = true,
                    shape = MeliShapes.input, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = it }) {
                    OutlinedTextField(
                        value = categorie, onValueChange = {}, readOnly = true,
                        label = { Text("Emplacement") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCat) },
                        shape = MeliShapes.input, modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categoriesFrigo.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categorie = cat; expandedCat = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(nom, quantite, categorie) },
                enabled = nom.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
