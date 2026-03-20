package com.foyer.gestion.ui.screens.frigo

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foyer.gestion.data.model.ArticleFrigo
import com.foyer.gestion.data.repository.ProduitInfo
import com.foyer.gestion.ui.theme.MeliColors
import com.foyer.gestion.ui.theme.MeliShapes
import com.foyer.gestion.viewmodel.AuthViewModel
import com.foyer.gestion.viewmodel.Emplacement
import com.foyer.gestion.viewmodel.FrigoMode
import com.foyer.gestion.viewmodel.FrigoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

// ── Couleurs spécifiques frigo inspirées Méli Mélo ────────────────────────────
private val FrigoBg      = Color(0xFF0D2B25)
private val FrigoCard    = Color(0xFF163D33)
private val FrigoTab     = Color(0xFF1E4D40)
private val FrigoTabSel  = Color(0xFF25D197)
private val FrigoAccent  = Color(0xFF25D197)
private val FrigoText    = Color(0xFFFFFFFF)
private val FrigoMuted   = Color(0xFF7ABFAA)
private val DotGreen     = Color(0xFF2ECC71)
private val DotOrange    = Color(0xFFF39C12)
private val DotRed       = Color(0xFFE74C3C)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FrigoScreen(
    onBack: () -> Unit,
    viewModel: FrigoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val foyerId = authViewModel.authState.collectAsState().value.foyerId ?: ""
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(foyerId) { if (foyerId.isNotEmpty()) viewModel.observer(foyerId) }

    // Mode scanner en plein écran
    if (uiState.mode == FrigoMode.SCANNING) {
        if (cameraPermission.status.isGranted) {
            BarcodeScannerView(
                onBarcodeDetected = { barcode ->
                    viewModel.onBarcodeDetected(foyerId, barcode)
                },
                onDismiss = { viewModel.setMode(FrigoMode.NORMAL) }
            )
        } else {
            LaunchedEffect(Unit) { cameraPermission.launchPermissionRequest() }
            viewModel.setMode(FrigoMode.NORMAL)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FrigoBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(FrigoCard, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, "Retour", tint = FrigoText)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Au frigo",
                    color = FrigoText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { /* TODO: recherche dans la liste */ },
                    modifier = Modifier
                        .size(40.dp)
                        .background(FrigoCard, CircleShape)
                ) {
                    Icon(Icons.Default.Search, "Rechercher", tint = FrigoText)
                }
            }

            // ── Tabs emplacement ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Emplacement.values().forEach { emp ->
                    val selected = uiState.emplacementActif == emp
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(MeliShapes.pill)
                            .background(if (selected) FrigoTabSel else FrigoTab)
                            .clickable { viewModel.setEmplacement(emp) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            emp.label,
                            color = if (selected) FrigoBg else FrigoMuted,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Liste articles ───────────────────────────────────────────────
            val articles = uiState.articlesFiltres
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.emplacementActif.emoji, fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "${uiState.emplacementActif.label} vide",
                            color = FrigoText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Appuie sur + pour ajouter un aliment",
                            color = FrigoMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleRow(
                            article = article,
                            onSupprimer = { viewModel.supprimer(foyerId, article.id) }
                        )
                    }
                }
            }
        }

        // ── Boutons bas ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton home
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(52.dp)
                    .background(FrigoTab, CircleShape)
            ) {
                Icon(Icons.Default.Home, "Accueil", tint = FrigoText, modifier = Modifier.size(24.dp))
            }
            // Bouton +
            IconButton(
                onClick = { viewModel.setMode(FrigoMode.ADDING) },
                modifier = Modifier
                    .size(52.dp)
                    .background(FrigoAccent, CircleShape)
            ) {
                Icon(Icons.Default.Add, "Ajouter", tint = FrigoBg, modifier = Modifier.size(28.dp))
            }
        }
    }

    // ── Bottom sheet : méthode d'ajout ────────────────────────────────────────
    if (uiState.mode == FrigoMode.NORMAL && false) { /* placeholder */ }

    // Sheet sélection méthode
    var showMethodSheet by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.mode) {
        showMethodSheet = uiState.mode == FrigoMode.ADDING && uiState.produitPreselectionne == null && !uiState.isLoading
    }

    if (showMethodSheet) {
        MethodeAjoutSheet(
            onScannerCode = {
                showMethodSheet = false
                if (cameraPermission.status.isGranted) {
                    viewModel.setMode(FrigoMode.SCANNING)
                } else {
                    cameraPermission.launchPermissionRequest()
                }
            },
            onRechercherInternet = {
                showMethodSheet = false
                viewModel.setMode(FrigoMode.SEARCHING_ONLINE)
            },
            onAjouterManuellement = {
                showMethodSheet = false
                viewModel.selectionnerProduit(ProduitInfo("", null))
            },
            onDismiss = { viewModel.setMode(FrigoMode.NORMAL) }
        )
    }

    // Sheet recherche internet
    if (uiState.mode == FrigoMode.SEARCHING_ONLINE) {
        RechercheInternetSheet(
            query = uiState.searchQuery,
            results = uiState.searchResults,
            isSearching = uiState.isSearching,
            onSearch = { viewModel.rechercherSurInternet(it) },
            onSelect = { viewModel.selectionnerProduit(it) },
            onDismiss = { viewModel.setMode(FrigoMode.NORMAL) }
        )
    }

    // Sheet formulaire ajout (après sélection produit ou manuellement)
    if (uiState.mode == FrigoMode.ADDING && uiState.produitPreselectionne != null) {
        AjouterArticleSheet(
            produitPreselectionne = uiState.produitPreselectionne,
            emplacementDefaut = uiState.emplacementActif,
            articlesExistants = uiState.articles,
            isLoading = uiState.isLoading,
            onAjouter = { nom, quantite, categorie, dateExp ->
                viewModel.ajouter(
                    foyerId, nom, quantite, categorie, dateExp,
                    uiState.produitPreselectionne?.imageUrl
                )
                viewModel.setMode(FrigoMode.NORMAL)
            },
            onDismiss = { viewModel.setMode(FrigoMode.NORMAL) }
        )
    }
}

