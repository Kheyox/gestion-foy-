package com.foyer.gestion.ui.screens.courses

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.foyer.gestion.data.model.ArticleCourse
import com.foyer.gestion.data.model.Categories
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
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
        containerColor = MeliColors.BgDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(listeNom, color = MeliColors.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = MeliColors.White)
                    }
                },
                actions = {
                    if (articlesCoches.isNotEmpty()) {
                        IconButton(onClick = { showDeleteCochesDialog = true }) {
                            Icon(Icons.Default.CleaningServices, contentDescription = "Vider cochés", tint = MeliColors.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MeliColors.BgDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MeliColors.CardMint,
                contentColor = MeliColors.BgDark,
                shape = MeliShapes.fab
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter article")
            }
        }
    ) { padding ->
        if (uiState.articles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛍️", fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Liste vide", color = MeliColors.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Appuie sur + pour ajouter un article", color = MeliColors.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp, bottom = 80.dp)
            ) {
                if (articlesNonCoches.isNotEmpty()) {
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MeliColors.BgDark)
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                "À acheter  •  ${articlesNonCoches.size}",
                                color = MeliColors.CardMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    items(articlesNonCoches, key = { it.id }) { article ->
                        Spacer(Modifier.height(8.dp))
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MeliColors.BgDark)
                                .padding(vertical = 10.dp)
                                .padding(top = if (articlesNonCoches.isNotEmpty()) 8.dp else 0.dp)
                        ) {
                            Text(
                                "Dans le panier  •  ${articlesCoches.size}",
                                color = MeliColors.CardYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    items(articlesCoches, key = { it.id }) { article ->
                        Spacer(Modifier.height(8.dp))
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
            shape = MeliShapes.bigCard,
            title = { Text("Vider les articles cochés ?", fontWeight = FontWeight.Bold) },
            text = { Text("Les ${articlesCoches.size} articles cochés seront supprimés.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.viderCoches(foyerId, listeId)
                        showDeleteCochesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeliColors.RedAlert),
                    shape = MeliShapes.pill
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
        if (article.estCoche) MeliColors.White.copy(alpha = 0.5f) else MeliColors.White,
        label = "bg"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = article.estCoche,
                onCheckedChange = { onCoche() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MeliColors.BgDark,
                    uncheckedColor = MeliColors.BgDark.copy(alpha = 0.4f)
                )
            )
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    article.nom,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (article.estCoche) TextDecoration.LineThrough else null,
                    color = if (article.estCoche) MeliColors.TextMuted else MeliColors.TextDark
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
                    Text(details, fontSize = 12.sp, color = MeliColors.TextMuted)
                }
            }
            IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = MeliColors.CardPink, modifier = Modifier.size(18.dp))
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
        shape = MeliShapes.bigCard,
        title = { Text("Ajouter un article", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Article *") },
                    placeholder = { Text("Ex: Lait, Pain, Yaourts...") },
                    singleLine = true,
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantite,
                        onValueChange = { quantite = it },
                        label = { Text("Qté") },
                        singleLine = true,
                        shape = MeliShapes.input,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unite,
                        onValueChange = { unite = it },
                        label = { Text("Unité") },
                        placeholder = { Text("kg, L...") },
                        singleLine = true,
                        shape = MeliShapes.input,
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
                        shape = MeliShapes.input,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCategorie, onDismissRequest = { expandedCategorie = false }) {
                        Categories.courses.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { categorie = cat; expandedCategorie = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAjouter(nom, quantite, unite, categorie) },
                enabled = nom.isNotBlank(),
                shape = MeliShapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = MeliColors.BgDark)
            ) { Text("Ajouter") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
