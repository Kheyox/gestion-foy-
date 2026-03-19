package com.foyer.gestion.ui.screens.courses

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.ArticleCourse
import com.foyer.gestion.data.model.Categories
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.CoursesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListeCoursesScreen(
    listeId: String,
    listeNom: String,
    onBack: () -> Unit,
    viewModel: CoursesViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val foyerId = authState.foyerId ?: ""

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteCochesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(foyerId, listeId) {
        if (foyerId.isNotEmpty()) viewModel.observerArticles(foyerId, listeId)
    }

    val articlesNonCoches = uiState.articles.filter { !it.estCoche }
    val articlesCoches = uiState.articles.filter { it.estCoche }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listeNom) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (articlesCoches.isNotEmpty()) {
                        IconButton(onClick = { showDeleteCochesDialog = true }) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Vider cochés")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter article")
            }
        }
    ) { padding ->
        if (uiState.articles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛍️", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Liste vide", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Appuyez sur + pour ajouter un article",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (articlesNonCoches.isNotEmpty()) {
                    stickyHeader {
                        Surface(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "À acheter (${articlesNonCoches.size})",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    items(articlesNonCoches, key = { it.id }) { article ->
                        ArticleItem(
                            article = article,
                            onCoche = { viewModel.cocherArticle(foyerId, listeId, article.id, true) },
                            onSupprimer = { viewModel.supprimerArticle(foyerId, listeId, article.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }

                if (articlesCoches.isNotEmpty()) {
                    stickyHeader {
                        Surface(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Dans le panier (${articlesCoches.size})",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    items(articlesCoches, key = { it.id }) { article ->
                        ArticleItem(
                            article = article,
                            onCoche = { viewModel.cocherArticle(foyerId, listeId, article.id, false) },
                            onSupprimer = { viewModel.supprimerArticle(foyerId, listeId, article.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AjouterArticleDialog(
            onDismiss = { showAddDialog = false },
            onAjouter = { nom, quantite, unite, categorie ->
                viewModel.ajouterArticle(foyerId, listeId, nom, quantite, unite, categorie)
                showAddDialog = false
            }
        )
    }

    if (showDeleteCochesDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCochesDialog = false },
            title = { Text("Vider les articles cochés ?") },
            text = { Text("Les ${articlesCoches.size} articles cochés seront supprimés.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.viderCoches(foyerId, listeId)
                        showDeleteCochesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Vider") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCochesDialog = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun ArticleItem(
    article: ArticleCourse,
    onCoche: () -> Unit,
    onSupprimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        if (article.estCoche) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        label = "bg"
    )

    Surface(color = bgColor, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = article.estCoche,
                onCheckedChange = { onCoche() }
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    article.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (article.estCoche) TextDecoration.LineThrough else null,
                    color = if (article.estCoche) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.onSurface
                )
                val details = buildString {
                    if (article.quantite.isNotEmpty() && article.quantite != "1") append(article.quantite)
                    if (article.unite.isNotEmpty()) append(" ${article.unite}")
                    if (article.categorie.isNotEmpty() && article.categorie != "Autre") {
                        if (isNotEmpty()) append(" • ")
                        append(article.categorie)
                    }
                }
                if (details.isNotEmpty()) {
                    Text(
                        details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onSupprimer) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterArticleDialog(
    onDismiss: () -> Unit,
    onAjouter: (String, String, String, String) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var quantite by remember { mutableStateOf("1") }
    var unite by remember { mutableStateOf("") }
    var categorie by remember { mutableStateOf("Autre") }
    var expandedCategorie by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un article") },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Article *") },
                    placeholder = { Text("Ex: Lait, Pain, Yaourts...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantite,
                        onValueChange = { quantite = it },
                        label = { Text("Qté") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unite,
                        onValueChange = { unite = it },
                        label = { Text("Unité") },
                        placeholder = { Text("kg, L...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedCategorie,
                    onExpandedChange = { expandedCategorie = it }
                ) {
                    OutlinedTextField(
                        value = categorie,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Catégorie") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategorie) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategorie,
                        onDismissRequest = { expandedCategorie = false }
                    ) {
                        Categories.courses.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { categorie = cat; expandedCategorie = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(nom, quantite, unite, categorie) },
                enabled = nom.isNotBlank()
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