// ── Article Row ────────────────────────────────────────────────────────────────

@Composable
private fun ArticleRow(article: ArticleFrigo, onSupprimer: () -> Unit) {
    val joursRestants = article.dateExpiration?.let {
        val diff = it.toDate().time - System.currentTimeMillis()
        TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    val dotColor = when {
        joursRestants == null -> FrigoMuted
        joursRestants <= 0 -> DotRed
        joursRestants <= 3 -> DotOrange
        else -> DotGreen
    }
    val joursLabel = when {
        joursRestants == null -> ""
        joursRestants <= 0 -> "Périmé"
        else -> "${joursRestants}j"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MeliShapes.card)
            .background(FrigoCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image ou icône
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FrigoTab),
            contentAlignment = Alignment.Center
        ) {
            if (!article.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.nom,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            } else {
                Text(
                    when (article.categorie) {
                        "Congélateur" -> "❄️"
                        "Garde-manger" -> "📦"
                        else -> "🧊"
                    },
                    fontSize = 22.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(article.nom, color = FrigoText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            article.dateExpiration?.let { ts ->
                val fmt = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                Text(fmt.format(ts.toDate()), color = FrigoMuted, fontSize = 12.sp)
            }
        }

        // Jours restants + dot
        if (joursRestants != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    joursLabel,
                    color = dotColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        IconButton(onClick = onSupprimer, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.DeleteOutline,
                "Supprimer",
                tint = FrigoMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Sheet : Méthode d'ajout ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodeAjoutSheet(
    onScannerCode: () -> Unit,
    onRechercherInternet: () -> Unit,
    onAjouterManuellement: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrigoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ajouter au frigo",
                    color = FrigoText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Fermer", tint = FrigoMuted)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MeliShapes.card)
                    .background(FrigoTab)
            ) {
                Column {
                    MethodeItem(
                        icon = { Icon(Icons.Default.QrCodeScanner, "Scanner", tint = FrigoText, modifier = Modifier.size(22.dp)) },
                        label = "Scanner un code-barre",
                        onClick = onScannerCode,
                        showDivider = true
                    )
                    MethodeItem(
                        icon = { Icon(Icons.Default.Language, "Internet", tint = FrigoText, modifier = Modifier.size(22.dp)) },
                        label = "Rechercher sur internet",
                        onClick = onRechercherInternet,
                        showDivider = true
                    )
                    MethodeItem(
                        icon = { Icon(Icons.Default.Edit, "Manuel", tint = FrigoText, modifier = Modifier.size(22.dp)) },
                        label = "Ajouter manuellement",
                        onClick = onAjouterManuellement,
                        showDivider = false
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodeItem(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(label, color = FrigoText, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = FrigoMuted, modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            HorizontalDivider(color = FrigoBg.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

// ── Sheet : Recherche internet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RechercheInternetSheet(
    query: String,
    results: List<ProduitInfo>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onSelect: (ProduitInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var localQuery by remember { mutableStateOf(query) }
    val keyboard = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrigoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Rechercher un produit",
                    color = FrigoText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Fermer", tint = FrigoMuted)
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = localQuery,
                onValueChange = { localQuery = it },
                placeholder = { Text("Ex: Lait, Yaourts...", color = FrigoMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = FrigoMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    onSearch(localQuery)
                    keyboard?.hide()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FrigoAccent,
                    unfocusedBorderColor = FrigoTab,
                    focusedTextColor = FrigoText,
                    unfocusedTextColor = FrigoText,
                    cursorColor = FrigoAccent
                ),
                shape = MeliShapes.input,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            if (isSearching) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FrigoAccent)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results) { produit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(FrigoTab)
                                .clickable { onSelect(produit) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!produit.imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = produit.imageUrl,
                                    contentDescription = produit.nom,
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(produit.nom, color = FrigoText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                produit.marque?.let {
                                    Text(it, color = FrigoMuted, fontSize = 12.sp)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = FrigoMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Sheet : Formulaire d'ajout ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AjouterArticleSheet(
    produitPreselectionne: ProduitInfo?,
    emplacementDefaut: Emplacement,
    articlesExistants: List<ArticleFrigo>,
    isLoading: Boolean,
    onAjouter: (String, String, String, Timestamp?) -> Unit,
    onDismiss: () -> Unit
) {
    var nom by remember { mutableStateOf(produitPreselectionne?.nom ?: "") }
    var quantite by remember { mutableStateOf("") }
    var emplacement by remember { mutableStateOf(emplacementDefaut) }
    var dateExpiration by remember { mutableStateOf<Timestamp?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expandedProduit by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val nomsFrecuents = articlesExistants.map { it.nom }.distinct().filter { it.isNotBlank() }.sorted()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        dateExpiration = Timestamp(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK", color = FrigoAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler", color = FrigoMuted) }
            },
            colors = DatePickerDefaults.colors(containerColor = FrigoCard)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = FrigoCard,
                    titleContentColor = FrigoText,
                    headlineContentColor = FrigoText,
                    weekdayContentColor = FrigoMuted,
                    subheadContentColor = FrigoMuted,
                    dayContentColor = FrigoText,
                    selectedDayContainerColor = FrigoAccent,
                    selectedDayContentColor = FrigoBg,
                    todayContentColor = FrigoAccent,
                    todayDateBorderColor = FrigoAccent
                )
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FrigoCard,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titre
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Ajouter au frigo",
                    color = FrigoText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Fermer", tint = FrigoMuted)
                }
            }

            // Produit existant (optionnel)
            if (nomsFrecuents.isNotEmpty()) {
                Column {
                    Text("Produit (optionnel)", color = FrigoText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    ExposedDropdownMenuBox(expanded = expandedProduit, onExpandedChange = { expandedProduit = it }) {
                        OutlinedTextField(
                            value = if (expandedProduit) "" else "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Sélectionner un produit", color = FrigoMuted) },
                            leadingIcon = { Icon(Icons.Default.Inventory2, null, tint = FrigoMuted) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedProduit) },
                            colors = outlinedFieldColors(),
                            shape = MeliShapes.input,
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProduit,
                            onDismissRequest = { expandedProduit = false },
                            modifier = Modifier.background(FrigoTab)
                        ) {
                            nomsFrecuents.forEach { n ->
                                DropdownMenuItem(
                                    text = { Text(n, color = FrigoText) },
                                    onClick = { nom = n; expandedProduit = false }
                                )
                            }
                        }
                    }
                }
            }

            // Nom de l'aliment
            Column {
                Text("Nom de l'aliment", color = FrigoText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    placeholder = { Text("Ex: Lait, Yaourts...", color = FrigoMuted) },
                    leadingIcon = { Icon(Icons.Default.Kitchen, null, tint = FrigoMuted) },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Date d'expiration
            Column {
                Text("Date d'expiration", color = FrigoText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                val dateLabel = dateExpiration?.let {
                    SimpleDateFormat("d MMMM yyyy", Locale.FRENCH).format(it.toDate())
                } ?: "Sélectionner une date"
                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Sélectionner une date", color = FrigoMuted) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, "Date", tint = FrigoMuted)
                        }
                    },
                    colors = outlinedFieldColors(),
                    shape = MeliShapes.input,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                )
            }

            // Emplacement
            Column {
                Text("Emplacement", color = FrigoText, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Emplacement.values().forEach { emp ->
                        val selected = emplacement == emp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) FrigoAccent.copy(alpha = 0.15f) else FrigoTab)
                                .border(
                                    width = if (selected) 2.dp else 0.dp,
                                    color = if (selected) FrigoAccent else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { emplacement = emp }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emp.emoji, fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    emp.label,
                                    color = if (selected) FrigoAccent else FrigoText,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Bouton ajouter
            if (isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FrigoAccent)
                }
            } else {
                Button(
                    onClick = {
                        if (nom.isNotBlank()) {
                            onAjouter(nom, quantite, emplacement.label, dateExpiration)
                        }
                    },
                    enabled = nom.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MeliShapes.pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrigoAccent,
                        contentColor = FrigoBg,
                        disabledContainerColor = FrigoTab
                    )
                ) {
                    Text("Ajouter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FrigoAccent,
    unfocusedBorderColor = FrigoTab,
    focusedTextColor = FrigoText,
    unfocusedTextColor = FrigoText,
    cursorColor = FrigoAccent,
    focusedLeadingIconColor = FrigoMuted,
    unfocusedLeadingIconColor = FrigoMuted
)
